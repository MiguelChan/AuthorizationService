package com.mchan.authorization.service.authorization.components;

import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.utils.MyBatisExceptionsTranslator;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * The component for deleting an {@link com.mchan.authorization.lib.models.Application}.
 */
@Log4j2
@Component
public class DeleteApplicationComponent {

    private final ApplicationDao applicationDao;
    private final MyBatisExceptionsTranslator exceptionsTranslator;

    /**
     * .
     *
     * @param applicationDao .
     *
     * @param exceptionsTranslator .
     */
    @Autowired
    public DeleteApplicationComponent(ApplicationDao applicationDao,
                                      MyBatisExceptionsTranslator exceptionsTranslator) {
        this.applicationDao = applicationDao;
        this.exceptionsTranslator = exceptionsTranslator;
    }

    /**
     * .
     *
     * @param applicationId .
     */
    @Transactional
    public void deleteApplication(int applicationId) {
        validateApplication(applicationId);
        try {
            applicationDao.deactivateApplication(applicationId);
        } catch (Exception e) {
            throw exceptionsTranslator.getException(e);
        }
    }

    private void validateApplication(int applicationId) {
        ApplicationEntity applicationEntity = applicationDao.getApplication(applicationId);
        if (applicationEntity == null) {
            String errorMessage =
                String.format("Application with Id: '%d' does not exist", applicationId);
            log.error(errorMessage);
            throw new EntityNotFoundException(errorMessage);
        }

        if (!applicationEntity.isActive()) {
            String errorMessage = String.format("Application with Id: '%d' is already deactivated", applicationId);
            log.error(errorMessage);
            throw new InvalidArgumentException(errorMessage);
        }
    }

}
