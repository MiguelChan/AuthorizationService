package com.mchan.authorization.service.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.mchan.authorization.service.exceptions.DatabaseException;
import com.mchan.authorization.service.exceptions.DuplicateEntityException;
import com.mchan.authorization.service.utils.MyBatisExceptionsTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

/**
 * .
 */
public class MyBatisExceptionsTranslatorTests {

    private MyBatisExceptionsTranslator translator;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        translator = new MyBatisExceptionsTranslator();
    }

    @Test
    public void getException_should_returnCorrectTypeOfException() {
        Exception exception = mock(DuplicateKeyException.class);

        Exception theException = translator.getException(exception);

        assertThat(theException).isInstanceOfAny(DuplicateEntityException.class);
    }

    @Test
    public void getException_should_returnDatabaseException_when_exceptionIsUnknown() {
        Exception exception = new RuntimeException("fixture-failure");

        Exception theException = translator.getException(exception);

        assertThat(theException).isInstanceOfAny(DatabaseException.class);
    }

}
