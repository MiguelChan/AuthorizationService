package com.mchan.authorization.service.authorization.spring.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mchan.authorization.lib.dtos.ClientCredentials;
import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.authorization.components.ApplicationOwnershipComponent;
import com.mchan.authorization.service.authorization.components.ClientCredentialsComponent;
import com.mchan.authorization.service.authorization.components.CreateApplicationComponent;
import com.mchan.authorization.service.authorization.components.DeleteApplicationComponent;
import com.mchan.authorization.service.authorization.components.UpdateApplicationComponent;
import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.entities.spring.facade.AuthenticationFacade;
import com.mchan.authorization.service.spring.security.EntitiesAuthenticationToken;
import com.mchan.authorization.service.spring.security.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Exercises the controller and actual ownership check for two distinct profiles.
 */
@WebMvcTest(controllers = SpringApplicationsController.class)
@Import({WebSecurityConfig.class, AuthenticationFacade.class, ApplicationOwnershipComponent.class})
public class ApplicationOwnershipHttpTests {

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ClientCredentialsComponent clientCredentials;
    @MockBean
    private CreateApplicationComponent createApplicationComponent;
    @MockBean
    private DeleteApplicationComponent deleteApplicationComponent;
    @MockBean
    private UpdateApplicationComponent updateApplicationComponent;
    @MockBean
    private ApplicationDao applicationDao;

    @Test
    public void create_should_useTheAuthenticatedOwnerWhenProfileIdIsOmitted() throws Exception {
        when(clientCredentials.issue(org.mockito.ArgumentMatchers.anyInt())).thenReturn(new ClientCredentials("client", "secret", 1));
        when(createApplicationComponent.createApplication(any())).thenReturn(7);
        mvc.perform(post("/api/applications").with(csrf()).with(profile("owner")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"application\":{\"appName\":\"example\"}}"))
            .andExpect(status().isOk());
        ArgumentCaptor<CreateApplicationRequest> captor = ArgumentCaptor.forClass(CreateApplicationRequest.class);
        verify(createApplicationComponent).createApplication(captor.capture());
        assertThat(captor.getValue().getProfileId()).isEqualTo("owner");
    }

    @Test
    public void create_should_rejectAnotherProfilesIdBeforeWriting() throws Exception {
        mvc.perform(post("/api/applications").with(csrf()).with(profile("other")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"profileId\":\"owner\",\"application\":{}}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(createApplicationComponent);
    }

    @Test
    public void owner_should_updateAndDeactivateTheirApplication() throws Exception {
        when(applicationDao.getApplication(7)).thenReturn(ApplicationEntity.builder().profileId("owner").build());
        when(updateApplicationComponent.updateApplication(any())).thenReturn(true);
        mvc.perform(put("/api/applications/7").with(csrf()).with(profile("owner")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"application\":{\"appName\":\"changed\"}}"))
            .andExpect(status().isOk());
        mvc.perform(delete("/api/applications/7").with(csrf()).with(profile("owner"))).andExpect(status().isOk());
        verify(updateApplicationComponent).updateApplication(any());
        verify(deleteApplicationComponent).deleteApplication(7);
    }

    @Test
    public void otherProfile_should_notUpdateOrDeactivateAnOwnersApplication() throws Exception {
        when(applicationDao.getApplication(7)).thenReturn(ApplicationEntity.builder().profileId("owner").build());
        mvc.perform(put("/api/applications/7").with(csrf()).with(profile("other")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"application\":{\"appName\":\"attack\"}}"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/api/applications/7").with(csrf()).with(profile("other"))).andExpect(status().isForbidden());
        verifyNoInteractions(updateApplicationComponent, deleteApplicationComponent);
    }

    @Test
    public void missingApplication_should_returnNotFoundWithoutWriting() throws Exception {
        mvc.perform(put("/api/applications/7").with(csrf()).with(profile("owner")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"application\":{}}"))
            .andExpect(status().isNotFound());
        mvc.perform(delete("/api/applications/7").with(csrf()).with(profile("owner"))).andExpect(status().isNotFound());
        verifyNoInteractions(updateApplicationComponent, deleteApplicationComponent);
    }

    private RequestPostProcessor profile(String profileId) {
        return authentication(EntitiesAuthenticationToken.builder().principal(profileId).credentials("test-password")
            .profile(Profile.builder().profileId(profileId).build()).build());
    }
}
