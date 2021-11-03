package com.mchan.authorization.service.entities.dao.mappers;

import com.mchan.authorization.service.entities.dao.entities.IdentificationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface EntitiesMapper {

    /**
     * .
     *
     * @param identificationEntity .
     */
    void createEntity(IdentificationEntity identificationEntity);
}
