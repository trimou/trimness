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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.trimou.engine.priority.Priorities;
import org.trimou.trimness.util.CompositeComponent;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Delegates to a valid repository with the highest priority.
 *
 * @author Martin Kouba
 */
@Typed(DelegateResultRepository.class)
@ApplicationScoped
public class DelegateResultRepository extends CompositeComponent<ResultRepository> implements ResultRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegateResultRepository.class.getName());

    // Make it proxyable
    DelegateResultRepository() {
    }

    @Inject
    public DelegateResultRepository(Instance<ResultRepository> repositories) {
        super(repositories, Priorities.higherFirst());
        if (components.isEmpty()) {
            throw new IllegalStateException("No result repository found");
        }
        LOGGER.info("Using result repository: {0}", components.get(0).getId());
    }

    @Override
    public Result get(String resultId) {
        return first().get(resultId);
    }

    @Override
    public ResultLink getLink(String linkId) {
        return first().getLink(linkId);
    }

    @Override
    public Result init(RenderRequest renderRequest) {
        return first().init(renderRequest);
    }

    @Override
    public boolean remove(String resultId) {
        return first().remove(resultId);
    }

    @Override
    public int size() {
        return first().size();
    }

    @Override
    public void clear() {
        first().clear();
    }

}
