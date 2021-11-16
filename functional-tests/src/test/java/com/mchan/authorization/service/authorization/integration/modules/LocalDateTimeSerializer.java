package com.mchan.authorization.service.authorization.integration.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * .
 */
class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * .
     *
     * @param src .
     * @param typeOfSrc .
     * @param context .
     * @return .
     */
    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc,
                                 JsonSerializationContext context) {
        return new JsonPrimitive(formatter.format(src));
    }
}

