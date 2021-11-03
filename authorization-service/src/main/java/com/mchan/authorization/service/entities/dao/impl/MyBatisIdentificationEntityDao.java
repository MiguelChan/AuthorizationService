package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.IdentificationEntityDao;
import com.mchan.authorization.service.entities.dao.entities.IdentificationEntity;
import com.mchan.authorization.service.entities.dao.mappers.EntitiesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * .
 */
@Component
public class MyBatisIdentificationEntityDao implements IdentificationEntityDao {

    private static final String ID_PREFIX = "";

    private final RandomIdGenerator randomIdGenerator;
    private final EntitiesMapper entitiesMapper;

    /**
     * .
     *
     * @param randomIdGenerator .
     *
     * @param entitiesMapper .
     */
    @Autowired
    public MyBatisIdentificationEntityDao(RandomIdGenerator randomIdGenerator,
                                          EntitiesMapper entitiesMapper) {
        this.randomIdGenerator = randomIdGenerator;
        this.entitiesMapper = entitiesMapper;
    }

    @Override
    public String createIdentificationEntity(IdentificationEntity identificationEntity) {
        String identifier = randomIdGenerator.generateRandomId(ID_PREFIX);

        IdentificationEntity entityToInsert = identificationEntity.toBuilder()
            .entityId(identifier)
            .build();

        entitiesMapper.createEntity(entityToInsert);

        return identifier;
    }
}
