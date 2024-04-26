package org.quickdev.domain.folder.service;

import static org.quickdev.sdk.exception.BizError.NO_RESOURCE_FOUND;

import java.util.Collection;

import org.quickdev.domain.folder.model.Folder;
import org.quickdev.domain.folder.repository.FolderRepository;
import org.quickdev.infra.mongo.MongoUpsertHelper;
import org.quickdev.sdk.constants.FieldName;
import org.quickdev.sdk.exception.BizError;
import org.quickdev.sdk.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FolderService {

    @Autowired
    private FolderRepository repository;

    @Autowired
    private MongoUpsertHelper mongoUpsertHelper;

    public Mono<Boolean> updateById(String id, Folder resource) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return mongoUpsertHelper.updateById(resource, id);
    }

    public Mono<Folder> findById(String id) {
        if (id == null) {
            return Mono.error(new BizException(BizError.INVALID_PARAMETER, "INVALID_PARAMETER", FieldName.ID));
        }

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new BizException(BizError.NO_RESOURCE_FOUND, "FOLDER_NOT_FOUND", id)));
    }

    public Mono<Folder> create(Folder folder) {
        return repository.save(folder);
    }

    public Flux<Folder> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId);
    }

    public Mono<Void> deleteAllById(Collection<String> ids) {
        return repository.deleteAllById(ids);
    }

    public Mono<Boolean> exist(String id) {
        return findById(id)
                .hasElement()
                .onErrorResume(throwable -> {
                    if (throwable instanceof BizException bizException && bizException.getError() == NO_RESOURCE_FOUND) {
                        return Mono.just(false);
                    }
                    return Mono.error(throwable);
                });
    }
}
