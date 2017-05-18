package org.trimou.trimness.model;

import java.util.Collections;
import java.util.Map;

import org.trimou.trimness.render.RenderRequest;
import org.trimou.trimness.render.SimpleRenderRequest;
import org.trimou.trimness.template.ImmutableTemplate;
import org.trimou.trimness.template.Template;
import org.trimou.trimness.util.Jsons;

class DummyModelRequest implements ModelRequest {

    private volatile Object result;

    private final Template template;

    public DummyModelRequest() {
        this(ImmutableTemplate.of("foo"), Collections.emptyMap());
    }

    public DummyModelRequest(Template template, Map<String, Object> params) {
        this.template = template;
    }

    public RenderRequest getRenderRequest() {
        return new SimpleRenderRequest(template, null, Jsons.EMPTY_OBJECT);
    }

    @Override
    public void complete(Object result) {
        this.result = result;
    }

    Object getResult() {
        return result;
    }

}
