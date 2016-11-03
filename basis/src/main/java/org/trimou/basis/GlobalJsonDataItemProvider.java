package org.trimou.basis;

import static org.trimou.basis.BasisConfigurationKey.DEFAULT_FILE_ENCODING;
import static org.trimou.basis.BasisConfigurationKey.GLOBAL_JSON_DATA_FILE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.trimou.util.ImmutableList;
import org.trimou.util.ImmutableList.ImmutableListBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 *
 * @author Martin Kouba
 * @see BasisConfigurationKey#GLOBAL_JSON_DATA_FILE
 */
@ApplicationScoped
public class GlobalJsonDataItemProvider implements DataItemProvider {

    public static final String NAMESPACE = "global";

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GlobalJsonDataItemProvider.class);

    @Inject
    private BasisConfiguration configuration;

    private List<DataItem> dataItems;

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @PostConstruct
    public void init() {

        String filePath = configuration.getStringValue(GLOBAL_JSON_DATA_FILE);
        dataItems = Collections.emptyList();
        if (filePath.isEmpty()) {
            return;
        }

        File file = new File(filePath);
        if (!file.canRead()) {
            LOGGER.debug(
                    "Global JSON data file does not exist or cannot be read: "
                            + file);
            return;
        }

        try {

            JsonElement globalDataElement = new JsonParser()
                    .parse(new InputStreamReader(new FileInputStream(file),
                            configuration
                                    .getStringValue(DEFAULT_FILE_ENCODING)));
            if (globalDataElement.isJsonObject()) {

                ImmutableListBuilder<DataItem> builder = ImmutableList
                        .builder();
                JsonObject globalDataObject = globalDataElement
                        .getAsJsonObject();

                for (Entry<String, JsonElement> entry : globalDataObject
                        .entrySet()) {
                    builder.add(new ImmutableDataItem(entry.getKey(),
                            entry.getValue()));
                }
                dataItems = builder.build();
            }

        } catch (JsonIOException | JsonSyntaxException
                | UnsupportedEncodingException | FileNotFoundException e) {
            LOGGER.warn("Error reading global JSON data file: " + file, e);
        }
    }

    @Override
    public List<DataItem> getData(String templateId) {
        return dataItems;
    }

}
