package com.mchan.authorization.service.authorization.integration.clients.impl;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * .
 */
public interface RetrofitAuthorizationClient {

    /**
     * .
     *
     * @param request .
     *
     * @return .
     */
    @POST("applications")
    Call<CreateApplicationResponse> createApplication(@Body CreateApplicationRequest request);

}
