package org.travelbot.java.support.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.typesafe.config.ConfigValue;

public class ConfigValueSerializer extends StdSerializer<ConfigValue> {

    public ConfigValueSerializer(Class<ConfigValue> t) {
        super(t);
    }

    public ConfigValueSerializer() {
        this(null);
    }

    private static final long serialVersionUID = 1906797360517736007L;

    @Override
    public void serialize(ConfigValue value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeObject(value.unwrapped());
    }
}
