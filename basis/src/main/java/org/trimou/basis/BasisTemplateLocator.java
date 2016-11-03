package org.trimou.basis;

import java.io.Reader;
import java.io.StringReader;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.trimou.engine.locator.TemplateLocator;

/**
 *
 * @author Martin Kouba
 */
@Dependent
public class BasisTemplateLocator implements TemplateLocator {

    @Inject
    private CompositeTemplateRepository repository;

    @Override
    public Reader locate(String name) {
        Template template = repository.get(name);
        if (template != null) {
            return new StringReader(template.getContent());
        }
        return null;
    }

    @Override
    public Set<String> getAllIdentifiers() {
        return repository.getAll().stream().map((template) -> template.getId())
                .collect(Collectors.toSet());
    }

}
