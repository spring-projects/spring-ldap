package org.springframework.ldap.core;

import javax.naming.Binding;
import javax.naming.NameClassPair;

/**
 * A CollectingNameClassPairCallbackHandler to wrap a ContextMapper. That
 * is, the found object is extracted from each {@link Binding}, and then
 * passed to the specified ContextMapper for translation.
 * 
 * @author Mattias Arthursson
 * @author Ulrik Sandberg
 */
public class ContextMapperCallbackHandler extends
        CollectingNameClassPairCallbackHandler {
    private ContextMapper mapper;

    public ContextMapperCallbackHandler(ContextMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Cast the NameClassPair to a {@link Binding} and pass its attributes
     * to the ContextMapper.
     * 
     * @param nameClassPair
     *            a SearchResult instance.
     * @return the Object returned from the Mapper.
     */
    public Object getObjectFromNameClassPair(NameClassPair nameClassPair) {
        Binding binding = (Binding) nameClassPair;
        Object object = binding.getObject();
        if (object == null) {
            throw new ObjectRetrievalException(
                    "SearchResult did not contain any object.");
        }
        return mapper.mapFromContext(object);
    }
}