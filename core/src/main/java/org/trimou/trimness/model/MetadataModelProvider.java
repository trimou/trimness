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

import static org.trimou.trimness.util.Strings.TEMPLATE_ID;
import static org.trimou.trimness.util.Strings.TIME;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;

import org.trimou.util.ImmutableMap;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class MetadataModelProvider implements ModelProvider {

    public static final String NAMESPACE = "meta";

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public void handle(ModelRequest request) {
        request.setResult(ImmutableMap.<String, Object>builder().put(TIME, LocalDateTime.now())
                .put(TEMPLATE_ID, request.getTemplate().getId()).build());
    }

}
