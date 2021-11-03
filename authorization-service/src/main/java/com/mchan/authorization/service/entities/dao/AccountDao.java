package com.mchan.authorization.service.entities.dao;

import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;

/**
 * Defines the interface for inserting/creating Accounts.
 */
public interface AccountDao {

    /**
     * Creates a {@link ClassicAccountEntity} that will be later retrieved in order to validate
     * a given account.
     *
     * @param classicAccountEntity .
     *
     * @return The Id of the newly created Account.
     */
    String createBasicAccount(ClassicAccountEntity classicAccountEntity);

}
