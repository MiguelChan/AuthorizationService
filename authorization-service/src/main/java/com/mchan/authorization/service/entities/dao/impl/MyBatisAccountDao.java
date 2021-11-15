package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.mappers.AccountsMapper;
import com.mchan.authorization.service.entities.utils.MyBatisExceptionsTranslator;
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
    private final MyBatisExceptionsTranslator exceptionsTranslator;

    /**
     * .
     *
     * @param randomIdGenerator .
     *
     * @param accountsMapper .
     */
    @Autowired
    public MyBatisAccountDao(RandomIdGenerator randomIdGenerator,
                             AccountsMapper accountsMapper,
                             MyBatisExceptionsTranslator myBatisExceptionsTranslator) {
        this.randomIdGenerator = randomIdGenerator;
        this.accountsMapper = accountsMapper;
        this.exceptionsTranslator = myBatisExceptionsTranslator;
    }

    @Override
    public String createBasicAccount(ClassicAccountEntity classicAccountEntity) {
        String newAccountId = randomIdGenerator.generateRandomId(ACCOUNT_PREFIX);

        ClassicAccountEntity entityToCreate = classicAccountEntity.toBuilder()
            .accountId(newAccountId)
            .build();

        try {
            accountsMapper.createBasicAccount(entityToCreate);
        } catch (Exception e) {
            throw exceptionsTranslator.getException(e);
        }

        return newAccountId;
    }

    @Override
    public ClassicAccountEntity getAccountByEmail(String email) {
        return accountsMapper.getAccountByEmail(email);
    }

}
