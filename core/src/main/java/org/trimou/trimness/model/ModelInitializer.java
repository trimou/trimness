/*
 * Copyright 2017 Trimness team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trimou.trimness.model;

import static org.trimou.trimness.util.Strings.MODEL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.trimou.trimness.config.Configuration;
import org.trimou.trimness.config.TrimnessKey;
import org.trimou.trimness.render.RenderRequest;
import org.trimou.util.ImmutableList;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class ModelInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelInitializer.class.getName());

    private final List<ModelProvider> providers;

    private final long timeout;

    // Make it proxyable
    ModelInitializer() {
        providers = null;
        timeout = 0l;
    }

    @Inject
    public ModelInitializer(Instance<ModelProvider> modelProviders, Configuration configuration) {
        List<ModelProvider> providers = new ArrayList<>();
        for (ModelProvider provider : modelProviders) {
            if (provider.isValid()) {
                providers.add(provider);
            }
        }
        this.providers = ImmutableList.copyOf(providers);
        this.timeout = configuration.getLongValue(TrimnessKey.MODEL_INIT_TIMEOUT);
    }

    /**
     *
     * @param renderRequest
     * @param requestModel
     * @return the initialized model
     */
    public Map<String, Object> initModel(RenderRequest renderRequest, Object requestModel) {

        Map<String, Object> model = new HashMap<>();
        model.put(MODEL, requestModel != null ? requestModel : Collections.emptyMap());

        if (providers.isEmpty()) {
            return model;
        }

        List<SimpleModelRequest> requests = new ArrayList<>(providers.size());
        CountDownLatch latch = new CountDownLatch(providers.size());

        for (ModelProvider provider : providers) {
            SimpleModelRequest request = new SimpleModelRequest(provider.getNamespace(), renderRequest, latch);
            requests.add(request);
            try {
                provider.handle(request);
            } catch (RuntimeException e) {
                LOGGER.warn("Provider [{0}] failed to handle model request for template [{1}]", provider.getNamespace(),
                        renderRequest.getTemplate().getId());
                LOGGER.debug("ModelProvider failure: ", e);
            }
        }

        try {
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) {
                LOGGER.warn(
                        "Timeout expired - model not initialized completely for {0}: {1} from {2} providers completed within {3} ms ",
                        renderRequest.getTemplate().getId(), providers.size() - latch.getCount(), providers.size(),
                        timeout);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        for (SimpleModelRequest request : requests) {
            Object result = request.getResult();
            if (result != null) {
                model.put(request.getNamespace(), result);
            }
        }
        return model;
    }

}
