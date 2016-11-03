package org.trimou.basis;

import static org.trimou.basis.Strings.TEMPLATE_ID;
import static org.trimou.basis.Strings.TIME;

import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.trimou.util.ImmutableList;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class MetaDataItemProvider implements DataItemProvider {

    public static final String NAMESPACE = "meta";

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public List<DataItem> getData(String templateId) {
        return ImmutableList.<DataItem> builder()
                .add(ImmutableDataItem.of(TIME, LocalDateTime.now()))
                .add(ImmutableDataItem.of(TEMPLATE_ID, templateId)).build();
    }

}
