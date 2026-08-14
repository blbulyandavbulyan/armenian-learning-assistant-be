package com.blbulyandavbulyan.larm.api.exercises;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

@RequestMapping("/exercises")
public interface ExerciseApi {

    @PostMapping("/progress")
    void trackProgress(@RequestBody TrackProgressRequest request);
}
