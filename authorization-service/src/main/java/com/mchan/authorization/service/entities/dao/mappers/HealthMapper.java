package com.mchan.authorization.service.entities.dao.mappers;

import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface HealthMapper {

    /**
     * Whether the DB is healthy or not.
     *
     * @return .
     */
    boolean isHealthy();

}
