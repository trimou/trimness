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
package org.trimou.trimness.util;

import java.util.regex.Pattern;

public final class Strings {

    private Strings() {
    }

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String MODEL = "model";
    public static final String TEMPLATE_ID = "templateId";
    public static final String NOW = "now";
    public static final String CONTENT_TYPE = "contentType";
    public static final String CONTENT = "content";
    public static final String RESULT = "result";
    public static final String TIME = "time";
    public static final String TIMEOUT = "timeout";
    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";
    public static final String DESCRIPTION = "desc";
    public static final String ASYNC = "async";
    public static final String RESULT_TYPE = "resultType";
    public static final String RESULT_ID = "resultId";
    public static final String CODE = "code";
    public static final String CHECKS = "checks";
    public static final String TEMPLATES = "templates";
    public static final String MSG = "msg";
    public static final String PARAMS = "params";
    public static final String ERROR = "error";
    public static final String OUTPUT = "output";
    public static final String CONFIG = "config";
    public static final String LINK = "link";

    public static final String APP_JSON = "application/json";
    public static final String APP_JAVASCRIPT = "application/javascript";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_CSS = "text/css";
    public static final String TEXT_HTML = "text/html";
    public static final String APP_JSON_UTF8 = APP_JSON + "; charset=utf-8";
    public static final String HEADER_CONTENT_TYPE = "Content-type";

    public static final String SUFFIX_HTML = "html";
    public static final String SUFFIX_HTM = "htm";
    public static final String SUFFIX_CSS = "css";
    public static final String SUFFIX_JS = "js";
    public static final String SUFFIX_JSON = "json";
    public static final String SUFFIX_TXT = "txt";

    private static final Pattern LINK_PATTERN = Pattern.compile("^[a-zA-Z_0-9-]{1,50}");

    public static boolean matchesLinkPattern(String input) {
        return LINK_PATTERN.matcher(input).matches();
    }

}
