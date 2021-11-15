package com.mchan.authorization.service.entities.authentication.strategies.impl;

import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.models.ClassicAuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.strategies.AuthenticationStrategy;
import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.SessionsDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import com.mchan.authorization.service.entities.utils.DateProvider;
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
    private final SessionsDao sessionsDao;
    private final DateProvider dateProvider;
    private final SecurePasswordUtils securePasswordUtils;

    /**
     * .
     *
     * @param accountDao .
     *
     * @param sessionsDao .
     *
     * @param dateProvider .
     *
     * @param securePasswordUtils .
     */
    @Autowired
    public ClassicAuthenticationStrategy(AccountDao accountDao,
                                         SessionsDao sessionsDao,
                                         DateProvider dateProvider,
                                         SecurePasswordUtils securePasswordUtils) {
        this.accountDao = accountDao;
        this.securePasswordUtils = securePasswordUtils;
        this.sessionsDao = sessionsDao;
        this.dateProvider = dateProvider;
    }

    @Override
    public void authenticateUser(AuthenticationRequest authRequest) {
        ClassicAuthenticationRequest request = getRequest(authRequest);
        String username = request.getUsername();
        String password = request.getPassword();

        ClassicAccountEntity accountEntity = accountDao.getAccountByEmail(username);
        if (accountEntity == null) {
            throw new NotAuthorizedException("Invalid Username or Password");
        }

        String encryptedPassword = accountEntity.getPassword();

        try {
            if (!securePasswordUtils.isValidPassword(password, encryptedPassword)) {
                throw new NotAuthorizedException("Invalid Username or Password");
            }
        } catch (NotAuthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }

        createSession(accountEntity);
    }

    private void createSession(ClassicAccountEntity classicAccountEntity) {
        SessionEntity sessionEntity = SessionEntity.builder()
            .accountId(classicAccountEntity.getAccountId())
            .sessionType(classicAccountEntity.getAccountType())
            .sessionTime(dateProvider.now())
            .build();

        sessionsDao.createSession(sessionEntity);
    }

    private ClassicAuthenticationRequest getRequest(AuthenticationRequest request) {
        if (request instanceof ClassicAuthenticationRequest) {
            return (ClassicAuthenticationRequest) request;
        }
        throw new InvalidArgumentException("The provided request is invalid");
    }
}
