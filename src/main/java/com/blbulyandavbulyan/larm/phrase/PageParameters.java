package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

@Builder
public record PageParameters(int pageNumber, int pageSize) {
}
