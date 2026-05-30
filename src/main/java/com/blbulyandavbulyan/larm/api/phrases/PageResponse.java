package com.blbulyandavbulyan.larm.api.phrases;

import lombok.Builder;

@Builder
record PageResponse(int pageNumber, int pageSize, int totalPages) {
}
