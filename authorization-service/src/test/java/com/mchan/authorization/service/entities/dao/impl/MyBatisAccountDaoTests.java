package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.mappers.AccountsMapper;
import com.mchan.authorization.service.entities.utils.MyBatisExceptionsTranslator;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import com.mchan.authorization.service.exceptions.DatabaseException;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class MyBatisAccountDaoTests {

    @Mock
    private AccountsMapper accountsMapper;
    @Mock
    private RandomIdGenerator randomIdGenerator;
    @Mock
    private MyBatisExceptionsTranslator translator;

    private MyBatisAccountDao accountsDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        accountsDao = new MyBatisAccountDao(randomIdGenerator, accountsMapper, translator);
    }

    @Test
    public void createBasicAccount_should_createTheAccount() {
        String expectedAccountId = "ProfileId";
        ClassicAccountEntity accountEntity = EnhancedRandom.random(ClassicAccountEntity.class, "accountId");
        ClassicAccountEntity expectedAccountEntity = accountEntity.toBuilder()
            .profileId(expectedAccountId)
            .build();

        when(randomIdGenerator.generateRandomId(any())).thenReturn(expectedAccountId);

        String accountId = accountsDao.createBasicAccount(accountEntity);

        assertThat(accountId).isEqualTo(expectedAccountId);
        verify(accountsMapper).createBasicAccount(eq(expectedAccountEntity));
    }

    @Test
    public void getAccountByEmail_should_returnTheAccount() {
        String accountEmail = "some@some.com";
        ClassicAccountEntity expectedAccount = EnhancedRandom.random(ClassicAccountEntity.class);

        when(accountsMapper.getAccountByEmail(accountEmail)).thenReturn(expectedAccount);

        ClassicAccountEntity account = accountsDao.getAccountByEmail(accountEmail);

        assertThat(account).isEqualTo(expectedAccount);
    }

    @Test
    public void createBasicAccount_should_bubbleUpException() {
        String expectedAccountId = "ProfileId";
        ClassicAccountEntity accountEntity = EnhancedRandom.random(ClassicAccountEntity.class, "accountId");

        when(randomIdGenerator.generateRandomId(any())).thenReturn(expectedAccountId);
        doThrow(RuntimeException.class).when(accountsMapper).createBasicAccount(any());
        when(translator.getException(any())).thenReturn(new DatabaseException("SomeSome"));

        assertThatThrownBy(() -> accountsDao.createBasicAccount(accountEntity))
            .isInstanceOfAny(DatabaseException.class);
    }
}
