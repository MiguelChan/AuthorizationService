package com.mchan.authorization.service.entities.dao.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mchan.authorization.service.entities.dao.entities.IdentificationEntity;
import com.mchan.authorization.service.entities.dao.mappers.EntitiesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
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
public class MyBatisIdentificationEntityDaoTests {

    @Mock
    private EntitiesMapper entitiesMapper;
    @Mock
    private RandomIdGenerator randomIdGenerator;

    private MyBatisIdentificationEntityDao entitiesDao;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        entitiesDao = new MyBatisIdentificationEntityDao(randomIdGenerator, entitiesMapper);
    }

    @Test
    public void createIdentificationEntity_should_createTheEntity() {
        String expectedEntityId = "ProfileId";
        IdentificationEntity entity = EnhancedRandom.random(IdentificationEntity.class, "entityId");
        IdentificationEntity expectedAccountEntity = entity.toBuilder()
            .entityId(expectedEntityId)
            .build();

        when(randomIdGenerator.generateRandomId(any())).thenReturn(expectedEntityId);

        String entityId = entitiesDao.createIdentificationEntity(entity);

        assertThat(entityId).isEqualTo(expectedEntityId);
        verify(entitiesMapper).createEntity(eq(expectedAccountEntity));
    }
}
