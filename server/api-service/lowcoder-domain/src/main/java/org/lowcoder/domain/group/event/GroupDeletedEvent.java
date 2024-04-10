package org.quickdev.domain.group.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupDeletedEvent {

    private String groupId;

}
