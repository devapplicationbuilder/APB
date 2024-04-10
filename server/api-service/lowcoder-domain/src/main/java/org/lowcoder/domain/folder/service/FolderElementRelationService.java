package org.quickdev.domain.folder.service;

import static org.quickdev.infra.birelation.BiRelationBizType.FOLDER_ELEMENT;

import java.util.List;

import org.quickdev.domain.folder.model.FolderElement;
import org.quickdev.infra.birelation.BiRelationBizType;
import org.quickdev.infra.birelation.BiRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FolderElementRelationService {

    @Autowired
    private BiRelationService biRelationService;

    public Mono<Boolean> deleteByFolderIds(List<String> folderIds) {
        return biRelationService.removeAllBiRelations(FOLDER_ELEMENT, folderIds);
    }

    public Mono<Boolean> deleteByElementId(String elementId) {
        return biRelationService.removeAllBiRelationsByTargetId(FOLDER_ELEMENT, elementId);
    }

    public Mono<Void> create(String folderId, String elementId) {
        return biRelationService.addBiRelation(BiRelationBizType.FOLDER_ELEMENT, folderId, elementId, null, null)
                .then();
    }

    public Flux<FolderElement> getByElementIds(List<String> elementIds) {
        return biRelationService.getByTargetIds(BiRelationBizType.FOLDER_ELEMENT, elementIds)
                .map(biRelation -> new FolderElement(biRelation.getSourceId(), biRelation.getTargetId()));
    }
}
