package com.mchan.authorization.service.entities.dao.impl;

import com.mchan.authorization.service.entities.dao.ProfileDao;
import com.mchan.authorization.service.entities.dao.entities.ProfileEntity;
import com.mchan.authorization.service.entities.dao.mappers.ProfilesMapper;
import com.mchan.authorization.service.entities.utils.RandomIdGenerator;
import com.mchan.authorization.service.exceptions.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The MyBatis Profile DAO.
 */
@Log4j2
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

    @Override
    public ProfileEntity getProfile(String profileId) {
        ProfileEntity foundProfile = profilesMapper.getProfile(profileId);
        if (foundProfile == null) {
            String errorMessage = String.format("Profile with Id: %s does not exist", profileId);
            log.error(errorMessage);
            throw new EntityNotFoundException(errorMessage);
        }
        return foundProfile;
    }

}
