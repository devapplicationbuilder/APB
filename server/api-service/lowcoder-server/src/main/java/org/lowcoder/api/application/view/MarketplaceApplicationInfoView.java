package org.quickdev.api.application.view;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.quickdev.domain.application.model.ApplicationStatus;

@Builder
@Getter
@Setter
public class MarketplaceApplicationInfoView {

    // marketplace specific details
    private String title;
    private String description;
    private String category;
    private String image;

    // org details
    private final String orgId;
    private final String orgName;

    // creator info
    private final String creatorEmail;

    // App details
    private final String applicationId;
    private final String name;
    private final long createAt;
    private final String createBy;
    /**
     * @see org.quickdev.domain.application.model.ApplicationType
     */
    private final int applicationType;
    private final ApplicationStatus applicationStatus;


}
