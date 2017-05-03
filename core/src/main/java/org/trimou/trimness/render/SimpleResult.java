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

/**
 *
 * @author Martin Kouba
 */
public class SimpleResult implements Result {

    static SimpleResult init(String id, String templateId, String contentType) {
        return new SimpleResult(id, templateId, Code.INCOMPLETE, null, null, contentType);
    }

    private final String id;

    private final String templateId;

    private final String contentType;

    private final LocalDateTime createdAt;

    private final AtomicReference<Code> code;

    private final AtomicReference<String> errorMessage;

    private final AtomicReference<String> output;

    /**
     *
     * @param id
     * @param code
     * @param errorMessage
     * @param output
     * @param templateId
     * @param contentType
     */
    SimpleResult(String id, String templateId, Code code, String errorMessage, String output, String contentType) {
        this.id = id;
        this.code = new AtomicReference<>(code);
        this.errorMessage = new AtomicReference<>();
        this.output = new AtomicReference<>();
        this.templateId = templateId;
        this.contentType = contentType;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public Code getCode() {
        return code.get();
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
        synchronized (this.code) {
            checkIsIncomplete();
            this.code.set(Code.FAILURE);
            this.errorMessage.set(errorMessage);
        }
    }

    @Override
    public void complete(String output) {
        synchronized (this.code) {
            checkIsIncomplete();
            this.code.set(Code.SUCESS);
            this.output.set(output);
        }
    }

    private void checkIsIncomplete() {
        if (isComplete()) {
            throw new IllegalStateException("Result " + getId() + " already completed!");
        }
    }

}
