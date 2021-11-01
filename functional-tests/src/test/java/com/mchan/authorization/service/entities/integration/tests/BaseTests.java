package com.mchan.authorization.service.entities.integration.tests;

import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.modules.ClientModule;
import javax.inject.Inject;
import org.testng.annotations.Guice;

/**
 * Base Test Class.
 */
@Guice(modules = {
    ClientModule.class,
})
public class BaseTests {

    @Inject
    protected EntitiesServiceClient serviceClient;

}
