package org.quickdev.api.query.view;

import java.util.List;

import org.quickdev.domain.query.model.LibraryQuery;
import org.quickdev.domain.query.model.LibraryQueryRecord;
import org.quickdev.domain.user.model.User;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibraryQueryAggregateView(LibraryQueryMetaView libraryQueryMetaView,
                                        List<LibraryQueryRecordMetaView> recordMetaViewList) {

    public static LibraryQueryAggregateView from(LibraryQuery libraryQuery, User libraryQueryCreator, List<LibraryQueryRecord> libraryQueryRecordList) {
        List<LibraryQueryRecordMetaView> libraryQueryRecordMetaViews = libraryQueryRecordList.stream()
                .map(LibraryQueryRecordMetaView::from)
                .toList();
        LibraryQueryMetaView libraryQueryMetaView = LibraryQueryMetaView.from(libraryQuery, libraryQueryCreator);
        return new LibraryQueryAggregateView(libraryQueryMetaView, libraryQueryRecordMetaViews);
    }

    public static LibraryQueryAggregateView from(LibraryQuery libraryQuery, User libraryQueryCreator) {
        LibraryQueryMetaView libraryQueryMetaView = LibraryQueryMetaView.from(libraryQuery, libraryQueryCreator);
        return new LibraryQueryAggregateView(libraryQueryMetaView, null);
    }

}
