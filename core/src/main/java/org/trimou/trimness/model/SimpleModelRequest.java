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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.trimou.trimness.template.Template;

/**
 *
 * @author Martin Kouba
 */
class SimpleModelRequest implements ModelRequest {

    private final String namespace;

    private final Template template;

    private final Map<String, Object> parameters;

    private final AtomicReference<Object> result;

    private final CountDownLatch latch;

    /**
     *
     * @param namespace
     * @param template
     * @param parameters
     * @param latch
     */
    SimpleModelRequest(String namespace, Template template, Map<String, Object> parameters, CountDownLatch latch) {
        this.namespace = namespace;
        this.template = template;
        this.parameters = parameters;
        this.result = new AtomicReference<Object>();
        this.latch = latch;
    }

    @Override
    public Template getTemplate() {
        return template;
    }

    @Override
    public Optional<Object> getParameter(String name) {
        return Optional.ofNullable(parameters.get(name));
    }

    @Override
    public void setResult(Object value) {
        if (result.compareAndSet(null, value)) {
            latch.countDown();
        } else {
            throw new IllegalStateException("Result already set");
        }
    }

    Object getResult() {
        return result.get();
    }

    String getNamespace() {
        return namespace;
    }

}
