package org.quickdev.api.usermanagement.view;

import org.quickdev.domain.group.model.GroupMember;
import org.quickdev.domain.user.model.User;

public class GroupMemberView {

    private final GroupMember groupMember;
    private final User user;

    public GroupMemberView(GroupMember groupMember, User user) {
        this.groupMember = groupMember;
        this.user = user;
    }

    public String getUserId() {
        return user.getId();
    }

    public String getUserName() {
        return user.getName();
    }

    public String getAvatarUrl() {
        return user.getAvatarUrl();
    }

    public String getRole() {
        return groupMember.getRole().getValue();
    }

    public String getGroupId() {
        return groupMember.getGroupId();
    }

    public String getOrgId() {
        return groupMember.getOrgId();
    }

    public long getJoinTime() {
        return groupMember.getJoinTime();
    }
}
