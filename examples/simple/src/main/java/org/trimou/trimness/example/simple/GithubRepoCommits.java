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

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.trimness.model.ModelProvider;
import org.trimou.trimness.model.ModelRequest;
import org.trimou.util.ImmutableMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
public class GithubRepoCommits implements ModelProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(GithubRepoCommits.class.getName());

    private static final String REPOSITORY = "repository";

    private static final String HOST = "api.github.com";

    private static final String COMMITS_RESOURCE_PREFIX = "/repos/";

    private static final String COMMITS_RESOURCE_SUFFIX = "/commits";

    private static final String DEFAULT_REPO = "trimou/trimness";

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

        String repository = request.getParameter(REPOSITORY).map((r) -> {
            // Currently, trimness is using gson to parse the request payload
            if (r instanceof JsonElement) {
                return ((JsonElement) r).getAsString();
            } else {
                return r.toString();
            }
        }).orElse(DEFAULT_REPO).toString();

        LOGGER.info("Handle model request for {0} using thread {1}", repository, Thread.currentThread().getName());

        String uri = COMMITS_RESOURCE_PREFIX + repository + COMMITS_RESOURCE_SUFFIX;
        client.get(uri, (response) -> {
            LOGGER.info("Fetching last commits from {0} using thread {1}", uri, Thread.currentThread().getName());
            if (response.statusCode() == 200) {
                response.bodyHandler((buffer) -> {
                    request.setResult(ImmutableMap.builder().put("id", repository).put("commits", new JsonParser().parse(buffer.toString())).build());
                });
            } else {
                LOGGER.warn("Invalid HTTP response: " + response.statusCode() + " " + response.statusMessage());
                request.noResult();
            }
        })
                // Use v3 version of the API
                .putHeader("Accept", "application/vnd.github.v3+json")
                // User-Agent header is required
                .putHeader("User-Agent", "Trimness-Simple-Example").end();
    }

}
