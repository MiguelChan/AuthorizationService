package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import com.mchan.authorization.lib.models.ApplicationGrant;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationEndpointsMapper;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationGrantsMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

/**
 * Tests target-scoped grants and default-deny directional evaluation.
 */
public class ApplicationGrantsComponentTests {
    private final ApplicationGrantsMapper mapper = mock(ApplicationGrantsMapper.class);
    private final ApplicationEndpointsMapper endpoints = mock(ApplicationEndpointsMapper.class);
    private final ApplicationDao applications = mock(ApplicationDao.class);
    private final ApplicationGrantsComponent component = new ApplicationGrantsComponent(mapper, endpoints, applications);

    @Test
    public void create_should_bindTheGrantToThePathTarget() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        when(applications.getApplication(3)).thenReturn(ApplicationEntity.builder().applicationId(3).isActive(true).build());
        when(endpoints.get(7, 2)).thenReturn(ApplicationEndpoint.builder().applicationId(7).endpointId(2).active(true).build());
        when(mapper.create(ArgumentMatchers.any())).thenReturn(1);
        component.create(7, ApplicationGrant.builder().sourceApplicationId(3).endpointId(2).targetApplicationId(99).version(100).build());
        ArgumentCaptor<ApplicationGrant> captor = ArgumentCaptor.forClass(ApplicationGrant.class);
        verify(mapper).create(captor.capture());
        assertThat(captor.getValue().getTargetApplicationId()).isEqualTo(7);
        assertThat(captor.getValue().getSourceApplicationId()).isEqualTo(3);
        assertThat(captor.getValue().getVersion()).isZero();
    }

    @Test
    public void invalidTargetEndpoint_should_notCreateGrant() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        when(applications.getApplication(3)).thenReturn(ApplicationEntity.builder().applicationId(3).isActive(true).build());
        assertThatThrownBy(() -> component.create(7, ApplicationGrant.builder().sourceApplicationId(3).endpointId(2).build()))
            .hasMessageContaining("belong to the target");
        verify(mapper, never()).create(ArgumentMatchers.any());
    }

    @Test
    public void evaluation_should_denyMissingAndReverseGrants() {
        ApplicationGrant grant = ApplicationGrant.builder().sourceApplicationId(3).targetApplicationId(7).endpointId(2).build();
        when(mapper.allowed(3, 7)).thenReturn(List.of(grant));
        assertThat(component.isAllowed(3, 7, 2)).isTrue();
        assertThat(component.isAllowed(3, 7, 4)).isFalse();
        assertThat(component.isAllowed(7, 3, 2)).isFalse();
        when(mapper.allowed(3, 7)).thenReturn(List.of());
        assertThat(component.isAllowed(3, 7, 2)).isFalse();
    }

    @Test
    public void missingOrRevokedGrant_should_returnNotFound() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        assertThatThrownBy(() -> component.revoke(7, 2)).hasMessageContaining("404");
    }
}
