package com.mchan.authorization.service.entities.components;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.IdentificationEntityDao;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import com.mchan.authorization.service.entities.utils.SignUpRequestValidator;
import io.github.benas.randombeans.api.EnhancedRandom;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class SignUpComponentTests {

    @Mock
    private SignUpRequestValidator signUpRequestValidator;
    @Mock
    private ProfileDao profileDao;
    @Mock
    private AccountDao accountDao;
    @Mock
    private IdentificationEntityDao identificationEntityDao;
    @Mock
    private SecurePasswordUtils securePasswordUtils;

    private SignUpComponent signUpComponent;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        signUpComponent = new SignUpComponent(
            signUpRequestValidator,
            profileDao,
            accountDao,
            identificationEntityDao,
            securePasswordUtils
        );
    }

    @Test
    public void signUp_should_signUpaUser() throws Exception {
        String expectedIdentityId = "ThisIsTheFinalId";
        String profileId = "AProfileId";
        String accountId = "SomeAccountId";
        SignUpRequest signUpRequest = EnhancedRandom.random(SignUpRequest.class);

        when(profileDao.createProfile(any())).thenReturn(profileId);
        when(securePasswordUtils.createSecurePassword(any())).thenReturn("AString");
        when(accountDao.createBasicAccount(any())).thenReturn(accountId);
        when(identificationEntityDao.createIdentificationEntity(any())).thenReturn(expectedIdentityId);

        Optional<String> identityIdOpt = signUpComponent.signUp(signUpRequest);

        assertThat(identityIdOpt.isPresent()).isTrue();
        assertThat(identityIdOpt.get()).isEqualTo(profileId);
        verify(signUpRequestValidator).validateSignUpRequest(signUpRequest);
    }



}
