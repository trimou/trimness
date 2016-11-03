package org.trimou.basis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.trimou.basis.Result.Code;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class InMemoryResultRepository implements ResultRepository {

    private AtomicLong idGenerator;

    private ConcurrentMap<Long, Result> results;

    @PostConstruct
    void init() {
        idGenerator = new AtomicLong();
        results = new ConcurrentHashMap<>();
    }

    @Override
    public Result get(Long id) {
        return results.get(id);
    }

    @Override
    public Result next(String templateId, String contentType) {
        Result result = Result.init(idGenerator.incrementAndGet(), templateId,
                contentType);
        results.put(result.getId(), result);
        return result;
    }

    @Override
    public void complete(Long id, Code code, String errorMessage,
            String renderedTemplate) {
        Result result = get(id);
        if (result == null) {
            throw new IllegalStateException(
                    "Result with the specified id does not exist: " + result);
        }
        if (result.isComplete()) {
            throw new IllegalStateException(
                    "Result already completed: " + result);
        }
        results.put(id,
                Result.complete(result, code, errorMessage, renderedTemplate));
    }

    @Override
    public boolean remove(Long id) {
        return results.remove(id) != null;
    }

    @Override
    public int size() {
        return results.size();
    }

    @Override
    public void clear() {
        results.clear();
    }

}
