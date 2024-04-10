package org.quickdev.api.query;

import java.util.List;
import java.util.Map;

import org.quickdev.api.framework.view.ResponseView;
import org.quickdev.api.query.view.LibraryQueryRecordMetaView;
import org.quickdev.domain.query.model.LibraryQueryCombineId;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
public class LibraryQueryRecordController implements LibraryQueryRecordEndpoints 
{
    private final LibraryQueryRecordApiService libraryQueryRecordApiService;

    @Override
    public Mono<Void> delete(@PathVariable String libraryQueryRecordId) {
        return libraryQueryRecordApiService.delete(libraryQueryRecordId);
    }

    @Override
    public Mono<ResponseView<List<LibraryQueryRecordMetaView>>> getByLibraryQueryId(@RequestParam(name = "libraryQueryId") String libraryQueryId) {
        return libraryQueryRecordApiService.getByLibraryQueryId(libraryQueryId)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Map<String, Object>>> dslById(@RequestParam(name = "libraryQueryId") String libraryQueryId,
            @RequestParam(name = "libraryQueryRecordId") String libraryQueryRecordId) {
        LibraryQueryCombineId libraryQueryCombineId = new LibraryQueryCombineId(libraryQueryId, libraryQueryRecordId);
        return libraryQueryRecordApiService.getRecordDSLFromLibraryQueryCombineId(libraryQueryCombineId)
                .map(ResponseView::success);
    }

}
