package com.blbulyandavbulyan.larm.api.learning.plan;

import com.blbulyandavbulyan.larm.security.DatabaseUserJwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
// TODO generate real open api descriptions here instead of 'comemnts'
@RequestMapping("/learning-plan")
public interface LearningPlanApi {

    // adds the item to the learning plan and places it on the first place, if it was already added -> moves it to the first place
    @PostMapping("/items")
    void addItemToPlan(DatabaseUserJwtAuthenticationToken token, @RequestBody AddLearningItemRequest request);

    // removes the item from the learning plan
    @DeleteMapping("/items/{learningItemId}")
    void removeItemFromPlan(DatabaseUserJwtAuthenticationToken token,

                            @PathVariable
                            UUID learningItemId);

    // returns learning plan for today
    @GetMapping
    LearningPlanResponse getMyPlan(DatabaseUserJwtAuthenticationToken token,

            @RequestParam(required = false, defaultValue = "5")
            int maxItems);
}
