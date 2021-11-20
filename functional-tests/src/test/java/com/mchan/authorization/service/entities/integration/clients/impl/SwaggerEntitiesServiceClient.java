package com.mchan.authorization.service.entities.integration.clients.impl;

import com.google.gson.Gson;
import com.mchan.authorization.lib.dtos.EditProfileRequest;
import com.mchan.authorization.lib.dtos.EditProfileResponse;
import com.mchan.authorization.lib.dtos.GetProfileRequest;
import com.mchan.authorization.lib.dtos.GetProfileResponse;
import com.mchan.authorization.lib.dtos.LogInRequest;
import com.mchan.authorization.lib.dtos.LogInResponse;
import com.mchan.authorization.lib.dtos.PingRequest;
import com.mchan.authorization.lib.dtos.PingResponse;
import com.mchan.authorization.lib.dtos.SignUpRequest;
import com.mchan.authorization.lib.dtos.SignUpResponse;
import com.mchan.authorization.lib.models.Profile;
import com.mchan.authorization.service.client.api.LogInControllerApi;
import com.mchan.authorization.service.client.api.SpringHealthControllerApi;
import com.mchan.authorization.service.client.api.SpringProfileControllerApi;
import com.mchan.authorization.service.client.api.SpringSignUpControllerApi;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.utils.BadRequestException;
import com.mchan.authorization.service.entities.integration.utils.BadRequestResponse;
import com.sun.jdi.InternalException;
import java.net.HttpURLConnection;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.AuthenticationException;
import retrofit2.Call;
import retrofit2.Response;

/**
 * .
 */
@Singleton
public class SwaggerEntitiesServiceClient implements EntitiesServiceClient {

    private final SpringHealthControllerApi healthApi;
    private final SpringProfileControllerApi profileApi;
    private final SpringSignUpControllerApi signUpApi;
    private final LogInControllerApi logInControllerApi;
    private final Gson gson;

    /**
     * .
     *
     * @param healthApi .
     *
     * @param profileApi .
     *
     * @param signUpApi .
     *
     * @param logInControllerApi .
     *
     * @param gson .
     */
    @Inject
    public SwaggerEntitiesServiceClient(SpringHealthControllerApi healthApi,
                                        SpringProfileControllerApi profileApi,
                                        SpringSignUpControllerApi signUpApi,
                                        LogInControllerApi logInControllerApi,
                                        Gson gson) {
        this.healthApi = healthApi;
        this.profileApi = profileApi;
        this.signUpApi = signUpApi;
        this.logInControllerApi = logInControllerApi;
        this.gson = gson;
    }

    @Override
    public PingResponse ping(PingRequest request) throws Exception {
        com.mchan.authorization.service.client.model.PingResponse apiResponse =
            healthApi.pingUsingGET().execute().body();

        return PingResponse.builder()
            .healthy(apiResponse.isHealthy())
            .message(apiResponse.getMessage())
            .build();
    }

    @Override
    public PingResponse deepPing(PingRequest request) throws Exception {
        com.mchan.authorization.service.client.model.PingResponse apiResponse =
            healthApi.deepPingUsingGET().execute().body();

        return PingResponse.builder()
            .healthy(apiResponse.isHealthy())
            .message(apiResponse.getMessage())
            .build();
    }

    @Override
    public SignUpResponse signUp(SignUpRequest request) throws Exception {
        com.mchan.authorization.service.client.model.SignUpRequest signUpRequest =
            new com.mchan.authorization.service.client.model.SignUpRequest();

        signUpRequest.setFirstName(request.getFirstName());
        signUpRequest.setLastName(request.getLastName());
        signUpRequest.setPhoneNumber(request.getPhoneNumber());
        signUpRequest.setEmailAddress(request.getEmailAddress());
        signUpRequest.setPassword(request.getPassword());

        Call<com.mchan.authorization.service.client.model.SignUpResponse> responseCall =
            signUpApi.signUpUsingPOST(signUpRequest);

        Response<com.mchan.authorization.service.client.model.SignUpResponse> responseObject =
            responseCall.execute();

        if (responseObject.isSuccessful()) {
            com.mchan.authorization.service.client.model.SignUpResponse response = responseObject.body();
            return SignUpResponse.builder()
                .profileId(response.getProfileId())
                .message(response.getMessage())
                .build();
        }

        if (responseObject.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
            throwBadRequestException(responseObject);
        }

        throw new InternalError("????");
    }

    @Override
    public GetProfileResponse getProfile(GetProfileRequest reuqest) throws Exception {
        Call<com.mchan.authorization.service.client.model.GetProfileResponse> responseCall =
            profileApi.getProfileUsingGET();
        Response<com.mchan.authorization.service.client.model.GetProfileResponse> responseObject =
            responseCall.execute();

        if (responseObject.code() == HttpURLConnection.HTTP_UNAUTHORIZED
            || responseObject.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new AuthenticationException("");
        }

        if (responseObject.isSuccessful()) {
            com.mchan.authorization.service.client.model.GetProfileResponse response =
                responseObject.body();

            Profile profile = Profile.builder()
                .profileId(response.getProfile().getProfileId())
                .firstName(response.getProfile().getFirstName())
                .lastName(response.getProfile().getLastName())
                .phoneNumber(response.getProfile().getPhoneNumber())
                .emailAddress(response.getProfile().getEmailAddress())
                .profileId(response.getProfile().getProfileId())
                .build();

            return GetProfileResponse.builder()
                .profile(profile)
                .message(response.getMessage())
                .build();
        }

        throw new InternalException("???");
    }

    @Override
    public EditProfileResponse editProfile(String profileId, EditProfileRequest request)
        throws Exception {
        return null;
    }

    @Override
    public LogInResponse logIn(LogInRequest logInRequest) throws Exception {
        String username = logInRequest.getUserName();
        String password = logInRequest.getPassword();

        com.mchan.authorization.service.client.model.LogInRequest req = new com.mchan.authorization.service.client.model.LogInRequest();
        req.setPassword(password);
        req.setUserName(username);

        Call<Void> call = logInControllerApi.loginUsingPOST(req);
        Response<Void> responseObject = call.execute();

        return null;
    }



    private <T> void throwBadRequestException(Response<T> response) throws RuntimeException {
        BadRequestResponse badRequestResponse =
            gson.fromJson(response.errorBody().charStream(), BadRequestResponse.class);
        throw new BadRequestException(badRequestResponse);
    }
}
