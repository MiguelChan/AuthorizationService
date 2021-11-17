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

    /**
     * .
     *
     * @param applicationId .
     */
    int deactivateApplication(int applicationId);

    /**
     * .
     *
     * @param applicationId .
     *
     * @return .
     */
    ApplicationEntity getApplication(int applicationId);

    /**
     * .
     *
     * @param application .
     *
     * @return .
     */
    int updateApplication(ApplicationEntity application);

}
