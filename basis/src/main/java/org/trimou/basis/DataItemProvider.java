package org.trimou.basis;

import java.util.List;

/**
 *
 * @author Martin Kouba
 */
public interface DataItemProvider {

    /**
     * The namespace must be unique. <tt>data</tt> is reserved.
     *
     * @return the namespace
     */
    String getNamespace();

    /**
     *
     * @param templateId
     * @return the data items
     */
    List<DataItem> getData(String templateId);

    /**
     * Represents a named data item.
     */
    interface DataItem {

        String getName();

        Object getValue();

    }

}
