package com.flowoa.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    private static final long JS_SAFE_INTEGER_MAX = 9007199254740991L;

    /**
     * Serialize Long values as strings to avoid JavaScript precision loss.
     * Only Long values exceeding JavaScript's safe integer range are serialized as strings;
     * smaller values remain numeric to avoid breaking existing API consumers.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            JsonSerializer<Long> longSerializer = new JsonSerializer<Long>() {
                @Override
                public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else if (value > JS_SAFE_INTEGER_MAX || value < -JS_SAFE_INTEGER_MAX) {
                        gen.writeString(value.toString());
                    } else {
                        gen.writeNumber(value);
                    }
                }
            };
            builder.serializerByType(Long.class, longSerializer);
            builder.serializerByType(Long.TYPE, longSerializer);
        };
    }
}
