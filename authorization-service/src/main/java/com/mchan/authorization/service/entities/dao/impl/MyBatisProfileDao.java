package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.dao.mappers.ProfilesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The MyBatis Profile DAO.
 */
@Component
public class MyBatisProfileDao implements ProfileDao {

    private static final String ID_PREFIX = "pr";

    private final RandomIdGenerator randomIdGenerator;
    private final ProfilesMapper profilesMapper;

    @Autowired
    public MyBatisProfileDao(RandomIdGenerator randomIdGenerator,
                             ProfilesMapper profilesMapper) {
        this.randomIdGenerator = randomIdGenerator;
        this.profilesMapper = profilesMapper;
    }

    @Override
    public String createProfile(ProfileEntity profileEntity) {
        String newProfileId = randomIdGenerator.generateRandomId(ID_PREFIX);

        ProfileEntity profileToCreate = profileEntity.toBuilder()
            .profileId(newProfileId)
            .build();

        profilesMapper.createProfile(profileToCreate);

        return newProfileId;
    }

}
