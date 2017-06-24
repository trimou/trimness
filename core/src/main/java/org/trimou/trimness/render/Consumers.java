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
import javax.json.JsonObject;

import org.jboss.weld.vertx.VertxConsumer;
import org.jboss.weld.vertx.VertxEvent;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.Strings;

/**
 * Consumes messages sent over Vert.x event bus.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class Consumers {

    public static final String ADDR_RENDER = "org.trimou.trimness.render";

    public static final String ADDR_RESULT = "org.trimou.trimness.result";

    public static final String ADDR_RESULT_REMOVE = "org.trimou.trimness.result.remove";

    public static final String ADDR_RESULT_LINK = "org.trimou.trimness.result.link";

    @Inject
    private RenderLogic renderLogic;

    @Inject
    private ResultLogic resultLogic;

    void render(@Observes @VertxConsumer(ADDR_RENDER) VertxEvent event) {
        renderLogic.render(event.getMessageBody().toString(), (result, contentType) -> event.setReply(result),
                (code, message) -> event.fail(code, message));
    }

    void getResult(@Observes @VertxConsumer(ADDR_RESULT) VertxEvent event) {
        JsonObject input = getMessageBody(event);
        resultLogic.get(input.getString(Strings.RESULT_ID, null), input.getString(Strings.RESULT_TYPE, null),
                (result, contentType) -> event.setReply(result), (code, message) -> event.fail(code, message));
    }

    void removeResult(@Observes @VertxConsumer(ADDR_RESULT_REMOVE) VertxEvent event) {
        JsonObject input = getMessageBody(event);
        resultLogic.remove(input.getString(Strings.RESULT_ID, null), (result, contentType) -> event.setReply(result),
                (code, message) -> event.fail(code, message));
    }

    void getLink(@Observes @VertxConsumer(ADDR_RESULT_LINK) VertxEvent event) {
        JsonObject input = getMessageBody(event);
        resultLogic.getLink(input.getString(Strings.LINK_ID, null),
                link -> resultLogic.get(link.getResultId(), input.getString(Strings.RESULT_TYPE, null),
                        (result, contentType) -> event.setReply(result), (code, message) -> event.fail(code, message)),
                (code, message) -> event.fail(code, message));
    }

    private JsonObject getMessageBody(VertxEvent event) {
        JsonObject input = Jsons.asJsonObject(event.getMessageBody().toString());
        if (input == null) {
            event.fail(Codes.CODE_INVALID_INPUT, "The message body must be a JSON object");
        }
        return input;
    }

}
