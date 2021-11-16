package com.mchan.authorization.service.authorization.mappers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.mchan.authorization.lib.models.Application;
import com.mchan.authorization.service.authorization.dao.entities.ApplicationEntity;
import io.github.benas.randombeans.api.EnhancedRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * .
 */
public class ApplicationMapperTests {

    private ApplicationMapper appMapper;

    /**
     * .
     */
    @BeforeEach
    public void setup() {
        appMapper = new ApplicationMapper();
    }

    @Test
    public void fromApplication_should_returnAppEntity() {
        Application app = EnhancedRandom.random(Application.class);

        ApplicationEntity appEntity = appMapper.fromApplication(app);

        assertThat(appEntity.getApplicationId()).isEqualTo(app.getApplicationId());
        assertThat(appEntity.getAppName()).isEqualTo(app.getAppName());
        assertThat(appEntity.getAppIcon()).isEqualTo(app.getAppIcon());
        assertThat(appEntity.getAppHomePage()).isEqualTo(app.getAppHomePage());
        assertThat(appEntity.getShortDescription()).isEqualTo(app.getShortDescription());
    }

}
