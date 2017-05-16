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
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.trimou.trimness.util.CompositeComponent;
import org.trimou.trimness.util.Strings;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class ResultLinkDefinitions extends CompositeComponent<ResultLinkDefinition> {

    public ResultLinkDefinitions() {
    }

    @Inject
    public ResultLinkDefinitions(Instance<ResultLinkDefinition> instances) {
        super(instances, null);
    }

    @Override
    protected boolean checkUniqueIds() {
        return true;
    }

    @Override
    protected boolean isComponentValid(ResultLinkDefinition component) {
        return super.isComponentValid(component) && Strings.matchesLinkPattern(component.getId());
    }

}
