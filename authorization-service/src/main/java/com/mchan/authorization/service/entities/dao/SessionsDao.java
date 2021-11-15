package com.mchan.authorization.service.entities.dao;

import com.mchan.authorization.service.entities.dao.entities.SessionEntity;

/**
 * A DAO that handles the Sessions.
 */
public interface SessionsDao {

    /**
     * Creates a Session in the Storage Layer.
     *
     * @param sessionEntity .
     */
    void createSession(SessionEntity sessionEntity);

}
