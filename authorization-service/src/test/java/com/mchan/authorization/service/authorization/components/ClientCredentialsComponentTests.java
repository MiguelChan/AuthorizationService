package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ClientCredentialsMapper;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests random secret delivery, one-way verification and rotation behavior.
 */
public class ClientCredentialsComponentTests {
    private final ClientCredentialsMapper mapper = mock(ClientCredentialsMapper.class);
    private final SecurePasswordUtils passwords = new SecurePasswordUtils("test-pepper", 4);
    private final ClientCredentialsComponent component = new ClientCredentialsComponent(mapper, passwords);

    @Test
    public void issue_should_deliverRandomSecretAndPersistOnlyHash() throws Exception {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        ClientCredentials first = component.issue(7);
        ClientCredentials second = component.issue(7);
        assertThat(first.getClientSecret()).matches("[A-Za-z0-9_-]{43}").isNotEqualTo(second.getClientSecret());
        assertThat(first.toString()).doesNotContain(first.getClientSecret());
        ArgumentCaptor<ClientCredentialEntity> captor = ArgumentCaptor.forClass(ClientCredentialEntity.class);
        Mockito.verify(mapper, Mockito.times(2)).save(captor.capture());
        ClientCredentialEntity stored = captor.getAllValues().get(0);
        assertThat(stored.getSecretHash()).isNotEqualTo(first.getClientSecret());
        assertThat(passwords.isValidPassword(first.getClientSecret(), stored.getSecretHash())).isTrue();
        assertThat(stored.toString()).doesNotContain(stored.getSecretHash());
        when(mapper.findActive(first.getClientId())).thenReturn(stored);
        assertThat(component.authenticate(first.getClientId(), first.getClientSecret())).isSameAs(stored);
        assertThat(component.authenticate(first.getClientId(), second.getClientSecret())).isNull();
    }

    @Test
    public void rotation_should_preserveIdentityAndIncreaseVersion() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        ClientCredentialEntity stored = new ClientCredentialEntity();
        stored.setClientId("stable-client-id");
        stored.setVersion(4);
        when(mapper.findByApplication(7)).thenReturn(stored);
        ClientCredentials rotated = component.issue(7);
        assertThat(rotated.getClientId()).isEqualTo("stable-client-id");
        assertThat(rotated.getVersion()).isEqualTo(5);
    }

    @Test
    public void inactiveApplications_should_notIssueCredentials() {
        when(mapper.lockActiveApplication(7)).thenReturn(null);
        assertThatThrownBy(() -> component.issue(7)).hasMessage("Application must be active");
        Mockito.verify(mapper, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void malformedCredentials_should_notQueryStorage() {
        assertThat(component.authenticate(null, null)).isNull();
        assertThat(component.authenticate("client", "short")).isNull();
        verifyNoInteractions(mapper);
    }
}
