package com.mchan.authorization.service.authorization.spring.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.authorization.components.OauthTokensComponent;
import com.mchan.authorization.service.authorization.dao.entities.ClientCredentialEntity;
import com.mchan.authorization.service.spring.security.OauthSecurityConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exercises confidential-client OAuth HTTP errors and stateless transport rules.
 */
@WebMvcTest(controllers = SpringOauthController.class, properties = {"app.oauth.allow-insecure-localhost=false"})
@Import(OauthSecurityConfig.class)
public class OauthHttpTests {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private ClientCredentialsComponent credentials;
    @MockitoBean
    private OauthTokensComponent tokens;

    @Test
    public void token_should_authenticateClientAndDisableCachingAndSessions() throws Exception {
        ClientCredentialEntity client = new ClientCredentialEntity();
        client.setApplicationId(3);
        when(credentials.authenticate("client", "secret")).thenReturn(client);
        when(tokens.issue(any(), eq("target"), eq("read"))).thenReturn(Map.of("access_token", "opaque", "token_type", "Bearer"));
        mvc.perform(post("/oauth/token").secure(true).with(httpBasic("client", "secret"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "client_credentials")
            .param("audience", "target").param("scope", "read"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(header().string("Cache-Control", "no-store")).andExpect(cookie().doesNotExist("JSESSIONID"));
    }

    @Test
    public void userSession_should_notAuthenticateAnOauthClient() throws Exception {
        mvc.perform(post("/oauth/token").secure(true).session(new MockHttpSession())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "client_credentials").param("audience", "target"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.error").value("invalid_client"))
            .andExpect(header().string("WWW-Authenticate", "Basic realm=\"oauth\""));
        verifyNoInteractions(credentials, tokens);
    }

    @Test
    public void duplicateParametersAndBodyCredentials_should_beRejectedBeforeAuthentication() throws Exception {
        mvc.perform(post("/oauth/token").secure(true).contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials", "password")).andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("invalid_request"));
        mvc.perform(post("/oauth/token").secure(true).contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("client_secret", "submitted-secret")).andExpect(status().isBadRequest());
        verifyNoInteractions(credentials, tokens);
    }

    @Test
    public void insecureTransport_should_beRejectedBeforeReadingCredentials() throws Exception {
        mvc.perform(post("/oauth/token").contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("grant_type", "client_credentials")).andExpect(status().isBadRequest());
        verifyNoInteractions(credentials, tokens);
    }

    @Test
    public void developmentTransport_should_rejectNonLoopbackPeersAndSpoofedForwardedHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.10");
        request.setLocalAddr("127.0.0.1");
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.addHeader("X-Forwarded-Proto", "https");
        request.addParameter("grant_type", "client_credentials");
        SpringOauthController controller = new SpringOauthController(credentials, tokens, true);
        org.junit.jupiter.api.Assertions.assertEquals(400, controller.token(request).getStatusCode().value());
        verifyNoInteractions(credentials, tokens);
    }

    @Test
    public void disallowedGrantsAndMalformedBasic_should_haveProtocolErrors() throws Exception {
        when(credentials.authenticate("client", "secret")).thenReturn(new ClientCredentialEntity());
        mvc.perform(post("/oauth/token").secure(true).with(httpBasic("client", "secret"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED).param("grant_type", "password"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("unsupported_grant_type"));
        mvc.perform(post("/oauth/token").secure(true).header("Authorization", "Basic !!!")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)).andExpect(status().isUnauthorized());
        verifyNoInteractions(tokens);
    }
}
