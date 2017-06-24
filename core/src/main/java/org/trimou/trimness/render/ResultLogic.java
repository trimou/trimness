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

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.trimness.render.RenderLogic.ResultType;
import org.trimou.trimness.util.Jsons;
import org.trimou.trimness.util.Strings;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
class ResultLogic {

    @Inject
    private DelegateResultRepository resultRepository;

    void get(String resultId, String resultType, BiConsumer<String, String> resultConsumer,
            BiConsumer<Integer, String> errorConsumer) {

        if (resultId == null) {
            errorConsumer.accept(Codes.CODE_ID_NOT_SET, "Result id must be set");
            return;
        }

        Result result = resultRepository.get(resultId);

        if (result == null) {
            errorConsumer.accept(Codes.CODE_NOT_FOUND, "Result not found for id: " + resultId);
        } else {
            switch (ResultType.of(resultType)) {
            case RAW:
                if (!result.isComplete()) {
                    resultConsumer.accept(Jsons.message("Result %s not complete yet", resultId).build().toString(),
                            Strings.APP_JSON);
                } else if (result.isFailure()) {
                    resultConsumer.accept(Jsons.message("Result failed: %s", result.getValue()).build().toString(),
                            Strings.APP_JSON);
                } else {
                    resultConsumer.accept(result.getValue(), result.getContentType());
                }
                break;
            case METADATA:
                resultConsumer.accept(Jsons.metadataResult(result), Strings.APP_JSON);
                break;
            default:
                errorConsumer.accept(Codes.CODE_INVALID_RESULT_TYPE, "Unsupported result type: " + resultType);
            }
        }
    }

    void remove(String resultId, BiConsumer<String, String> resultConsumer, BiConsumer<Integer, String> errorConsumer) {

        if (resultId == null) {
            errorConsumer.accept(Codes.CODE_ID_NOT_SET, "Result id must be set");
        } else if (resultRepository.remove(resultId)) {
            resultConsumer.accept(Jsons.message("Result %s removed", resultId).build().toString(), Strings.APP_JSON);
        } else {
            errorConsumer.accept(Codes.CODE_NOT_FOUND, "Result not found for id: " + resultId);
        }
    }

    void getLink(String linkId, Consumer<ResultLink> resultLinkConsumer, BiConsumer<Integer, String> errorConsumer) {

        if (linkId == null) {
            errorConsumer.accept(Codes.CODE_ID_NOT_SET, "Result link id must be set");
        } else {
            ResultLink link = resultRepository.getLink(linkId);

            if (link != null) {
                resultLinkConsumer.accept(link);
            } else {
                errorConsumer.accept(Codes.CODE_NOT_FOUND, "Result link does not exits: " + linkId);
            }
        }
    }

}
