package org.quickdev.domain.datasource.model;

import org.quickdev.sdk.models.HasIdAndAuditing;
import org.quickdev.sdk.models.TokenBasedConnectionDetail;

import lombok.Getter;
import lombok.Setter;

/**
 * token based plugin needs to save user token for future reuse
 */
@Getter
@Setter
public class TokenBasedConnection extends HasIdAndAuditing {

    private String datasourceId;
    private TokenBasedConnectionDetail tokenDetail;

    public boolean isStale() {
        return tokenDetail.isStale();
    }
}
