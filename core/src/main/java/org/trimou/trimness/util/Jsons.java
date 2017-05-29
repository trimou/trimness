package org.trimou.trimness.util;

import java.io.Reader;
import java.io.Writer;

import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.JsonWriter;

/**
 * To avoid service provider lookup for every {@link Jsons} static method
 * invocation.
 *
 * @author Martin Kouba
 */
public final class Jsons {

    public static final javax.json.spi.JsonProvider INSTANCE = javax.json.spi.JsonProvider.provider();

    public static final JsonObject EMPTY_OBJECT = objectBuilder().build();

    public static JsonObjectBuilder objectBuilder() {
        return INSTANCE.createObjectBuilder();
    }

    public static JsonArrayBuilder arrayBuilder() {
        return INSTANCE.createArrayBuilder();
    }

    public static JsonReader reader(Reader reader) {
        return INSTANCE.createReader(reader);
    }

    public static JsonWriter writer(Writer writer) {
        return INSTANCE.createWriter(writer);
    }

    public static Long getLong(JsonObject json, String name, Long defaultValue) {
        JsonValue number = json.get(name);
        if (number != null && number.getValueType().equals(ValueType.NUMBER)) {
            return ((JsonNumber) number).longValue();
        }
        return defaultValue;
    }

}
