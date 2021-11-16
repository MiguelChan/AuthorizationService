package com.mchan.authorization.service.authorization.integration.tests;

import com.mchan.authorization.service.authorization.integration.clients.AuthorizationServiceClient;
import com.mchan.authorization.service.authorization.integration.modules.ClientModule;
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
    protected AuthorizationServiceClient serviceClient;

}
