package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.dao.mappers.AccountsMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
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

    private MyBatisAccountDao accountsDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        accountsDao = new MyBatisAccountDao(randomIdGenerator, accountsMapper);
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
}
