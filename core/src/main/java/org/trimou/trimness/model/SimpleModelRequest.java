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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.trimou.trimness.render.RenderRequest;

/**
 *
 * @author Martin Kouba
 */
class SimpleModelRequest implements ModelRequest {

    private final String namespace;

    private final RenderRequest renderRequest;

    private final AtomicReference<Object> result;

    private final CountDownLatch latch;

    /**
     *
     * @param namespace
     * @param renderRequest
     * @param latch
     */
    SimpleModelRequest(String namespace, RenderRequest renderRequest, CountDownLatch latch) {
        this.namespace = namespace;
        this.renderRequest = renderRequest;
        this.result = new AtomicReference<Object>();
        this.latch = latch;
    }

    @Override
    public RenderRequest getRenderRequest() {
        return renderRequest;
    }

    @Override
    public void complete(Object value) {
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
