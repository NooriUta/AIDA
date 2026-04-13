package studio.seer.dali.infrastructure;

import com.hound.HoundParserImpl;
import com.hound.api.HoundParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * CDI producer — exposes {@link HoundParser} as an {@code @ApplicationScoped} bean
 * so that {@code ParseJob} and other beans can inject it without knowing the impl class.
 *
 * <p>{@link HoundParserImpl} is thread-safe — each parse call creates its own
 * engine/listener instances, so one shared instance is sufficient.
 */
@ApplicationScoped
public class HoundParserProducer {

    @Produces
    @ApplicationScoped
    public HoundParser houndParser() {
        return new HoundParserImpl();
    }
}
