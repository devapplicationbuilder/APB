package org.quickdev.api.query.view;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.quickdev.sdk.util.StreamUtils.toMapNullFriendly;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.quickdev.domain.query.model.LibraryQueryCombineId;
import org.quickdev.sdk.models.Param;

import lombok.Setter;

@Setter
public class QueryExecutionRequest {

    private String applicationId;

    private String queryId;

    private String libraryQueryId;

    private String libraryQueryRecordId;

    private List<Param> params;

    private boolean viewMode = false;

    private String[] path;

    public String getApplicationId() {
        return applicationId;
    }

    public String getQueryId() {
        return queryId;
    }

    public boolean isViewMode() {
        return viewMode;
    }

    public String[] getPath() {
        return path;
    }

    public Map<String, Object> paramMap() {
        return emptyIfNull(params).stream()
                .filter(it -> StringUtils.isNotBlank(it.getKey()))
                .collect(toMapNullFriendly(param -> param.getKey().trim(), Param::getValue, (a, b) -> b));
    }

    public boolean isApplicationQueryRequest() {
        return StringUtils.isNoneBlank(queryId, applicationId);
    }

    public LibraryQueryCombineId getLibraryQueryCombineId() {
        return new LibraryQueryCombineId(libraryQueryId, libraryQueryRecordId);
    }
}
