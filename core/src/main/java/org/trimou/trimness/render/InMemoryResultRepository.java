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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class InMemoryResultRepository implements ResultRepository {

    private AtomicLong idGenerator;

    private ConcurrentMap<String, SimpleResult> results;

    @PostConstruct
    void init() {
        idGenerator = new AtomicLong(System.currentTimeMillis());
        results = new ConcurrentHashMap<>();
    }

    @Override
    public SimpleResult get(String id) {
        return results.get(id);
    }

    @Override
    public Result init(String templateId, String contentType) {
        SimpleResult result = SimpleResult.init("" + idGenerator.incrementAndGet(), templateId, contentType);
        results.put(result.getId(), result);
        return result;
    }

    @Override
    public boolean remove(String id) {
        return results.remove(id) != null;
    }

    @Override
    public int size() {
        return results.size();
    }

    @Override
    public void clear() {
        results.clear();
    }

}
