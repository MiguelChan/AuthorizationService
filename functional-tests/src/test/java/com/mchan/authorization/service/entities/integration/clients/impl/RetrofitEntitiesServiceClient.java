package com.mchan.authorization.service.entities.integration.clients.impl;

import com.google.gson.Gson;
import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import com.mchan.authorization.service.entities.integration.utils.BadRequestResponse;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Defines the ServiceClient. Defaults to the Retrofit Implementation.
 */
@Singleton
public class RetrofitEntitiesServiceClient implements EntitiesServiceClient {

    private final RetrofitEntitiesClient retrofitEntitiesClient;
    private final Gson gson;

    /**
     * .
     *
     * @param retrofitEntitiesClient .
     */
    @Inject
    public RetrofitEntitiesServiceClient(RetrofitEntitiesClient retrofitEntitiesClient, Gson gson) {
        this.retrofitEntitiesClient = retrofitEntitiesClient;
        this.gson = gson;
    }

    @Override
    public PingResponse ping(PingRequest request) throws Exception {
        return makeCall(retrofitEntitiesClient.ping());
    }

    @Override
    public PingResponse deepPing(PingRequest request) throws Exception {
        return makeCall(retrofitEntitiesClient.deepPing());
    }

    @Override
    public SignUpResponse signUp(SignUpRequest request) throws Exception {
        Response<SignUpResponse> response = retrofitEntitiesClient.signUp(request).execute();

        if (response.body() != null) {
            return response.body();
        }

        BadRequestResponse badRequestResponse =
            gson.fromJson(response.errorBody().charStream(), BadRequestResponse.class);
        throw new BadRequestException(badRequestResponse);
    }

    private <T> T makeCall(Call<T> callMethod) throws Exception {
        Response<T> callResponse = callMethod.execute();
        return callResponse.body();
    }
}
