package org.trimou.trimness.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.trimou.trimness.config.TrimnessKey.GLOBAL_JSON_FILE;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonStructure;
import javax.json.JsonValue.ValueType;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.trimness.DummyConfiguration;

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

        assertNotNull(dummyModelRequest.getResult());
        JsonStructure model = (JsonStructure) dummyModelRequest.getResult();

        assertEquals(ValueType.OBJECT, model.getValueType());
        JsonObject modelObject = (JsonObject) model;
        assertEquals(2, modelObject.size());

        assertEquals("bar", modelObject.getString("foo"));

        JsonArray array = modelObject.getJsonArray("array");
        assertEquals(3, array.size());
        assertEquals(1, array.getInt(0));
    }

}
