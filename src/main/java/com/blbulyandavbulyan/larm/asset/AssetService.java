package com.blbulyandavbulyan.larm.asset;

import java.util.UUID;

public interface AssetService {
    /**
     * Find assets by media id.
     *
     * @param mediaId mediaId which should be fetched
     * @return Returns the found asset.
     * @throws AssetNotFoundException in case if asset(media) was not found
     */
    AssetResource findAssetByMediaId(UUID mediaId);
}
