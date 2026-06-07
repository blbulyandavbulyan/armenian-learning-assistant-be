package com.blbulyandavbulyan.larm.api.phrases;

import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Descriptions;
import com.blbulyandavbulyan.larm.api.openapi.OpenApiConstants.Examples;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;

@Schema(name = "Page Request")
@Validated
record PageRequest(
        @Schema(description = Descriptions.PAGE_NUMBER, defaultValue = "1", example = Examples.PAGE_NUMBER)
        @RequestParam(defaultValue = "1")
        @Min(1)
        int pageNumber,

        @Schema(description = Descriptions.PAGE_SIZE, defaultValue = "10", example = Examples.PAGE_SIZE)
        @RequestParam(defaultValue = "10")
        @Max(100)
        @Min(10)
        int pageSize) {
}
