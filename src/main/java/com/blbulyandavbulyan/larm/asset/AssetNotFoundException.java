package com.blbulyandavbulyan.larm.asset;

import com.blbulyandavbulyan.larm.core.exception.EntityNotFoundException;

public class AssetNotFoundException extends EntityNotFoundException {
    public AssetNotFoundException(String message) {
        super(message);
    }
}
