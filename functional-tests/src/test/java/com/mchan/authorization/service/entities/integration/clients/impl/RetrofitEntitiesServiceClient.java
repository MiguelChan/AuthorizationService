package com.mchan.authorization.service.entities.integration.clients.impl;

import com.google.gson.Gson;
import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileRequest;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import com.mchan.authorization.service.entities.integration.utils.BadRequestResponse;
import java.net.HttpURLConnection;
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

    @Override
    public GetProfileResponse getProfile(GetProfileRequest reuqest) throws Exception {
        Response<GetProfileResponse> response = retrofitEntitiesClient.getProfile(reuqest.getProfileId())
            .execute();

        if (response.body() != null) {
            return response.body();
        }

        if (response.code() == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new ClassNotFoundException("Not Found");
        }

        throw new InternalError(response.errorBody().string());
    }

    @Override
    public EditProfileResponse editProfile(String profileId, EditProfileRequest request) throws Exception {
        return makeCall(retrofitEntitiesClient.editProfile(profileId, request));
    }

    private <T> T makeCall(Call<T> callMethod) throws Exception {
        Response<T> callResponse = callMethod.execute();
        return callResponse.body();
    }
}
