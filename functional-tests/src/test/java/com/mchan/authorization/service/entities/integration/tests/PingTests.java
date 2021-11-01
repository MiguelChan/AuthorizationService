package com.mchan.authorization.service.entities.integration.tests;

import static org.testng.AssertJUnit.assertTrue;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import org.testng.annotations.Test;

/**
 * Tests for ping Operations.
 */
@Test
public class PingTests extends BaseTests {

    @Test
    public void ping_should_returnTrue() throws Exception {
        PingRequest pingRequest = PingRequest.builder().build();

        PingResponse pingResponse = this.serviceClient.ping(pingRequest);

        assertTrue(pingResponse.isHealthy());
    }

    @Test
    public void deepPing_should_returnTrue() throws Exception {
        PingRequest pingRequest = PingRequest.builder().build();

        PingResponse pingResponse = this.serviceClient.deepPing(pingRequest);

        assertTrue(pingResponse.isHealthy());
    }

}
