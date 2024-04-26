package org.quickdev.api.permission;

import static org.quickdev.api.util.ViewBuilder.multiBuild;

import java.util.List;
import java.util.Locale;

import javax.validation.constraints.NotEmpty;

import org.quickdev.api.permission.view.PermissionItemView;
import org.quickdev.domain.group.model.Group;
import org.quickdev.domain.group.service.GroupService;
import org.quickdev.domain.permission.model.ResourceHolder;
import org.quickdev.domain.permission.model.ResourcePermission;
import org.quickdev.domain.user.service.UserService;
import org.quickdev.sdk.util.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PermissionHelper {

    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;

    public Mono<List<PermissionItemView>> getGroupPermissions(@NotEmpty List<ResourcePermission> resourcePermissions) {
        return Flux.fromIterable(resourcePermissions)
                .filter(ResourcePermission::ownedByGroup)
                .collectList()
                .flatMap(groupPermissions -> Mono.deferContextual(contextView -> {
                    Locale locale = LocaleUtils.getLocale(contextView);
                    return multiBuild(groupPermissions,
                            ResourcePermission::getResourceHolderId,
                            groupService::getByIds,
                            Group::getId,
                            (permission, group) -> PermissionItemView.builder()
                                    .permissionId(permission.getId())
                                    .type(ResourceHolder.GROUP)
                                    .id(group.getId())
                                    .name(group.getName(locale))
                                    .avatar("")
                                    .role(permission.getResourceRole().getValue())
                                    .build()
                    );
                }));
    }

    public Mono<List<PermissionItemView>> getUserPermissions(@NotEmpty List<ResourcePermission> resourcePermissions) {
        return Flux.fromIterable(resourcePermissions)
                .filter(ResourcePermission::ownedByUser)
                .collectList()
                .flatMap(userPermissions -> multiBuild(userPermissions,
                        ResourcePermission::getResourceHolderId,
                        userService::getByIds,
                        (permission, user) -> PermissionItemView.builder()
                                .permissionId(permission.getId())
                                .type(ResourceHolder.USER)
                                .id(user.getId())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .role(permission.getResourceRole().getValue())
                                .build()
                ));
    }
}
