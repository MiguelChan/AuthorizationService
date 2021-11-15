package com.mchan.authorization.service.entities.authentication.strategies.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.authentication.models.AuthenticationRequest;
import com.mchan.authorization.service.entities.authentication.models.ClassicAuthenticationRequest;
import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.SessionsDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import com.mchan.authorization.service.entities.utils.DateProvider;
import com.mchan.authorization.service.entities.utils.SecurePasswordUtils;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.exceptions.NotAuthorizedException;
import io.github.benas.randombeans.api.EnhancedRandom;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class ClassicAuthenticationStrategyTests {

    private static final Instant TEST_INSTANT = Instant.now();

    @Mock
    private AccountDao accountDao;
    @Mock
    private SecurePasswordUtils securePasswordUtils;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private SessionsDao sessionsDao;

    private ClassicAuthenticationStrategy strategy;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        strategy = new ClassicAuthenticationStrategy(
            accountDao,
            sessionsDao,
            dateProvider,
            securePasswordUtils);
    }

    @Test
    public void authenticateUser_should_authenticateTheUser() throws Exception {
        ClassicAuthenticationRequest request = EnhancedRandom.random(ClassicAuthenticationRequest.class);
        ClassicAccountEntity expectedAccountEntity = EnhancedRandom.random(ClassicAccountEntity.class);

        when(dateProvider.now()).thenReturn(TEST_INSTANT);
        when(accountDao.getAccountByEmail(request.getUsername())).thenReturn(expectedAccountEntity);
        when(securePasswordUtils.isValidPassword(request.getPassword(), expectedAccountEntity.getPassword())).thenReturn(true);

        strategy.authenticateUser(request);

        SessionEntity expectedSession = SessionEntity.builder()
            .accountId(expectedAccountEntity.getAccountId())
            .sessionType(expectedAccountEntity.getAccountType())
            .sessionTime(TEST_INSTANT)
            .build();
        verify(sessionsDao).createSession(expectedSession);
    }

    @Test
    public void authenticateUser_should_throwNotAuthorizedException_when_userNameDoesNotExist() throws Exception {
        ClassicAuthenticationRequest request = EnhancedRandom.random(ClassicAuthenticationRequest.class);

        when(accountDao.getAccountByEmail(request.getUsername())).thenReturn(null);

        assertThatThrownBy(() -> strategy.authenticateUser(request)).isInstanceOfAny(NotAuthorizedException.class);
    }

    @Test
    public void authenticateUser_should_throwNotAuthorizedException_when_passwordsDoNotMatch() throws Exception {
        ClassicAuthenticationRequest request = EnhancedRandom.random(ClassicAuthenticationRequest.class);
        ClassicAccountEntity expectedAccountEntity = EnhancedRandom.random(ClassicAccountEntity.class);

        when(accountDao.getAccountByEmail(request.getUsername())).thenReturn(expectedAccountEntity);
        when(securePasswordUtils.isValidPassword(request.getPassword(), expectedAccountEntity.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> strategy.authenticateUser(request)).isInstanceOfAny(NotAuthorizedException.class);
    }

    @Test
    public void authenticateUser_should_bubbleUpException_when_anErrorOccurs() throws Exception {
        ClassicAuthenticationRequest request = EnhancedRandom.random(ClassicAuthenticationRequest.class);
        ClassicAccountEntity expectedAccountEntity = EnhancedRandom.random(ClassicAccountEntity.class);

        when(accountDao.getAccountByEmail(request.getUsername())).thenReturn(expectedAccountEntity);
        when(securePasswordUtils.isValidPassword(request.getPassword(), expectedAccountEntity.getPassword())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> strategy.authenticateUser(request)).isInstanceOfAny(RuntimeException.class);
    }

    @Test
    public void authenticateUser_should_throwInvalidArgumentException_when_theProvidedRequestIsNotCorrectOne() {
        AuthenticationRequest request = new AuthenticationRequest() {
        };

        assertThatThrownBy(() -> strategy.authenticateUser(request)).isInstanceOfAny(InvalidArgumentException.class);
    }
}
