package org.trimou.basis;

import org.trimou.basis.DataItemProvider.DataItem;

/**
 *
 * @author Martin Kouba
 */
public class ImmutableDataItem implements DataItem {

    static ImmutableDataItem of(String name, Object value) {
        return new ImmutableDataItem(name, value);
    }

    private final String name;

    private final Object value;

    ImmutableDataItem(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        return value;
    }

}
