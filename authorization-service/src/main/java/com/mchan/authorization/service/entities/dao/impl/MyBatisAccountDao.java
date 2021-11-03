package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.mappers.AccountsMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * .
 */
@Component
public class MyBatisAccountDao implements AccountDao {

    private static final String ACCOUNT_PREFIX = "cact";

    private final RandomIdGenerator randomIdGenerator;
    private final AccountsMapper accountsMapper;

    /**
     * .
     *
     * @param randomIdGenerator .
     *
     * @param accountsMapper .
     */
    @Autowired
    public MyBatisAccountDao(RandomIdGenerator randomIdGenerator,
                             AccountsMapper accountsMapper) {
        this.randomIdGenerator = randomIdGenerator;
        this.accountsMapper = accountsMapper;
    }

    @Override
    public String createBasicAccount(ClassicAccountEntity classicAccountEntity) {
        String newAccountId = randomIdGenerator.generateRandomId(ACCOUNT_PREFIX);

        ClassicAccountEntity entityToCreate = classicAccountEntity.toBuilder()
            .accountId(newAccountId)
            .build();

        accountsMapper.createBasicAccount(entityToCreate);

        return newAccountId;
    }
}
