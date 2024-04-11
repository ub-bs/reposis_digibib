package de.vzg.reposis.digibib.contact.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;

import de.vzg.reposis.digibib.contact.model.Contact;

/**
 * Provides service to collect contacts.
 */
public class ContactCollectorService {

    private final static List<ContactCollector> COLLECTORS = new ArrayList<>();

    private static final String COLLECTOR_PROP_PREFIX = "Digibib.ContactCollector.";

    static {
        MCRConfiguration2.getSubPropertiesMap(COLLECTOR_PROP_PREFIX).entrySet().stream()
            .filter(p -> p.getKey().endsWith(".Class")).map(p -> COLLECTOR_PROP_PREFIX.concat(p.getKey()))
            .map(p -> MCRConfiguration2.<ContactCollector>getSingleInstanceOf(p).orElseThrow())
            .forEach(COLLECTORS::add);
    }

    /**
     * Uses configured collectors to return list over {@link Contact} for an object.
     *
     * @param object object
     * @return list of contact elements
     */
    public static List<Contact> collectContacts(MCRObject object) {
        return COLLECTORS.stream().map(c -> c.collect(object)).flatMap(List::stream).collect(Collectors.toList());
    }
}
