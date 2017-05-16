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
package org.trimou.trimness.example.simple;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.json.JsonString;

import org.trimou.trimness.render.ResultLinkDefinition;
import org.trimou.trimness.render.SimpleResultLinkDefinition;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class AdditionalResultLinks {

    @Produces
    ResultLinkDefinition configureWeldResultLink() {
        return new SimpleResultLinkDefinition("weld-core", (r) -> {
            // Update the link for charts template if repo is weld/core
            return r.getTemplate().getId().contains("charts") && r.getParameter(GithubModelProvider.REPOSITORY)
                    .map((repo) -> (repo instanceof JsonString) ? ((JsonString) repo).getString().equals("weld/core") : false).get();
        });
    }

}
