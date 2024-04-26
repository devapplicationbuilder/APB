package org.quickdev.infra.event.user;

import org.quickdev.infra.event.AbstractEvent;
import org.quickdev.infra.event.EventType;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class UserLogoutEvent extends AbstractEvent {

    @Override
    public EventType getEventType() {
        return EventType.USER_LOGOUT;
    }
}
