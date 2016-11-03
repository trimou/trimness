package org.trimou.basis;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class GsonProvider {

    private volatile Gson gson;

    @PostConstruct
    public void init() {
        gson = new GsonBuilder().create();
    }

    @Dependent
    @Produces
    public Gson provideGson() {
        return gson;
    }

}
