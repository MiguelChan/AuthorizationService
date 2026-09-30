package com.mchan.authorization.service.spring.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.spring.controllers.SpringApplicationsController;
import com.mchan.authorization.service.entities.components.EditProfileComponent;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.components.LogInComponent;
import com.mchan.authorization.service.entities.components.SignUpComponent;
import com.mchan.authorization.service.entities.dao.HealthDao;
import com.mchan.authorization.service.entities.spring.controllers.SpringHealthController;
import com.mchan.authorization.service.entities.spring.controllers.SpringProfileController;
import com.mchan.authorization.service.entities.spring.controllers.SpringSignUpController;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.spring.CatchAllController;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Exercises the real security filter chain before controllers and components.
 */
@WebMvcTest(controllers = {SpringApplicationsController.class, SpringProfileController.class,
    SpringHealthController.class, SpringSignUpController.class, CatchAllController.class})
@Import({WebSecurityConfig.class, EntitiesAuthenticationProvider.class, AuthenticationFacade.class})
public class WebSecurityConfigTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private CreateApplicationComponent createApplicationComponent;
    @MockBean
    private UpdateApplicationComponent updateApplicationComponent;
    @MockBean
    private DeleteApplicationComponent deleteApplicationComponent;
    @MockBean
    private GetProfileComponent getProfileComponent;
    @MockBean
    private EditProfileComponent editProfileComponent;
    @MockBean
    private SignUpComponent signUpComponent;
    @MockBean
    private LogInComponent logInComponent;
    @MockBean
    private HealthDao healthDao;

    @Test
    public void anonymousRequests_should_beRejectedBeforePrivateComponents() throws Exception {
        mvc.perform(get("/api/profile").accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
        mvc.perform(put("/api/profile").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/applications").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/applications/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/applications/1")).andExpect(status().isUnauthorized());
        verifyNoInteractions(createApplicationComponent, updateApplicationComponent, deleteApplicationComponent,
            getProfileComponent, editProfileComponent);
    }

    @Test
    public void unknownApiRoutesAndHtmlClients_should_requireAuthentication() throws Exception {
        mvc.perform(get("/api/unmapped")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/profile").accept(MediaType.TEXT_HTML)).andExpect(status().isUnauthorized());
    }

    @Test
    public void publicRoutes_should_remainAvailable() throws Exception {
        when(signUpComponent.signUp(any())).thenReturn(Optional.of("profile-1"));
        when(healthDao.isHealthy()).thenReturn(true);
        mvc.perform(post("/api/sign-up").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk());
        mvc.perform(get("/api/ping")).andExpect(status().isOk());
        mvc.perform(get("/api/deep_ping")).andExpect(status().isOk());
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
        // Missing static files may return 404, but must not trigger authentication.
        mvc.perform(get("/static/missing.js")).andExpect(status().isNotFound());
    }

    @Test
    public void signUpPreflight_should_allowAnonymousCrossOriginRequests() throws Exception {
        mvc.perform(options("/api/sign-up")
                .header(HttpHeaders.ORIGIN, "https://client.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        verifyNoInteractions(signUpComponent, logInComponent);
    }

    @Test
    public void privateApiPreflight_should_allowAuthorizationHeaderWithoutCredentials() throws Exception {
        mvc.perform(options("/api/profile")
                .header(HttpHeaders.ORIGIN, "https://client.example")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "authorization"))
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        verifyNoInteractions(getProfileComponent, logInComponent);
    }

    @Test
    public void actualCrossOriginRequests_should_preservePublicAndPrivateAuthenticationRules() throws Exception {
        when(signUpComponent.signUp(any())).thenReturn(Optional.of("profile-1"));
        mvc.perform(post("/api/sign-up").header(HttpHeaders.ORIGIN, "https://client.example")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example"));
        mvc.perform(get("/api/profile").header(HttpHeaders.ORIGIN, "https://client.example"))
            .andExpect(status().isUnauthorized())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example"));
        verifyNoInteractions(getProfileComponent, logInComponent);
        Profile profile = Profile.builder().profileId("profile-1").build();
        when(logInComponent.logIn(any())).thenReturn(profile);
        when(getProfileComponent.getProfile("profile-1")).thenReturn(profile);
        mvc.perform(get("/api/profile").header(HttpHeaders.ORIGIN, "https://client.example")
                .with(httpBasic("user@example.com", "test-password")))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://client.example"));
    }

    @Test
    public void basicAndFormLogin_should_returnTheAuthenticatedProfile() throws Exception {
        Profile profile = Profile.builder().profileId("profile-1").build();
        when(logInComponent.logIn(any())).thenReturn(profile);
        when(getProfileComponent.getProfile("profile-1")).thenReturn(profile);
        mvc.perform(get("/api/profile").with(httpBasic("user@example.com", "test-password")))
            .andExpect(status().isOk());
        MvcResult login = mvc.perform(post("/login").param("username", "user@example.com").param("password", "test-password"))
            .andExpect(status().isFound()).andExpect(redirectedUrl("/api/profile")).andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        mvc.perform(get("/api/profile").session(session)).andExpect(status().isOk());
    }
}
