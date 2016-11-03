package org.trimou.basis;

import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import org.trimou.Mustache;
import org.trimou.engine.MustacheEngine;
import org.trimou.engine.config.Configuration;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class MustacheEngineProvider implements HealthCheck {

    static final String TEST_TEMPLATE = MustacheEngineProvider.class.getName()
            + "_test";

    private final AtomicReference<MustacheEngine> reference;

    public MustacheEngineProvider() {
        reference = new AtomicReference<>();
    }

    void setMustacheEngine(MustacheEngine engine) {
        reference.set(engine);
    }

    @Dependent
    @Produces
    public MustacheEngine provideEngine() {
        return reference.get();
    }

    @Dependent
    @Produces
    public Configuration provideConfiguration() {
        return reference.get().getConfiguration();
    }

    @Override
    public String getId() {
        return MustacheEngine.class.getName();
    }

    @Override
    public Result perform() {
        MustacheEngine engine = reference.get();
        if (engine == null) {
            Result.failure("Engine not available");
        }
        Mustache mustache = engine.getMustache(TEST_TEMPLATE);
        if (mustache == null) {
            Result.failure("Test template not available");
        }
        if (Boolean.valueOf(mustache.render(true))) {
            return Result.SUCCESS;
        }
        return Result.failure("Test template not evaluated correctly");
    }

}
