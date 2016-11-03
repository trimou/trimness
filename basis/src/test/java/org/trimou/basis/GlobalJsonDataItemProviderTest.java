package org.trimou.basis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.trimou.basis.BasisConfigurationKey.GLOBAL_JSON_DATA_FILE;

import java.util.List;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.trimou.basis.DataItemProvider.DataItem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 *
 * @author Martin Kouba
 */
public class GlobalJsonDataItemProviderTest {

    @Test
    public void testBasicOperations() {
        try (WeldContainer container = new Weld().disableDiscovery()
                .beanClasses(GlobalJsonDataItemProvider.class,
                        DummyBasisConfiguration.class)
                .initialize()) {

            DummyBasisConfiguration configuration = container
                    .select(DummyBasisConfiguration.class).get();
            configuration.put(GLOBAL_JSON_DATA_FILE,
                    "src/test/resources/global-data.json");

            GlobalJsonDataItemProvider provider = container
                    .select(GlobalJsonDataItemProvider.class).get();

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

}
