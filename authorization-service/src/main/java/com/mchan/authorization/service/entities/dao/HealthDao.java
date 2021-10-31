package com.mchan.authorization.service.entities.dao;

/**
 * Defines a simple DAO for accessing Health Data from the Data Storage.
 */
public interface HealthDao {

    /**
     * Returns true when the DB Connection is Healthy, false otherwise.
     *
     * @return .
     */
    boolean isHealthy();

}
