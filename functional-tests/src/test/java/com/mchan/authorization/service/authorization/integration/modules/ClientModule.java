package com.mchan.authorization.service.authorization.integration.modules;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mchan.authorization.service.authorization.integration.clients.AuthorizationServiceClient;
import com.mchan.authorization.service.authorization.integration.clients.impl.RetrofitAuthorizationClient;
import com.mchan.authorization.service.authorization.integration.clients.impl.RetrofitAuthorizationServiceClient;
import java.time.LocalDateTime;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * .
 */
public class ClientModule extends AbstractModule {

    private static final String BASE_URL = "http://localhost:8094/auth-hub/";

    @Override
    protected void configure() {
        bind(AuthorizationServiceClient.class).to(RetrofitAuthorizationServiceClient.class);
    }

    /**
     * .
     *
     * @return .
     */
    @Provides
    @Singleton
    public RetrofitAuthorizationClient providesRetrofitAuthorizationClient() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());

        Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
            .build();

        return retrofit.create(RetrofitAuthorizationClient.class);
    }
}
