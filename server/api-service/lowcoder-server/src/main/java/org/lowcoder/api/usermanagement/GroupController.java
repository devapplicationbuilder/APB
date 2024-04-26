package org.quickdev.api.usermanagement;

import static org.quickdev.sdk.util.ExceptionUtils.ofError;

import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.quickdev.api.framework.view.ResponseView;
import org.quickdev.api.home.SessionUserService;
import org.quickdev.api.usermanagement.view.AddMemberRequest;
import org.quickdev.api.usermanagement.view.CreateGroupRequest;
import org.quickdev.api.usermanagement.view.GroupMemberAggregateView;
import org.quickdev.api.usermanagement.view.GroupView;
import org.quickdev.api.usermanagement.view.UpdateGroupRequest;
import org.quickdev.api.usermanagement.view.UpdateRoleRequest;
import org.quickdev.api.util.BusinessEventPublisher;
import org.quickdev.domain.group.service.GroupMemberService;
import org.quickdev.domain.group.service.GroupService;
import org.quickdev.domain.organization.model.MemberRole;
import org.quickdev.sdk.exception.BizError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@RestController
public class GroupController implements GroupEndpoints
{

    @Autowired
    private GroupApiService groupApiService;
    @Autowired
    private SessionUserService sessionUserService;
    @Autowired
    private GroupMemberService groupMemberService;
    @Autowired
    private BusinessEventPublisher businessEventPublisher;
    @Autowired
    private GroupService groupService;

    @Override
    public Mono<ResponseView<GroupView>> create(@Valid @RequestBody CreateGroupRequest newGroup) {
        return groupApiService.create(newGroup)
                .delayUntil(group -> businessEventPublisher.publishGroupCreateEvent(group))
                .flatMap(group -> GroupView.from(group, MemberRole.ADMIN.getValue()))
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> update(@PathVariable String groupId,
            @Valid @RequestBody UpdateGroupRequest updateGroupRequest) {
        return groupService.getById(groupId)
                .zipWhen(group -> groupApiService.update(groupId, updateGroupRequest))
                .delayUntil(tuple -> businessEventPublisher.publishGroupUpdateEvent(tuple.getT2(), tuple.getT1(), updateGroupRequest.getGroupName()))
                .map(tuple -> ResponseView.success(tuple.getT2()));
    }

    @Override
    public Mono<ResponseView<Boolean>> delete(@PathVariable String groupId) {
        return groupService.getById(groupId)
                .zipWhen(group -> groupApiService.deleteGroup(groupId))
                .delayUntil(tuple -> businessEventPublisher.publishGroupDeleteEvent(tuple.getT2(), tuple.getT1()))
                .map(tuple -> ResponseView.success(tuple.getT2()));
    }

    @Override
    public Mono<ResponseView<List<GroupView>>> getOrgGroups() {
        return groupApiService.getGroups()
                .map(ResponseView::success);
    }


    @Override
    public Mono<ResponseView<GroupMemberAggregateView>> getGroupMembers(@PathVariable String groupId,
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "count", required = false, defaultValue = "100") int count) {
        return groupApiService.getGroupMembers(groupId, page, count)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> addGroupMember(@PathVariable String groupId,
            @RequestBody AddMemberRequest addMemberRequest) {
        if (StringUtils.isBlank(groupId)) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_ORG_ID");
        }
        if (StringUtils.isBlank(addMemberRequest.getUserId())) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_USER_ID");
        }
        if (StringUtils.isBlank(addMemberRequest.getRole())) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_USER_ROLE");
        }
        return groupApiService.addGroupMember(groupId, addMemberRequest.getUserId(), addMemberRequest.getRole())
                .delayUntil(result -> businessEventPublisher.publishGroupMemberAddEvent(result, groupId, addMemberRequest))
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> updateRoleForMember(@RequestBody UpdateRoleRequest updateRoleRequest,
            @PathVariable String groupId) {
        return groupMemberService.getGroupMember(groupId, updateRoleRequest.getUserId())
                .zipWhen(tuple -> groupApiService.updateRoleForMember(groupId, updateRoleRequest))
                .delayUntil(
                        tuple -> businessEventPublisher.publishGroupMemberRoleUpdateEvent(tuple.getT2(), groupId, tuple.getT1(), updateRoleRequest))
                .map(Tuple2::getT2)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> leaveGroup(@PathVariable String groupId) {
        return sessionUserService.getVisitorOrgMemberCache()
                .flatMap(orgMember -> groupMemberService.getGroupMember(groupId, orgMember.getUserId()))
                .zipWhen(tuple -> groupApiService.leaveGroup(groupId))
                .delayUntil(tuple -> businessEventPublisher.publishGroupMemberLeaveEvent(tuple.getT2(), tuple.getT1()))
                .map(Tuple2::getT2)
                .map(ResponseView::success);
    }

    @Override
    public Mono<ResponseView<Boolean>> removeUser(@PathVariable String groupId,
            @RequestParam String userId) {
        if (StringUtils.isBlank(userId)) {
            return ofError(BizError.INVALID_PARAMETER, "INVALID_USER_ID");
        }
        return groupMemberService.getGroupMember(groupId, userId)
                .zipWhen(groupMember -> groupApiService.removeUser(groupId, userId))
                .delayUntil(tuple -> businessEventPublisher.publishGroupMemberRemoveEvent(tuple.getT2(), tuple.getT1()))
                .map(Tuple2::getT2)
                .map(ResponseView::success);
    }
}
