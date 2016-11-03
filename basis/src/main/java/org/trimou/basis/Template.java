package org.trimou.basis;

/**
 *
 * @author Martin Kouba
 */
public class Template {

    static Template of(String id, String contentType) {
        return of(id, null, contentType);
    }

    static Template of(String id, ContentLoader contentLoader,
            String contentType) {
        return new Template(id, contentLoader, contentType);
    }

    private final String id;

    private final ContentLoader contentLoader;

    private final String contentType;

    /**
     *
     * @param name
     * @param contentLoader
     * @param contentType
     */
    Template(String name, ContentLoader contentLoader, String contentType) {
        this.id = name;
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
        return contentLoader.load();
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
        Template other = (Template) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @FunctionalInterface
    interface ContentLoader {

        String load();

    }

}
