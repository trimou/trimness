package org.trimou.basis;

import java.util.Set;

import org.trimou.engine.priority.WithPriority;

/**
 * There might be several template repositories deployed.
 *
 * @author Martin Kouba
 * @see CompositeTemplateRepository
 */
public interface TemplateRepository extends WithPriority {

    /**
     *
     * @param id
     * @return the template with the given id or <code>null</code>
     */
    Template get(String id);

    /**
     *
     * @return all available templates
     */
    Set<Template> getAll();

}
