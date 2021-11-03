package com.mchan.authorization.service.entities.dao.mappers;

import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
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

}
