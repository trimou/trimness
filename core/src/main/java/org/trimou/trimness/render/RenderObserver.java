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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.vertx.VertxConsumer;
import org.jboss.weld.vertx.VertxEvent;

/**
 * Consumes messages sent over Vert.x event bus.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class RenderObserver {

    public static final String ADDR_RENDER = "org.trimou.trimness.render";

    @Inject
    private Renderer renderer;

    /**
     * The message body must be a JSON string.
     *
     * @param event
     */
    void handleRenderEvent(@Observes @VertxConsumer(ADDR_RENDER) VertxEvent event) {
        renderer.render(event.getMessageBody().toString(), (result, contentType) -> event.setReply(result),
                (code, message) -> event.fail(code, message));
    }

}
