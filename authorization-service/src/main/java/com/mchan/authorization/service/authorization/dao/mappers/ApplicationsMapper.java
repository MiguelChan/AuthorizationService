package com.mchan.authorization.service.authorization.dao.mappers;

import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface ApplicationsMapper {

    /**
     * .
     *
     * @param applicationEntity .
     */
    void createApplication(ApplicationEntity applicationEntity);

}
