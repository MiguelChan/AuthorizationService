package com.mchan.authorization.service.authorization.integration.modules;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * .
 */
class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * .
     *
     * @param json .
     * @param typeOfT .
     * @param context .
     * @return .
     * @throws JsonParseException .
     */
    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {
        String timeString = json.getAsString();
        return LocalDateTime.parse(timeString, formatter);
    }
}

