package com.mchan.authorization.service.entities.integration.modules;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.mchan.authorization.service.entities.integration.clients.EntitiesServiceClient;
import com.mchan.authorization.service.entities.integration.clients.impl.RetrofitEntitiesClient;
import com.mchan.authorization.service.entities.integration.clients.impl.RetrofitEntitiesServiceClient;
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
        bind(EntitiesServiceClient.class).to(RetrofitEntitiesServiceClient.class);
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
}
