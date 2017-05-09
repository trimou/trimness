package org.trimou.trimness.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Rule;
import org.junit.Test;
import org.trimou.engine.resolver.Mapper;
import org.trimou.trimness.DummyConfiguration;
import org.trimou.trimness.config.Key;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.util.Strings;

/**
 *
 * @author Martin Kouba
 */
public class MetadataModelProviderTest {

    private static final long TIMEOUT = 100l;

    @Rule
    public WeldInitiator weld = WeldInitiator.of(MetadataModelProvider.class, DummyConfiguration.class);

    @Test
    public void testInit() {
        DummyConfiguration configuration = weld.select(DummyConfiguration.class).get();
        configuration.put(TrimnessKey.MODEL_INIT_TIMEOUT, TIMEOUT);
        configuration.put(TrimnessKey.HOST, TrimnessKey.HOST.getDefaultValue());

        DummyModelRequest dummyModelRequest = new DummyModelRequest();
        MetadataModelProvider provider = weld.select(MetadataModelProvider.class).get();
        provider.handle(dummyModelRequest);

        Object result = dummyModelRequest.getResult();
        assertNotNull(result);
        assertTrue(result instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) result;
        assertTrue(map.containsKey(Strings.CONFIG));
        Mapper configMapper = (Mapper) map.get(Strings.CONFIG);
        assertEquals(TIMEOUT, configMapper.get(TrimnessKey.MODEL_INIT_TIMEOUT.getEnvKey()));
        assertEquals(TIMEOUT, configMapper.get(TrimnessKey.MODEL_INIT_TIMEOUT.get()));
        assertEquals(TIMEOUT,
                configMapper.get(TrimnessKey.MODEL_INIT_TIMEOUT.get().substring(Key.KEY_PREFIX.length())));
        assertEquals("localhost", configMapper.get("host"));

    }

}
