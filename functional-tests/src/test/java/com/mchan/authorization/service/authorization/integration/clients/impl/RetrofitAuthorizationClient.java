package com.mchan.authorization.service.authorization.integration.clients.impl;

import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

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

    /**
     * .
     *
     * @param applicationId .
     *
     * @param request .
     *
     * @return .
     */
    @PUT("applications/{applicationId}")
    Call<UpdateApplicationResponse> updateApplication(@Path("applicationId") int applicationId, @Body UpdateApplicationRequest request);

    /**
     * .
     *
     * @param applicationId .
     *
     * @return .
     */
    @DELETE("applications/{applicationId}")
    Call<DeactivateApplicationResponse> deactivateApplication(@Path("applicationId") int applicationId);

}
