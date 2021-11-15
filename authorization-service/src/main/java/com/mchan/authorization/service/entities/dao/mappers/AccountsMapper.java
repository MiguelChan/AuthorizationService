package com.mchan.authorization.service.entities.dao.mappers;

import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import java.sql.Timestamp;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface AccountsMapper {

    /**
     * .
     *
     * @param classicAccountEntity .
     */
    void createBasicAccount(ClassicAccountEntity classicAccountEntity);

    /**
     * .
     *
     * @param email .
     *
     * @return .
     */
    ClassicAccountEntity getAccountByEmail(String email);

    /**
     * Logs the current User's session.
     *
     * @param userName The username being logged in.
     *
     * @param logInTime The time of the event.
     */
    void logUserEntry(String userName, Instant logInTime);

}
