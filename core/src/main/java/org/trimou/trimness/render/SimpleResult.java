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

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *
 * @author Martin Kouba
 */
public class SimpleResult implements Result {

    static SimpleResult init(String id, String templateId, String contentType, Consumer<Result> onComplete) {
        return new SimpleResult(id, templateId, Status.INCOMPLETE, null, null, contentType, onComplete);
    }

    private final String id;

    private final String templateId;

    private final String contentType;

    private final LocalDateTime createdAt;

    private final AtomicReference<Status> status;

    private final AtomicReference<String> errorMessage;

    private final AtomicReference<String> output;

    private final Consumer<Result> onComplete;

    /**
     *
     * @param id
     * @param code
     * @param errorMessage
     * @param output
     * @param templateId
     * @param contentType
     * @param onComplete
     */
    SimpleResult(String id, String templateId, Status code, String errorMessage, String output, String contentType,
            Consumer<Result> onComplete) {
        this.id = id;
        this.status = new AtomicReference<>(code);
        this.errorMessage = new AtomicReference<>();
        this.output = new AtomicReference<>();
        this.templateId = templateId;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
        this.onComplete = onComplete;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status.get();
    }

    public String getError() {
        return errorMessage.get();
    }

    public String getOutput() {
        return output.get();
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public void fail(String errorMessage) {
        synchronized (this.status) {
            checkIsIncomplete();
            this.status.set(Status.FAILURE);
            this.errorMessage.set(errorMessage);
            onComplete();
        }
    }

    @Override
    public void complete(String output) {
        synchronized (this.status) {
            checkIsIncomplete();
            this.status.set(Status.SUCESS);
            this.output.set(output);
            onComplete();
        }
    }

    private void checkIsIncomplete() {
        if (isComplete()) {
            throw new IllegalStateException("Result " + getId() + " already completed!");
        }
    }

    private void onComplete() {
        if (onComplete != null) {
            onComplete.accept(this);
        }
    }

}
