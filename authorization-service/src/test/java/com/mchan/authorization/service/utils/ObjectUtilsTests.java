package com.mchan.authorization.service.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class ObjectUtilsTests {

    private ObjectUtils objectUtils;

    @BeforeEach
    public void setup() {
        objectUtils = new ObjectUtils();
    }

    @Test
    public void firstNonNull_should_returnLeftObject_when_itIsNotNull() {
        Object leftObject = new Object();
        Object rightObject = new Object();

        Object nonNullObject = objectUtils.firstNonNull(leftObject, rightObject);

        assertThat(nonNullObject).isNotNull();
        assertThat(nonNullObject).isEqualTo(leftObject);
    }

    @Test
    public void firstNonNull_should_returnRightObject_when_leftObjectIsNull() {
        Object leftObject = null;
        Object rightObject = new Object();

        Object nonNullObject = objectUtils.firstNonNull(leftObject, rightObject);

        assertThat(nonNullObject).isNotNull();
        assertThat(nonNullObject).isEqualTo(rightObject);
    }

}
