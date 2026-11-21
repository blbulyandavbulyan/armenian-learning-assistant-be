package com.blbulyandavbulyan.larm.api.learning;

import java.util.UUID;

import com.blbulyandavbulyan.larm.dao.entities.ContentItemType;
import com.blbulyandavbulyan.larm.learning.EnrolmentResult;
import com.blbulyandavbulyan.larm.learning.LearningItemService;
import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
class LearningItemsController implements LearningItemsApi {

    private final LearningItemService learningItemService;

    private final LearningResponseMapper mapper;

    @Override
    public ResponseEntity<LearningItemResponse> enrol(DatabaseUserJwtAuthenticationToken auth, EnrolLearningItemRequest request) {
        EnrolmentResult result = learningItemService.enrol(auth.getUserId(), request.contentItemId(), request.contentItemType());
        return ResponseEntity.status(result.created() ? HttpStatus.CREATED : HttpStatus.OK).body(mapper.toResponse(result.item()));
    }

    @Override
    public ResponseEntity<Void> exclude(DatabaseUserJwtAuthenticationToken auth, UUID learningItemId) {
        learningItemService.exclude(auth.getUserId(), learningItemId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public LearningItemResponse findByContent(DatabaseUserJwtAuthenticationToken auth, UUID contentItemId,
                                              ContentItemType contentItemType) {
        return mapper.toResponse(learningItemService.findActiveByContent(auth.getUserId(), contentItemId, contentItemType));
    }

    @Override
    public LearningCardsResponse getCards(DatabaseUserJwtAuthenticationToken auth, UUID learningItemId) {
        return mapper.toResponse(learningItemService.getCards(auth.getUserId(), learningItemId));
    }
}
