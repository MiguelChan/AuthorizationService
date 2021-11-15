package com.mchan.authorization.service.entities.dao.mappers;

import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * .
 */
@Mapper
public interface SessionsMapper {

    /**
     * .
     *
     * @param sessionEntity .
     */
    void createSession(SessionEntity sessionEntity);

}
