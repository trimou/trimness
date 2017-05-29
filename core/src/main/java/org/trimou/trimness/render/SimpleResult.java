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

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *
 * @author Martin Kouba
 */
public class SimpleResult implements Result {

    static SimpleResult init(String id, String templateId, String contentType, Consumer<Result> onComplete) {
        return new SimpleResult(id, System.currentTimeMillis(), null, templateId, contentType, Status.INCOMPLETE, null,
                onComplete);
    }

    private final String id;

    private final Long created;

    private final AtomicReference<Long> completed;

    private final String templateId;

    private final String contentType;

    private final AtomicReference<Status> status;

    private final AtomicReference<String> value;

    private final Consumer<Result> onComplete;

    /**
     *
     * @param id
     * @param created
     * @param completed
     * @param templateId
     * @param contentType
     * @param status
     * @param value
     * @param onComplete
     */
    SimpleResult(String id, Long created, Long completed, String templateId, String contentType, Status status,
            String value, Consumer<Result> onComplete) {
        this.id = id;
        this.created = created;
        this.completed = new AtomicReference<Long>(completed);
        this.status = new AtomicReference<>(status);
        this.value = new AtomicReference<>(value);
        this.templateId = templateId;
        this.contentType = contentType;
        this.onComplete = onComplete;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Long getCreated() {
        return created;
    }

    @Override
    public Long getCompleted() {
        return completed.get();
    }

    @Override
    public Status getStatus() {
        return status.get();
    }

    @Override
    public String getValue() {
        return value.get();
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void fail(String errorMessage) {
        synchronized (this.status) {
            complete(Status.FAILURE, errorMessage);
        }
    }

    @Override
    public void complete(String output) {
        synchronized (this.status) {
            complete(Status.SUCESS, output);
        }
    }

    private void complete(Status status, String value) {
        if (isComplete()) {
            throw new IllegalStateException("Result " + getId() + " already completed!");
        }
        this.status.set(status);
        this.completed.set(System.currentTimeMillis());
        this.value.set(value);
        if (onComplete != null) {
            onComplete.accept(this);
        }
    }

}
