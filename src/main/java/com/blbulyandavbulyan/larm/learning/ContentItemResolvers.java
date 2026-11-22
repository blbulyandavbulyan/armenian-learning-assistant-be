package com.blbulyandavbulyan.larm.learning;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import org.springframework.stereotype.Component;

/**
 * Looks up the {@link ContentItemResolver} bean for a content type.
 */
@Component
public class ContentItemResolvers {

    private final Map<ContentItemType, ContentItemResolver> resolversByType;

    public ContentItemResolvers(List<ContentItemResolver> resolvers) {
        this.resolversByType = resolvers.stream()
                .collect(Collectors.toMap(ContentItemResolver::supportedType, Function.identity()));
    }

    public ContentItemResolver forType(ContentItemType contentItemType) {
        ContentItemResolver resolver = resolversByType.get(contentItemType);
        if (resolver == null) {
            throw new IllegalStateException("No content item resolver for type " + contentItemType);
        }
        return resolver;
    }
}
