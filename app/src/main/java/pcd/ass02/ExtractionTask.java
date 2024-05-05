package pcd.ass02;

import io.vertx.core.Future;
import io.vertx.core.AbstractVerticle;

public abstract class ExtractionTask<A, B> extends AbstractVerticle {
    public abstract B extract(A from);

    /*public Future<B> extractAsync(A from) {
        return getVertx().executeBlocking(() -> {
            return extract(from);
        });
    }*/
}