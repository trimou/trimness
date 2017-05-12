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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.trimness.template.Template;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class InMemoryResultRepository implements ResultRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryResultRepository.class);

    @Inject
    private Vertx vertx;

    @Inject
    private IdGenerator idGenerator;

    private ConcurrentMap<String, SimpleResult> results;

    @PostConstruct
    void init() {
        results = new ConcurrentHashMap<>();
    }

    @Override
    public SimpleResult get(String id) {
        return results.get(id);
    }

    @Override
    public Result init(Template template, long timeout) {
        SimpleResult result = SimpleResult.init("" + idGenerator.nextId(), template.getId(), template.getContentType());
        results.put(result.getId(), result);
        if (timeout != 0) {
            // Schedule result removal
            vertx.setTimer(timeout, (id) -> {
                if (results.remove(result.getId()) != null) {
                    LOGGER.info("Result timed out [id: {0}]", result.getId());
                }
            });
        }
        LOGGER.info("Result initialized [id: {0}, template: {1}, timeout: {2}]", result.getId(), template.getId(),
                timeout);
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
