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
package org.trimou.trimness.monitor;

/**
 *
 * @author Martin Kouba
 */
public interface HealthCheck {

    /**
     *
     * @return a unique identifier
     */
    String getId();

    /**
     * Performs the check.
     *
     * @return the result
     */
    Result perform();

    /**
     *
     *
     */
    public static final class Result {

        public static final Result SUCCESS = new Result(true, null);

        public static Result failure(String details) {
            return new Result(false, details);
        }

        private final boolean isOk;

        private final String details;

        private Result(boolean isOk, String details) {
            this.isOk = isOk;
            this.details = details;
        }

        boolean isOk() {
            return isOk;
        }

        String getDetails() {
            return details;
        }

        boolean hasDetails() {
            return details != null;
        }

    }

}
