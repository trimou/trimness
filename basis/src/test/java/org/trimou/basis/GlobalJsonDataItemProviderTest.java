package org.trimou.basis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.basis.BasisConfigurationKey.GLOBAL_JSON_DATA_FILE;

import java.util.List;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.basis.DataItemProvider.DataItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 *
 * @author Martin Kouba
 */
public class GlobalJsonDataItemProviderTest {

    @Rule
    public WeldInitiator weld = WeldInitiator.of(GlobalJsonDataItemProvider.class, DummyBasisConfiguration.class);

    @Test
    public void testBasicOperations() {
        DummyBasisConfiguration configuration = weld.select(DummyBasisConfiguration.class).get();
        configuration.put(GLOBAL_JSON_DATA_FILE, "src/test/resources/global-data.json");

        GlobalJsonDataItemProvider provider = weld.select(GlobalJsonDataItemProvider.class).get();

        List<DataItem> data = provider.getData("whatever");

        assertNotNull(data);
        assertEquals(2, data.size());
        assertEquals("foo", data.get(0).getName());
        assertEquals("array", data.get(1).getName());

        Object fooData = data.get(0).getValue();

        assertTrue(fooData instanceof JsonElement);
        JsonElement fooElement = (JsonElement) fooData;
        assertEquals("bar", fooElement.getAsString());

        Object arrayData = data.get(1).getValue();

        assertTrue(arrayData instanceof JsonArray);
        JsonArray arrayElement = (JsonArray) arrayData;
        assertEquals(3, arrayElement.size());
        assertEquals(1, arrayElement.get(0).getAsInt());
    }

}
