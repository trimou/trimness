package org.trimou.trimness.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;

import java.util.Map;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.DummyConfiguration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 *
 * @author Martin Kouba
 */
public class GlobalJsonModelProviderTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(GlobalJsonModelProvider.class, DummyConfiguration.class);

    @Test
    public void testBasicOperations() {
        DummyConfiguration configuration = weld.select(DummyConfiguration.class).get();
        configuration.put(GLOBAL_JSON_FILE, "src/test/resources/global-data.json");

        DummyModelRequest dummyModelRequest = new DummyModelRequest();

        GlobalJsonModelProvider provider = weld.select(GlobalJsonModelProvider.class).get();

        provider.handle(dummyModelRequest);

        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) dummyModelRequest.getResult();

        assertNotNull(model);
        assertEquals(2, model.size());

        Object fooData = model.get("foo");

        assertTrue(fooData instanceof JsonElement);
        JsonElement fooElement = (JsonElement) fooData;
        assertEquals("bar", fooElement.getAsString());

        Object arrayData = model.get("array");

        assertTrue(arrayData instanceof JsonArray);
        JsonArray arrayElement = (JsonArray) arrayData;
        assertEquals(3, arrayElement.size());
        assertEquals(1, arrayElement.get(0).getAsInt());
    }

}