package org.trimou.basis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.trimou.engine.priority.Priorities;
import org.trimou.util.ImmutableList;
import org.trimou.util.ImmutableSet;

/**
 * Collects all template repositories and sorts them by priority.
 *
 * @author Martin Kouba
 */
@Typed(CompositeTemplateRepository.class)
@ApplicationScoped
public class CompositeTemplateRepository implements TemplateRepository {

    private final List<TemplateRepository> repositories;

    // Make it proxyable
    @SuppressWarnings("unused")
    private CompositeTemplateRepository() {
        this.repositories = null;
    }

    @Inject
    public CompositeTemplateRepository(
            Instance<TemplateRepository> templateRepoInstance) {
        List<TemplateRepository> repositories = new ArrayList<>();
        for (TemplateRepository repository : templateRepoInstance) {
            repositories.add(repository);
        }
        Collections.sort(repositories, Priorities.higherFirst());
        this.repositories = ImmutableList.copyOf(repositories);
    }

    @Override
    public Template get(String id) {
        Template template = null;
        for (TemplateRepository repository : repositories) {
            template = repository.get(id);
            if (template != null) {
                break;
            }
        }
        return template;
    }

    @Override
    public Set<Template> getAll() {
        ImmutableSet.ImmutableSetBuilder<Template> builder = ImmutableSet
                .builder();
        for (TemplateRepository repository : repositories) {
            repository.getAll().forEach((t) -> builder.add(t));
        }
        return builder.build();
    }

}
