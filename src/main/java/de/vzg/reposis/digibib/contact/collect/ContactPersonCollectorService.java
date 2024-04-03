package de.vzg.reposis.digibib.contact.collect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mycore.common.config.MCRConfiguration2;
import org.mycore.datamodel.metadata.MCRObject;

import de.vzg.reposis.digibib.contact.model.ContactPerson;

/**
 * Provides service to collect contact persons.
 */
public class ContactPersonCollectorService {

    private final static List<ContactPersonCollector> COLLECTORS = new ArrayList<>();

    private static final String COLLECTOR_PROP_PREFIX = "Digibib.ContactPersonCollector.";

    static {
        MCRConfiguration2.getSubPropertiesMap(COLLECTOR_PROP_PREFIX).entrySet().stream()
            .filter(p -> p.getKey().endsWith(".Class"))
            .map(p -> COLLECTOR_PROP_PREFIX.concat(p.getKey()))
            .map(p -> MCRConfiguration2.<ContactPersonCollector>getSingleInstanceOf(p).orElseThrow())
            .forEach(COLLECTORS::add);
    }

    /**
     * Uses configured person collectors to return list over {@link ContactPerson} for an object.
     *
     * @param object object
     * @return list of contact person elements
     */
    public static List<ContactPerson> collectContactPersons(MCRObject object) {
        return COLLECTORS.stream().map(c -> c.collect(object)).flatMap(List::stream).collect(Collectors.toList());
    }
}
