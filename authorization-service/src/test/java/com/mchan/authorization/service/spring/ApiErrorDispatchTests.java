package com.mchan.authorization.service.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.authorization.components.ApplicationOwnershipComponent;
import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.spring.controllers.SpringApplicationsController;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Verifies actual servlet error dispatch instead of MockMvc's direct exception handling.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = ApiErrorDispatchTests.ErrorApplication.class)
public class ApiErrorDispatchTests {

    @Configuration
    @EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        SecurityAutoConfiguration.class})
    @Import({SpringApplicationsController.class, CatchAllController.class})
    static class ErrorApplication {
    }

    @Autowired
    private TestRestTemplate rest;
    @MockBean
    private ClientCredentialsComponent clientCredentials;
    @MockBean
    private CreateApplicationComponent createApplicationComponent;
    @MockBean
    private DeleteApplicationComponent deleteApplicationComponent;
    @MockBean
    private UpdateApplicationComponent updateApplicationComponent;
    @MockBean
    private ApplicationOwnershipComponent applicationOwnershipComponent;
    @MockBean
    private AuthenticationFacade authenticationFacade;

    @BeforeEach
    public void authenticatedProfile() {
        when(authenticationFacade.getAuthenticationToken()).thenReturn(EntitiesAuthenticationToken.builder()
            .principal("owner").credentials("test-password").profile(Profile.builder().profileId("owner").build()).build());
    }

    @Test
    public void invalidApplication_should_returnBadRequestJsonThroughErrorDispatch() {
        when(updateApplicationComponent.updateApplication(any())).thenThrow(new InvalidArgumentException("Invalid application"));
        ResponseEntity<Map> response = rest.exchange("/api/applications/7", HttpMethod.PUT,
            new HttpEntity<>(Map.of("application", Map.of("appName", "example"))), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("path")).isEqualTo("/api/applications/7");
    }

    @Test
    public void internalApplicationFailure_should_returnJsonWithoutExceptionDetails() {
        String internalDetail = "PRIVATE_DATABASE_FAILURE_MARKER";
        when(updateApplicationComponent.updateApplication(any())).thenThrow(new RuntimeException(internalDetail));
        ResponseEntity<Map> response = rest.exchange("/api/applications/7", HttpMethod.PUT,
            new HttpEntity<>(Map.of("application", Map.of("appName", "example"))), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().toString()).doesNotContain(internalDetail);
    }

    @Test
    public void invalidCreate_should_returnBadRequestJson() {
        when(createApplicationComponent.createApplication(any())).thenThrow(new InvalidArgumentException("Invalid application"));
        ResponseEntity<Map> response = rest.postForEntity("/api/applications", Map.of("application", Map.of("appName", "example")), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("status")).isEqualTo(400);
    }

    @Test
    public void missingApplication_should_returnNotFoundJsonThroughErrorDispatch() {
        doThrow(new EntityNotFoundException("Application does not exist")).when(applicationOwnershipComponent).requireOwner(7, "owner");
        ResponseEntity<Map> response = rest.exchange("/api/applications/7", HttpMethod.PUT,
            new HttpEntity<>(Map.of("application", Map.of("appName", "example"))), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("status")).isEqualTo(404);
        verifyNoInteractions(updateApplicationComponent);
    }

    @Test
    public void missingApplicationPayload_should_returnBadRequestWithoutUpdating() {
        ResponseEntity<Map> response = rest.exchange("/api/applications/7", HttpMethod.PUT, new HttpEntity<>(Map.of()), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("status")).isEqualTo(400);
        verifyNoInteractions(updateApplicationComponent);
    }
}
