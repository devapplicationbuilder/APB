package org.quickdev.api.permission.view;

import org.quickdev.domain.permission.model.ResourceHolder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PermissionItemView {
    private String permissionId;
    private ResourceHolder type;
    private String id;
    private String avatar;
    private String name;
    private String role;
}
