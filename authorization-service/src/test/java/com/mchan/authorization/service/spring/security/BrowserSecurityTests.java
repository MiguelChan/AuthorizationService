package com.mchan.authorization.service.spring.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.components.EditProfileComponent;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.components.LogInComponent;
import com.mchan.authorization.service.entities.spring.controllers.SpringProfileController;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Reproduces cookie/browser forgery and rejects untrusted credentialed origins.
 */
@WebMvcTest(controllers = {SpringProfileController.class, CsrfController.class},
    properties = "app.security.allowed-origins=https://trusted.example")
@Import({WebSecurityConfig.class, EntitiesAuthenticationProvider.class, AuthenticationFacade.class})
public class BrowserSecurityTests {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private LogInComponent logins;
    @MockBean
    private GetProfileComponent profiles;
    @MockBean
    private EditProfileComponent edits;

    @Test
    public void browserMutationAndLogin_should_requireCsrfBeforeExecution() throws Exception {
        Profile profile = Profile.builder().profileId("owner").build();
        EntitiesAuthenticationToken token = new EntitiesAuthenticationToken("owner", null, profile);
        mvc.perform(put("/api/profile").with(authentication(token)).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/login").param("username", "owner").param("password", "secret"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(edits, logins);
        when(edits.editProfile(eq("owner"), any())).thenReturn(true);
        mvc.perform(put("/api/profile").with(authentication(token)).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    public void basicHeaderWithSession_should_notBypassCsrf() throws Exception {
        mvc.perform(put("/api/profile").session(new MockHttpSession()).header("Authorization", "Basic b3duZXI6c2VjcmV0")
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(edits, logins);
    }

    @Test
    public void arbitraryOrigin_should_notReadCredentialedProfile() throws Exception {
        mvc.perform(get("/api/profile").header("Origin", "https://attacker.example"))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        verifyNoInteractions(profiles);
    }

    @Test
    public void browserCsrfToken_should_bePublicAndUncached() throws Exception {
        mvc.perform(get("/api/csrf")).andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(header().string("Cache-Control", "no-store"));
    }
}
