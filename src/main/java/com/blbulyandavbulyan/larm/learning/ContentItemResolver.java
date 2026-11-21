package com.blbulyandavbulyan.larm.learning;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.ExerciseType;
import org.jspecify.annotations.Nullable;

/**
 * Gives the exercise engine access to one content type without it knowing that type's tables.
 */
public interface ContentItemResolver {

    ContentItemType supportedType();

    /**
     * Exercise types offered for this content type; enrolment creates one card per type and variant.
     */
    Set<ExerciseType> supportedExerciseTypes();

    boolean exists(UUID contentItemId);

    /**
     * Variants that are practised separately (speakers of a dialogue); empty means a single card without variant.
     */
    List<UUID> resolveVariantIds(UUID contentItemId);

    /**
     * Ordered sub-items to answer for the variant, or all sub-items when the variant is null.
     */
    List<UUID> resolveSubItemIds(UUID contentItemId, @Nullable UUID variantId);

    /**
     * Expected answer of a sub-item, empty when the sub-item does not belong to the content item.
     */
    Optional<ExpectedAnswer> resolveExpectedAnswer(UUID contentItemId, UUID contentSubItemId);
}
