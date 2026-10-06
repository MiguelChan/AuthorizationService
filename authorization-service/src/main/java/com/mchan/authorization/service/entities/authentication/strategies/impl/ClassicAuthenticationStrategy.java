package com.mchan.authorization.service.entities.authentication.strategies.impl;

import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.models.ClassicAuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import com.mchan.authorization.service.entities.components.GetProfileComponent;
import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.exceptions.NotAuthorizedException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The Classic AuthenticationStrategy.
 * Used for Classic log-in (Username, Password).
 */
@Log4j2
@Component("ClassicAuthenticationStrategy")
public class ClassicAuthenticationStrategy implements AuthenticationStrategy {

    private final AccountDao accountDao;
    private final GetProfileComponent getProfileComponent;
    private final SecurePasswordUtils securePasswordUtils;
    private final String missingAccountHash;

    /**
     * .
     *
     * @param accountDao .
     *
     * @param securePasswordUtils .
     */
    @Autowired
    public ClassicAuthenticationStrategy(AccountDao accountDao,
                                         GetProfileComponent getProfileComponent,
                                         SecurePasswordUtils securePasswordUtils) {
        this.accountDao = accountDao;
        this.securePasswordUtils = securePasswordUtils;
        try {
            this.missingAccountHash = securePasswordUtils.createSecurePassword("internal-missing-account-verifier");
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize authentication verifier", e);
        }
        this.getProfileComponent = getProfileComponent;
    }

    @Override
    public Profile authenticateUser(AuthenticationRequest authRequest) {
        ClassicAuthenticationRequest request = getRequest(authRequest);
        String username = request.getUsername();
        String password = request.getPassword();

        ClassicAccountEntity accountEntity = accountDao.getAccountByEmail(username);
        String encryptedPassword = accountEntity == null ? missingAccountHash : accountEntity.getPassword();

        try {
            if (!securePasswordUtils.isValidPassword(password, encryptedPassword) || accountEntity == null) {
                throw new NotAuthorizedException("Invalid Username or Password");
            }
        } catch (NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        return getProfileComponent.getProfile(accountEntity.getProfileId());
    }

    private ClassicAuthenticationRequest getRequest(AuthenticationRequest request) {
        if (request instanceof ClassicAuthenticationRequest) {
            return (ClassicAuthenticationRequest) request;
        }
        throw new InvalidArgumentException("The provided request is invalid");
    }
}
