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
package org.trimou.trimness.template;

import java.util.function.Supplier;

/**
 * An immutable {@link Template} implementation.
 *
 * @author Martin Kouba
 */
public class ImmutableTemplate implements Template {

    public static ImmutableTemplate of(String id) {
        return new ImmutableTemplate(id, null, null);
    }

    public static ImmutableTemplate of(String id, Supplier<String> contentLoader, String contentType) {
        return new ImmutableTemplate(id, contentLoader, contentType);
    }

    public static ImmutableTemplate of(String id, String content, String contentType) {
        return new ImmutableTemplate(id, () -> content, contentType);
    }

    private final String id;

    private final Supplier<String> contentLoader;

    private final String contentType;

    /**
     *
     * @param id
     * @param contentLoader
     * @param contentType
     */
    ImmutableTemplate(String id, Supplier<String> contentLoader, String contentType) {
        this.id = id;
        this.contentLoader = contentLoader;
        this.contentType = contentType;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        if (contentLoader == null) {
            throw new UnsupportedOperationException();
        }
        return contentLoader.get();
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImmutableTemplate other = (ImmutableTemplate) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Template [id=" + id + ", contentType=" + contentType + "]";
    }

}
