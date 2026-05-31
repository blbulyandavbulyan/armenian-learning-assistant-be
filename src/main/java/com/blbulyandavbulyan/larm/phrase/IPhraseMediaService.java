package com.blbulyandavbulyan.larm.phrase;

import java.util.List;

public interface IPhraseMediaService {
    List<MediaResource> saveMedias(List<CreateMediaResource> createMediaResources);
}
