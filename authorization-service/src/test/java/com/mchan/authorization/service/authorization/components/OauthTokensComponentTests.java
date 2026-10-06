package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.models.ApplicationGrant;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.authorization.dao.entities.OauthIntrospectionEntity;
import com.mchan.authorization.service.authorization.dao.entities.OauthTokenEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationGrantsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.ClientCredentialsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.OauthTokensMapper;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

/**
 * Verifies scoped opaque issuance and recipient-bound validation.
 */
public class OauthTokensComponentTests {
    private final OauthTokensMapper tokens = mock(OauthTokensMapper.class);
    private final ApplicationGrantsMapper grants = mock(ApplicationGrantsMapper.class);
    private final ClientCredentialsMapper clients = mock(ClientCredentialsMapper.class);
    private final OauthTokensComponent component = new OauthTokensComponent(tokens, grants, clients, "https://issuer.example", 300);

    @Test
    public void issue_should_storeOnlyDigestAndVersionedPermissions() {
        ClientCredentialEntity source = client(3, "source", 2);
        ClientCredentialEntity target = client(7, "target", 4);
        when(clients.findActive("target")).thenReturn(target);
        when(grants.allowed(3, 7)).thenReturn(List.of(ApplicationGrant.builder().grantId(9).version(5).action("orders.read").build()));
        Map<String, Object> response = component.issue(source, "target", "orders.read");
        String value = (String) response.get("access_token");
        assertThat(value).matches("[A-Za-z0-9_-]{43}");
        assertThat(response.get("token_type")).isEqualTo("Bearer");
        ArgumentCaptor<OauthTokenEntity> captor = ArgumentCaptor.forClass(OauthTokenEntity.class);
        verify(tokens).create(captor.capture());
        OauthTokenEntity token = captor.getValue();
        assertThat(token.getTokenHash()).matches("[0-9a-f]{64}").isNotEqualTo(value);
        assertThat(token.getSourceCredentialVersion()).isEqualTo(2);
        assertThat(token.getTargetCredentialVersion()).isEqualTo(4);
        assertThat(token.getExpiresAt().getTime() - token.getIssuedAt().getTime()).isEqualTo(300000);
        verify(tokens).addPermission(token.getTokenHash(), 9, 5);
    }

    @Test
    public void ungrantedScopes_should_neverIssueTokens() {
        when(clients.findActive("target")).thenReturn(client(7, "target", 4));
        when(grants.allowed(3, 7)).thenReturn(List.of(ApplicationGrant.builder().action("orders.read").build()));
        assertThatThrownBy(() -> component.issue(client(3, "source", 2), "target", "orders.delete")).hasMessage("invalid_scope");
        assertThatThrownBy(() -> component.issue(client(3, "source", 2), "target", "")).hasMessage("invalid_scope");
        verify(tokens, never()).create(ArgumentMatchers.any());
    }

    @Test
    public void introspection_should_bindIssuerAudienceExpiryAndLivePermissions() {
        OauthIntrospectionEntity stored = new OauthIntrospectionEntity();
        stored.setSourceId(3);
        stored.setTargetId(7);
        stored.setTargetCredentialVersion(4);
        stored.setIssuer("https://issuer.example");
        stored.setIssuedAt(Date.from(Instant.now()));
        stored.setExpiresAt(Date.from(Instant.now().plusSeconds(300)));

        String value = "a".repeat(43);
        assertThat(component.introspect(client(8, "other", 4), value)).isEqualTo(Map.of("active", false));
        assertThat(component.introspect(client(7, "target", 5), value)).isEqualTo(Map.of("active", false));
        stored.setEndpointId(9);
        stored.setAction("orders.read");
        stored.setHttpMethod("GET");
        stored.setPath("/orders");
        stored.setSourceClientId("source");
        when(tokens.introspection(ArgumentMatchers.anyString(), ArgumentMatchers.eq(7), ArgumentMatchers.eq(4L),
            ArgumentMatchers.eq("https://issuer.example"))).thenReturn(List.of(stored));
        Map<String, Object> response = component.introspect(client(7, "target", 4), value);
        assertThat(response.get("active")).isEqualTo(true);
        assertThat(response.get("aud")).isEqualTo("target");
        assertThat(response.get("scope")).isEqualTo("orders.read");
        stored.setIssuer("https://wrong.example");
        assertThat(component.introspect(client(7, "target", 4), value)).isEqualTo(Map.of("active", false));
        stored.setIssuer("https://issuer.example");
        stored.setExpiresAt(Date.from(Instant.now().minusSeconds(1)));
        assertThat(component.introspect(client(7, "target", 4), value)).isEqualTo(Map.of("active", false));
    }

    @Test
    public void invalidConfiguration_should_failAtStartup() {
        assertThatThrownBy(() -> new OauthTokensComponent(tokens, grants, clients, "http://remote.example", 300))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OauthTokensComponent(tokens, grants, clients, "http:bad", 300))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OauthTokensComponent(tokens, grants, clients, "https://issuer.example", 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    private ClientCredentialEntity client(int id, String clientId, long version) {
        ClientCredentialEntity entity = new ClientCredentialEntity();
        entity.setApplicationId(id);
        entity.setClientId(clientId);
        entity.setVersion(version);
        return entity;
    }
}
