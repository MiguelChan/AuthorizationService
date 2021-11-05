package com.mchan.authorization.service.entities.integration.clients.impl;

import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    @POST("sign-up")
    Call<SignUpResponse> signUp(@Body SignUpRequest request);

    /**
     * .
     *
     * @param profileId .
     *
     * @return .
     */
    @GET("auth/profile/{profileId}")
    Call<GetProfileResponse> getProfile(@Path("profileId") String profileId);

}
