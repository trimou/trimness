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
package org.trimou.trimness.template;

import org.trimou.trimness.template.TemplateProvider.Change;

public class ImmutableChange implements Change {

    private final String providerId;

    private final String templateId;

    public ImmutableChange(String providerId, String templateId) {
        this.providerId = providerId;
        this.templateId = templateId;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String getTemplateId() {
        return templateId;
    }

}
