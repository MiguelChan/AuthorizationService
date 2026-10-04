package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.models.ApplicationEndpoint;
import com.mchan.authorization.service.authorization.dao.mappers.ApplicationEndpointsMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

/**
 * Covers endpoint validation and immutable authorization identity.
 */
public class ApplicationEndpointsComponentTests {
    private final ApplicationEndpointsMapper mapper = mock(ApplicationEndpointsMapper.class);
    private final ApplicationEndpointsComponent component = new ApplicationEndpointsComponent(mapper);

    @Test
    public void create_should_usePathApplicationAndSupportNamedParameters() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        ApplicationEndpoint input = ApplicationEndpoint.builder().applicationId(99).endpointId(88).active(false)
            .httpMethod("GET").path("/orders/{id}").action("orders.read").build();
        ApplicationEndpoint created = component.create(7, input);
        assertThat(created.getApplicationId()).isEqualTo(7);
        assertThat(created.getEndpointId()).isZero();
        assertThat(created.isActive()).isTrue();
        verify(mapper).create(created);
    }

    @Test
    public void invalidPaths_should_neverPersist() {
        for (String path : new String[]{"https://example.com", "/../orders", "/orders?all=true", "/orders#all", "/orders/*", "/%2e%2e", "/a//b"}) {
            assertThatThrownBy(() -> component.create(7, ApplicationEndpoint.builder()
                .httpMethod("GET").path(path).action("orders.read").build())).isInstanceOf(RuntimeException.class);
        }
        verify(mapper, never()).create(ArgumentMatchers.any());
    }

    @Test
    public void update_should_preserveIdentityAndRejectIdentityChanges() {
        when(mapper.lockActiveApplication(7)).thenReturn(7);
        ApplicationEndpoint stored = ApplicationEndpoint.builder().applicationId(7).endpointId(2).active(true)
            .httpMethod("GET").path("/orders").action("orders.read").description("Old").build();
        when(mapper.get(7, 2)).thenReturn(stored);
        ApplicationEndpoint updated = component.update(7, 2, ApplicationEndpoint.builder().description("New").build());
        assertThat(updated.getDescription()).isEqualTo("New");
        assertThat(updated.getAction()).isEqualTo("orders.read");
        assertThatThrownBy(() -> component.update(7, 2, ApplicationEndpoint.builder().action("orders.delete").build()))
            .hasMessageContaining("identity is immutable");
    }
}
