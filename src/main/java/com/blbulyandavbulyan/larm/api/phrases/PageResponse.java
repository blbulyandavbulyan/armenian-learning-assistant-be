package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

@Builder
public record PageResponse(int pageNumber, int pageSize, int totalPages) {
}
