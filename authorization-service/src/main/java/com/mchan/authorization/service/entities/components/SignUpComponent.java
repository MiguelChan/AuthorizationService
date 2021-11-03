package com.mchan.authorization.service.entities.components;

import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.models.Account;
import com.mchan.authorization.lib.models.AccountType;
import com.mchan.authorization.lib.models.Entity;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.IdentificationEntityDao;
import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.entities.IdentificationEntity;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import com.mchan.authorization.service.entities.utils.SignUpRequestValidator;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Component that validates the SignUpRequest and Creates and appropriate profile if valid.
 * This Component will also make the association between an Account, Entity and Profile.
 */
@Log4j2
@Component
public class SignUpComponent {

    private final SignUpRequestValidator signUpRequestValidator;
    private final ProfileDao profileDao;
    private final AccountDao accountDao;
    private final IdentificationEntityDao identificationEntityDao;
    private final SecurePasswordUtils securePasswordUtils;

    /**
     * .
     *
     * @param signUpRequestValidator .
     *
     * @param profileDao .
     *
     * @param accountDao .
     *
     * @param identificationEntityDao .
     *
     * @param securePasswordUtils .
     */
    @Autowired
    public SignUpComponent(SignUpRequestValidator signUpRequestValidator,
                           ProfileDao profileDao,
                           AccountDao accountDao,
                           IdentificationEntityDao identificationEntityDao,
                           SecurePasswordUtils securePasswordUtils) {
        this.signUpRequestValidator = signUpRequestValidator;
        this.profileDao = profileDao;
        this.accountDao = accountDao;
        this.identificationEntityDao = identificationEntityDao;
        this.securePasswordUtils = securePasswordUtils;
    }

    /**
     * Method that signs an User up into our System.
     *
     * @param request .
     *
     * @return .
     *
     * @throws Exception .
     */
    @Transactional
    public Optional<String> signUp(SignUpRequest request) throws Exception {
        // 1.- We validate the Request
        log.info("Attempting to validate: {}", request);
        signUpRequestValidator.validateSignUpRequest(request);

        // 2.- We create a Profile based off the request
        Profile newUserProfile = createUserProfile(request);
        log.info("New Profile has been created with Id: {}", newUserProfile.getProfileId());

        // 3.- We create an Account for this Profile
        Account newUserAccount = createUserAccount(newUserProfile, request);
        log.info("New Account has been created with Id: {}", newUserAccount.getAccountId());

        // 4.- We finally create the Identity/Entity
        Entity newEntity = createEntity(newUserProfile);
        log.info("New Entity has been created with Id: {}", newEntity.getEntityId());

        // 5.- We have created everything. Now let's just return the EntityId.
        return Optional.of(newEntity.getEntityId());
    }

    private Entity createEntity(Profile profile) {
        IdentificationEntity identificationEntity = IdentificationEntity.builder()
            .profileId(profile.getProfileId())
            .build();

        String entityId = identificationEntityDao.createIdentificationEntity(identificationEntity);

        return Entity.builder()
            .entityId(entityId)
            .profile(profile)
            .build();
    }

    private Account createUserAccount(Profile newProfile, SignUpRequest request) throws Exception {
        // 1.- Let's first hash the password.
        String hashedPassword = securePasswordUtils.createSecurePassword(request.getPassword());

        // 2.- Here we manage, create, and update the Account.
        ClassicAccountEntity classicAccountEntity = ClassicAccountEntity.builder()
            .email(newProfile.getEmailAddress())
            .password(hashedPassword)
            .accountType(AccountType.CLASSIC.getAccountType())
            .profileId(newProfile.getProfileId())
            .build();

        String accountId = accountDao.createBasicAccount(classicAccountEntity);

        return Account.builder()
            .accountId(accountId)
            .profile(newProfile)
            .accountType(AccountType.CLASSIC)
            .build();
    }

    private Profile createUserProfile(SignUpRequest request) {
        ProfileEntity profileEntity = ProfileEntity.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .emailAddress(request.getEmailAddress())
            .phoneNumber(request.getPhoneNumber())
            .build();

        String profileId = profileDao.createProfile(profileEntity);

        return Profile.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .emailAddress(request.getEmailAddress())
            .phoneNumber(request.getPhoneNumber())
            .profileId(profileId)
            .build();
    }
}
