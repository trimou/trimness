package org.trimou.trimness.util;

import static org.trimou.trimness.util.Strings.CONTENT_TYPE;
import static org.trimou.trimness.util.Strings.ID;
import static org.trimou.trimness.util.Strings.MSG;
import static org.trimou.trimness.util.Strings.RESULT;
import static org.trimou.trimness.util.Strings.RESULT_ID;
import static org.trimou.trimness.util.Strings.STATUS;
import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;
import static org.trimou.trimness.util.Strings.TIMEOUT;
import static org.trimou.trimness.util.Strings.VALUE;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.trimou.trimness.render.RenderRequest;
import org.trimou.trimness.render.Result;
import org.trimou.trimness.template.Template;

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

    public static JsonArrayBuilder arrayBuilder(String... values) {
        JsonArrayBuilder builder = arrayBuilder();
        for (String value : values) {
            builder.add(value);
        }
        return builder;
    }


    public static Long getLong(JsonObject json, String name, Long defaultValue) {
        JsonValue number = json.get(name);
        if (number != null && number.getValueType().equals(ValueType.NUMBER)) {
            return ((JsonNumber) number).longValue();
        }
        return defaultValue;
    }

    public static JsonObject asJsonObject(String body) {
        JsonStructure input = asJson(body);
        if (input != null && ValueType.OBJECT.equals(input.getValueType())) {
            return (JsonObject) input;
        }
        return null;
    }

    /**
     *
     * @param body
     * @return the parsed JSON
     * @throws JsonParsingException
     */
    public static JsonStructure asJson(String body) {
        return Jsons.reader(new StringReader(body)).read();
    }

    public static JsonObjectBuilder empty() {
        return message(null);
    }

    public static JsonObjectBuilder message(String msg, Object... params) {
        JsonObjectBuilder response = Jsons.objectBuilder();
        // Server time
        response.add(TIME, LocalDateTime.now().toString());
        if (msg != null) {
            response.add(MSG, params.length > 0 ? String.format(msg, params) : msg);
        }
        return response;
    }

    public static String asyncResult(String resultId, RenderRequest renderRequest) {
        JsonObjectBuilder metadata = empty();
        metadata.add(RESULT_ID, resultId);
        metadata.add(TIMEOUT,
                LocalDateTime.ofInstant(Instant.ofEpochMilli(renderRequest.getTime() + renderRequest.getTimeout()),
                        ZoneId.systemDefault()).toString());
        return metadata.build().toString();
    }

    public static String metadataResult(Template template, String output) {
        JsonObjectBuilder metadata = Jsons.empty();
        metadata.add(VALUE, output);
        metadata.add(TEMPLATE_ID, template.getId());
        if (template.getContentType() != null) {
            metadata.add(CONTENT_TYPE, template.getContentType());
        }
        return metadata.build().toString();
    }

    public static String metadataResult(Result result) {
        JsonObjectBuilder builder = Jsons.objectBuilder();
        builder.add(Strings.TEMPLATE_ID, result.getTemplateId());
        builder.add(ID, result.getId());
        builder.add(STATUS, result.getStatus().toString());
        if (result.isFailure()) {
            builder.add(Strings.ERROR, result.getValue());
        } else {
            builder.add(Strings.VALUE, result.getValue());
            if (result.getContentType() != null) {
                builder.add(Strings.CONTENT_TYPE, result.getContentType());
            }
        }
        return Jsons.empty().add(RESULT, builder).build().toString();
    }

}
