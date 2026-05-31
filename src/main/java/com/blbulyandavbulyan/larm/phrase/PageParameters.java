package com.blbulyandavbulyan.larm.phrase;

import lombok.Builder;

/**
 * Provides page parameters
 * @param pageNumber page number, first page starts with 1
 * @param pageSize page size
 */
@Builder
public record PageParameters(int pageNumber, int pageSize) {
}
