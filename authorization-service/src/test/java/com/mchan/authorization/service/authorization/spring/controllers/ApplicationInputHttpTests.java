package com.mchan.authorization.service.authorization.spring.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.lib.models.AppType;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.authorization.components.ApplicationInputValidator;
import com.mchan.authorization.service.authorization.components.ApplicationOwnershipComponent;
import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.authorization.mappers.ApplicationMapper;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import com.mchan.authorization.service.spring.security.WebSecurityConfig;
import com.mchan.authorization.service.utils.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Exercises JSON type decoding and the real input, ownership and mutation components.
 */
@WebMvcTest(controllers = SpringApplicationsController.class)
@Import({WebSecurityConfig.class, AuthenticationFacade.class, ApplicationOwnershipComponent.class,
    CreateApplicationComponent.class, UpdateApplicationComponent.class, ApplicationMapper.class,
    ApplicationInputValidator.class, ObjectUtils.class})
public class ApplicationInputHttpTests {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private ClientCredentialsComponent clientCredentials;
    @MockBean
    private ApplicationDao applicationDao;
    @MockBean
    private ProfileDao profileDao;
    @MockBean
    private DeleteApplicationComponent deleteApplicationComponent;

    /**
     * Verifies JSON values survive the real application mapping path.
     *
     * @param value Supported API category.
     * @throws Exception On an HTTP test failure.
     */
    @ParameterizedTest
    @ValueSource(strings = {"Service", "WebService"})
    public void supportedType_should_reachPersistence(String value) throws Exception {
        when(clientCredentials.issue(org.mockito.ArgumentMatchers.anyInt())).thenReturn(new ClientCredentials("client", "secret", 1));
        when(profileDao.getProfile("owner")).thenReturn(ProfileEntity.builder().profileId("owner").build());
        when(applicationDao.createApplication(any())).thenReturn(7);
        mvc.perform(post("/api/applications").with(csrf()).with(owner()).contentType(MediaType.APPLICATION_JSON)
            .content(payload("https://example.com", ",\"appType\":\"" + value + "\""))).andExpect(status().isOk());
        ArgumentCaptor<ApplicationEntity> captor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(applicationDao).createApplication(captor.capture());
        assertThat(captor.getValue().getAppType()).isEqualTo(AppType.fromValue(value));
    }

    /**
     * Rejects unsupported values, including numeric enum ordinals.
     *
     * @param value Invalid JSON type value.
     * @throws Exception On an HTTP test failure.
     */
    @ParameterizedTest
    @ValueSource(strings = {"\"Mobile\"", "\"SERVICE\"", "\"\"", "1", "true", "{}"})
    public void unsupportedTypes_should_returnBadRequestBeforePersistence(String value) throws Exception {
        mvc.perform(post("/api/applications").with(csrf()).with(owner()).contentType(MediaType.APPLICATION_JSON)
            .content(payload("https://example.com", ",\"appType\":" + value))).andExpect(status().isBadRequest());
        verifyNoInteractions(profileDao, applicationDao);
    }

    @Test
    public void invalidCreationUrl_should_returnBadRequestBeforePersistence() throws Exception {
        mvc.perform(post("/api/applications").with(csrf()).with(owner()).contentType(MediaType.APPLICATION_JSON)
            .content(payload("https://example.com/callback", ""))).andExpect(status().isBadRequest());
        verifyNoInteractions(profileDao, applicationDao);
    }

    @Test
    public void invalidUpdateUrl_should_notWrite() throws Exception {
        when(applicationDao.getApplication(7)).thenReturn(ApplicationEntity.builder().profileId("owner").build());
        mvc.perform(put("/api/applications/7").with(csrf()).with(owner()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"application\":{\"redirectUrl\":\"https://example.com?next=app\"}}"))
            .andExpect(status().isBadRequest());
        verify(applicationDao, never()).updateApplication(any());
    }

    private String payload(String url, String extra) {
        return "{\"application\":{\"appName\":\"example\",\"shortDescription\":\"test\",\"redirectUrl\":\"" + url + "\"" + extra + "}}";
    }

    private RequestPostProcessor owner() {
        return authentication(EntitiesAuthenticationToken.builder().principal("owner").credentials("test-password")
            .profile(Profile.builder().profileId("owner").build()).build());
    }
}
