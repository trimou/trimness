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

import static org.trimou.trimness.util.Strings.ID;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.trimou.trimness.model.ModelProvider;
import org.trimou.trimness.model.ModelRequest;
import org.trimou.trimness.util.Jsons;
import org.trimou.util.ImmutableMap;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Note that unauthenticated clients can only make 60 requests per hour.
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class GithubModelProvider implements ModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubModelProvider.class.getName());

    static final String REPOSITORY = "repo";

    static final String HOST = "api.github.com";

    static final String COMMITS_RESOURCE_PREFIX = "/repos/";

    static final String COMMITS_RESOURCE_SUFFIX = "/commits";

    static final String DEFAULT_REPO = "trimou/trimness";

    static final String COMMITS = "commits";

    @Inject
    private Vertx vertx;

    private HttpClient client;

    @PostConstruct
    void init() {
        client = vertx.createHttpClient(new HttpClientOptions().setDefaultHost(HOST).setDefaultPort(443).setSsl(true));
    }

    @Override
    public String getNamespace() {
        return REPOSITORY;
    }

    @Override
    public void handle(ModelRequest request) {

        if (!request.getRenderRequest().getTemplate().getId().contains(COMMITS)) {
            request.complete();
            return;
        }

        String repository = request.getRenderRequest().getParameter(REPOSITORY).map((repo) ->
        // Trimness is currently using javax.json when parsing JSON requests
        (repo instanceof JsonString) ? ((JsonString) repo).getString() : repo.toString()).orElse(DEFAULT_REPO).toString();

        LOGGER.info("Handle model request for {0} using thread {1}", repository, Thread.currentThread().getName());

        String uri = COMMITS_RESOURCE_PREFIX + repository + COMMITS_RESOURCE_SUFFIX;
        long start = System.currentTimeMillis();
        client.get(uri, (response) -> {
            LOGGER.info("Fetching last commits from {0} using thread {1}", uri, Thread.currentThread().getName());
            if (response.statusCode() == 200) {
                response.bodyHandler((buffer) -> {
                    JsonValue json;
                    try {
                        json = Jsons.reader(new StringReader(buffer.toString())).read();
                    } catch (JsonException e) {
                        LOGGER.warn("Unable to parse the response from {0}", uri);
                        json = JsonValue.NULL;
                    }
                    LOGGER.info("Last commits fetched successfully from {0} in {1} ms", uri, (System.currentTimeMillis() - start));
                    if (request.getRenderRequest().getTemplate().getId().contains("charts")) {
                        // Prepare chart data
                        request.complete(ImmutableMap.builder().put(ID, repository).put("chart", prepareChartData(json)).build());
                    } else {
                        request.complete(ImmutableMap.builder().put(ID, repository).put(COMMITS, json).build());
                    }
                });
            } else {
                LOGGER.warn("Invalid HTTP response: " + response.statusCode() + " " + response.statusMessage());
                request.complete();
            }
        })
                // Use v3 version of the API
                .putHeader("Accept", "application/vnd.github.v3+json")
                // User-Agent header is required
                .putHeader("User-Agent", "Trimness-Simple-Example").end();
    }

    private Object prepareChartData(JsonValue json) {
        Map<String, Integer> chartData = new HashMap<>();
        if (ValueType.ARRAY.equals(json.getValueType())) {
            JsonArray commits = (JsonArray) json;
            for (JsonValue commit : commits) {
                if (ValueType.OBJECT.equals(commit.getValueType())) {
                    JsonObject commitObject = (JsonObject) commit;
                    chartData.compute(commitObject.getJsonObject("author").getJsonString("login").getString(), (user, value) -> {
                        if (value != null) {
                            return value + 1;
                        } else {
                            return 1;
                        }
                    });
                }
            }
        }
        return chartData;
    }

}
