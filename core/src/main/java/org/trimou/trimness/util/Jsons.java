package org.trimou.trimness.util;

import java.io.Reader;

import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

/**
 * To avoid service provider lookup for every {@link Jsons} static method
 * invocation.
 *
 * @author Martin Kouba
 */
public final class Jsons {

    public static final javax.json.spi.JsonProvider INSTANCE = javax.json.spi.JsonProvider.provider();

    public static JsonObjectBuilder objectBuilder() {
        return INSTANCE.createObjectBuilder();
    }

    public static JsonArrayBuilder arrayBuilder() {
        return INSTANCE.createArrayBuilder();
    }

    public static JsonReader reader(Reader reader) {
        return INSTANCE.createReader(reader);
    }

}
