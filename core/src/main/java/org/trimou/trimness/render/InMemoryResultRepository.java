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
package org.trimou.trimness.render;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The default in memory result repository.
 *
 * @author Martin Kouba
 */
@Dependent
public class InMemoryResultRepository implements ResultRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResultRepository.class);

    @Inject
    private Vertx vertx;

    @Inject
    private IdGenerator idGenerator;

    private final ConcurrentMap<String, Result> results;

    private final ConcurrentMap<String, ResultLink> links;

    InMemoryResultRepository() {
        results = new ConcurrentHashMap<>();
        links = new ConcurrentHashMap<>();
    }

    @Override
    public Result get(String id) {
        return results.get(id);
    }

    @Override
    public ResultLink getLink(String linkId) {
        return links.get(linkId);
    }

    @Override
    public Result init(RenderRequest renderRequest) {
        Consumer<Result> onComplete = renderRequest.getLinkId() == null ? null : (r) -> {
            if (r.isSucess()) {
                links.put(renderRequest.getLinkId(), new SimpleResultLink(renderRequest.getLinkId(), r.getId()));
                LOGGER.info("Result link {0} updated to: {1}", renderRequest.getLinkId(), r.getId());
            }
        };
        SimpleResult result = SimpleResult.init("" + idGenerator.nextId(), renderRequest.getTemplate().getId(),
                renderRequest.getTemplate().getContentType(), onComplete);
        results.put(result.getId(), result);
        if (renderRequest.getTimeout() != null && renderRequest.getTimeout() > 0) {
            // Schedule result removal
            vertx.setTimer(renderRequest.getTimeout(), (id) -> {
                if (results.remove(result.getId()) != null) {
                    LOGGER.info("Result timed out [id: {0}]", result.getId());
                }
            });
        }
        LOGGER.info("Result initialized [id: {0}, template: {1}, timeout: {2}]", result.getId(),
                renderRequest.getTemplate().getId(), renderRequest.getTimeout());
        return result;
    }

    @Override
    public boolean remove(String id) {
        if (results.remove(id) != null) {
            LOGGER.info("Result removed [id: {0}]", id);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return results.size();
    }

    @Override
    public void clear() {
        results.clear();
        LOGGER.info("All results removed");
    }

}
