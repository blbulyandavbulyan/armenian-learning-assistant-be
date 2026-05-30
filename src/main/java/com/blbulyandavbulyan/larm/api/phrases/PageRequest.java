package com.blbulyandavbulyan.larm.api.phrases;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

@Validated
record PageRequest(
        @RequestParam(defaultValue = "1")
        @Min(1)
        int pageNumber,

        @RequestParam(defaultValue = "10")
        @Max(100)
        @Min(10)
        int pageSize) {
}
