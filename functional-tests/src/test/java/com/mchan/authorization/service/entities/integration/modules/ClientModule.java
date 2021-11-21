package com.mchan.authorization.service.entities.integration.modules;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mchan.authorization.service.client.ApiClient;
import com.mchan.authorization.service.client.api.LogInControllerApi;
import com.mchan.authorization.service.client.api.SpringHealthControllerApi;
import com.mchan.authorization.service.client.api.SpringProfileControllerApi;
import com.mchan.authorization.service.client.api.SpringSignUpControllerApi;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.clients.impl.RetrofitEntitiesClient;
import com.mchan.authorization.service.entities.integration.clients.impl.SwaggerEntitiesServiceClient;
import java.time.LocalDateTime;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Guice module for connecting Clients.
 */
public class ClientModule extends AbstractModule {

    private static final String BASE_URL = "http://localhost:8094/auth-hub/";
    // private static final String BASE_URL = "https://mgl-auth-service-beta.herokuapp.com/auth-hub/";

    @Override
    protected void configure() {
        ApiClient apiClient = new ApiClient();

        bind(ApiClient.class).toInstance(apiClient);

        bind(EntitiesServiceClient.class).to(SwaggerEntitiesServiceClient.class);
    }

    /**
     * .
     *
     * @return .
     */
    @Provides
    @Singleton
    public RetrofitEntitiesClient providesRetrofitEntitiesClient() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build();

        return retrofit.create(RetrofitEntitiesClient.class);
    }

    /**
     * .
     *
     * @param apiClient .
     *
     * @return .
     */
    @Provides
    @Singleton
    public SpringHealthControllerApi providesHealthApi(ApiClient apiClient) {
        return apiClient.createService(SpringHealthControllerApi.class);
    }

    /**
     * .
     *
     * @param apiClient .
     *
     * @return .
     */
    @Provides
    @Singleton
    public SpringProfileControllerApi providesProfileApi(ApiClient apiClient) {
        return apiClient.createService(SpringProfileControllerApi.class);
    }

    /**
     * .
     *
     * @param apiClient .
     *
     * @return .
     */
    @Provides
    @Singleton
    public SpringSignUpControllerApi providesSignUpApi(ApiClient apiClient) {
        return apiClient.createService(SpringSignUpControllerApi.class);
    }

    /**
     * .
     *
     * @param apiClient .
     *
     * @return .
     */
    @Provides
    @Singleton
    public LogInControllerApi providesLogInControllerApi(ApiClient apiClient) {
        return apiClient.createService(LogInControllerApi.class);
    }
}
