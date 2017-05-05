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
package org.trimou.trimness;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.trimness.monitor.HealthCheck;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class TrimouEngine implements HealthCheck {

    static final String TEST_TEMPLATE = TrimouEngine.class.getName() + "_test";

    private final AtomicReference<MustacheEngine> reference;

    public TrimouEngine() {
        reference = new AtomicReference<>();
    }

    void setMustacheEngine(MustacheEngine engine) {
        reference.set(engine);
    }

    @Dependent
    @Produces
    public MustacheEngine produceEngine() {
        return reference.get();
    }

    @Override
    public Result perform() {
        Result result;
        MustacheEngine engine = reference.get();
        if (engine == null) {
            result = Result.failure("Engine not available");
        }
        Mustache mustache = engine.getMustache(TEST_TEMPLATE);
        if (mustache == null) {
            result = Result.failure("Test template not available");
        }
        result = Boolean.valueOf(mustache.render(true)) ? Result.SUCCESS
                : Result.failure("Test template not evaluated correctly");
        return result;
    }

}
