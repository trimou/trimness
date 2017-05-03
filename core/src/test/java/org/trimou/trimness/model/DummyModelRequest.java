package org.trimou.trimness.model;

import java.util.Optional;

import org.trimou.trimness.template.Template;

class DummyModelRequest implements ModelRequest {

    private volatile Object result;

    @Override
    public Template getTemplate() {
        return null;
    }

    @Override
    public Optional<Object> getParameter(String name) {
        return Optional.empty();
    }

    @Override
    public void setResult(Object result) {
        this.result = result;
    }

    Object getResult() {
        return result;
    }

}
