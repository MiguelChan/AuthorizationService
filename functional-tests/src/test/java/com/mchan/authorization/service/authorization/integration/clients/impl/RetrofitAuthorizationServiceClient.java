package com.mchan.authorization.service.authorization.integration.clients.impl;

import com.google.gson.Gson;
import com.mchan.authorization.lib.dtos.CreateApplicationRequest;
import com.mchan.authorization.lib.dtos.CreateApplicationResponse;
import com.mchan.authorization.lib.dtos.DeactivateApplicationRequest;
import com.mchan.authorization.lib.dtos.DeactivateApplicationResponse;
import com.mchan.authorization.lib.dtos.UpdateApplicationRequest;
import com.mchan.authorization.lib.dtos.UpdateApplicationResponse;
import com.mchan.authorization.service.authorization.integration.clients.AuthorizationServiceClient;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import com.mchan.authorization.service.entities.integration.utils.BadRequestResponse;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import retrofit2.Response;

/**
 * .
 */
public class RetrofitAuthorizationServiceClient implements AuthorizationServiceClient {

    private final RetrofitAuthorizationClient authClient;
    private final Gson gson;

    /**
     * .
     *
     * @param authClient .
     */
    @Inject
    public RetrofitAuthorizationServiceClient(RetrofitAuthorizationClient authClient,
                                              Gson gson) {
        this.authClient = authClient;
        this.gson = gson;
    }

    @Override
    public CreateApplicationResponse createApplication(CreateApplicationRequest request) throws Exception {
        Response<CreateApplicationResponse> responseCall = authClient.createApplication(request).execute();
        if (responseCall.isSuccessful()) {
            return responseCall.body();
        }

        if (responseCall.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            BadRequestResponse badRequestResponse =
                gson.fromJson(responseCall.errorBody().charStream(), BadRequestResponse.class);
            throw new BadRequestException(badRequestResponse);
        }

        throw new InternalError(responseCall.errorBody().string());
    }

    @Override
    public UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) throws Exception {
        Response<UpdateApplicationResponse> responseCall = authClient.updateApplication(request.getApplicationId(), request)
            .execute();

        if (responseCall.isSuccessful()) {
            return responseCall.body();
        }

        if (responseCall.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            BadRequestResponse badRequestResponse =
                gson.fromJson(responseCall.errorBody().charStream(), BadRequestResponse.class);
            throw new BadRequestException(badRequestResponse);
        }

        throw new InternalError(responseCall.errorBody().string());
    }

    @Override
    public DeactivateApplicationResponse deactivateApplication(DeactivateApplicationRequest request) throws Exception {
        Response<DeactivateApplicationResponse> responseCall = authClient.deactivateApplication(request.getApplicationId()).execute();

        if (responseCall.isSuccessful()) {
            return responseCall.body();
        }

        if (responseCall.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new ClassNotFoundException("Not Found");
        }

        if (responseCall.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            BadRequestResponse badRequestResponse =
                gson.fromJson(responseCall.errorBody().charStream(), BadRequestResponse.class);
            throw new BadRequestException(badRequestResponse);
        }

        throw new InternalError(responseCall.errorBody().string());
    }
}
