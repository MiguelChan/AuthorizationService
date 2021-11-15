package com.mchan.authorization.service.entities.utils;

import com.mchan.authorization.service.exceptions.DatabaseException;
import com.mchan.authorization.service.exceptions.DuplicateEntityException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/**
 * Translates MyBatis exceptions into the Application's Runtime Exception.
 */
@Component
public class MyBatisExceptionsTranslator {

    /**
     * Gets the Exception.
     *
     * @param exceptionFromDao .
     *
     * @return .
     */
    public RuntimeException getException(Exception exceptionFromDao) {
        if (exceptionFromDao instanceof DuplicateKeyException) {
            return new DuplicateEntityException(exceptionFromDao.getMessage(), exceptionFromDao);
        }
        return new DatabaseException(exceptionFromDao.getMessage(), exceptionFromDao);
    }

}
