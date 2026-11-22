package com.blbulyandavbulyan.larm.dao.repository;

import java.util.Optional;
import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.dao.entities.LearningItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningItemRepository extends JpaRepository<LearningItem, UUID> {

    Optional<LearningItem> findByUserIdAndContentItemIdAndContentItemType(
            UUID userId, UUID contentItemId, ContentItemType contentItemType);

    Optional<LearningItem> findByIdAndUserId(UUID id, UUID userId);
}
