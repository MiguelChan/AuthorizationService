package com.mchan.authorization.service.entities.integration.clients.impl;

import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
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

    /**
     * .
     *
     * @param retrofitEntitiesClient .
     */
    @Inject
    public RetrofitEntitiesServiceClient(RetrofitEntitiesClient retrofitEntitiesClient) {
        this.retrofitEntitiesClient = retrofitEntitiesClient;
    }

    @Override
    public PingResponse ping(PingRequest request) throws Exception {
        return makeCall(retrofitEntitiesClient.ping());
    }

    @Override
    public PingResponse deepPing(PingRequest request) throws Exception {
        return makeCall(retrofitEntitiesClient.deepPing());
    }

    private <T> T makeCall(Call<T> callMethod) throws Exception {
        Response<T> callResponse = callMethod.execute();
        return callResponse.body();
    }
}
