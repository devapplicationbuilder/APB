package org.quickdev.infra.event.groupmember;

import org.quickdev.infra.event.EventType;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class GroupMemberRemoveEvent extends BaseGroupMemberEvent {

    @Override
    public EventType getEventType() {
        return EventType.GROUP_MEMBER_REMOVE;
    }
}
