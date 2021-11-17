package com.mchan.authorization.service.authorization.components;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.authorization.dao.ApplicationDao;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import com.mchan.authorization.service.exceptions.InvalidArgumentException;
import com.mchan.authorization.service.utils.MyBatisExceptionsTranslator;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * .
 */
@ExtendWith(MockitoExtension.class)
public class DeleteApplicationComponentTests {

    @Mock
    private ApplicationDao applicationDao;
    @Mock
    private MyBatisExceptionsTranslator exceptionsTranslator;

    private DeleteApplicationComponent component;

    @BeforeEach
    public void setup() {
        component = new DeleteApplicationComponent(applicationDao, exceptionsTranslator);
    }

    @Test
    public void deleteApplication_should_deleteTheApplication() {
        int expectedAppId = 12345;

        ApplicationEntity dbApp = EnhancedRandom.random(ApplicationEntity.class);
        dbApp.setActive(true);

        when(applicationDao.getApplication(expectedAppId)).thenReturn(dbApp);

        component.deleteApplication(expectedAppId);

        verify(applicationDao).deactivateApplication(expectedAppId);
    }

    @Test
    public void deleteApplication_should_throwEntityNotFoundException_when_theApplicationCantBeFound() {
        int expectedAppId = 12345;

        when(applicationDao.getApplication(expectedAppId)).thenReturn(null);

        assertThatThrownBy(() -> component.deleteApplication(expectedAppId)).isInstanceOfAny(EntityNotFoundException.class);
    }

    @Test
    public void deleteApplication_should_throwInvalidArgumentException_when_theApplicationIsNotActive() {
        int expectedAppId = 12345;

        ApplicationEntity dbApp = EnhancedRandom.random(ApplicationEntity.class);
        dbApp.setActive(false);

        when(applicationDao.getApplication(expectedAppId)).thenReturn(dbApp);

        assertThatThrownBy(() -> component.deleteApplication(expectedAppId)).isInstanceOfAny(InvalidArgumentException.class);
    }

    @Test
    public void deleteApplication_should_rethrowExceptions_thrownByTheDatabase() {
        int expectedAppId = 12345;

        ApplicationEntity dbApp = EnhancedRandom.random(ApplicationEntity.class);
        dbApp.setActive(true);

        RuntimeException expectedException = new RuntimeException("SomeSoe");

        when(applicationDao.getApplication(expectedAppId)).thenReturn(dbApp);
        doThrow(expectedException).when(applicationDao).deactivateApplication(expectedAppId);
        when(exceptionsTranslator.getException(expectedException)).thenReturn(expectedException);

        assertThatThrownBy(() -> component.deleteApplication(expectedAppId)).isEqualTo(expectedException);
    }

}
