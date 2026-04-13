package studio.seer.dali;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Dali — async PL/SQL parse service.
 *
 * <p>Accepts parse sessions via REST, enqueues them as JobRunr background jobs,
 * executes them in-JVM via {@link com.hound.HoundParserImpl}, and persists
 * job state in FRIGG (ArcadeDB).
 */
@QuarkusMain
public class DaliApplication {

    public static void main(String... args) {
        Quarkus.run(args);
    }
}
