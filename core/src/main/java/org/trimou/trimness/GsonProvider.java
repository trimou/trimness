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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class GsonProvider {

    private volatile Gson gson;

    @PostConstruct
    public void init() {
        gson = new GsonBuilder().create();
    }

    @Dependent
    @Produces
    public Gson provideGson() {
        return gson;
    }

}
