package com.mchan.authorization.service.entities.integration.clients.impl;

import com.mchan.authorization.lib.dtos.PingResponse;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Defines the underlying Retrofit Client for the EntitiesService.
 */
public interface RetrofitEntitiesClient {

    /**
     * .
     *
     * @return .
     */
    @GET("ping")
    Call<PingResponse> ping();

    /**
     * .
     *
     * @return .
     */
    @GET("deep_ping")
    Call<PingResponse> deepPing();

}
