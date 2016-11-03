package org.trimou.basis;

import org.trimou.basis.Result.Code;

/**
 * A non-default result repository must be an alternative with priority.
 *
 * @author Martin Kouba
 */
public interface ResultRepository {

    /**
     *
     * @param id
     * @return the result with the given id or null
     */
    Result get(Long id);

    /**
     *
     * @return a new incomplete result
     */
    Result next(String templateId, String contentType);

    /**
     *
     * @param id
     * @param code
     * @param errorMessage
     * @param renderedTemplate
     * @param contentType
     */
    void complete(Long id, Code code, String errorMessage,
            String renderedTemplate);

    /**
     *
     * @param id
     * @return <code>true</code> if removed, <code>false</code> otherwise
     */
    boolean remove(Long id);

    /**
     *
     * @return the number of stored results
     */
    int size();

    /**
     * Remove all results.
     */
    void clear();

}
