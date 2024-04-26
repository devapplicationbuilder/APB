package org.quickdev.infra.event.datasource;

import java.util.Collection;

import org.quickdev.infra.event.AbstractEvent;
import org.quickdev.infra.event.EventType;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class DatasourcePermissionEvent extends AbstractEvent {

    private final String datasourceId;
    private final String name;
    private final String type;

    private final Collection<String> userIds;
    private final Collection<String> groupIds;
    private final String role;

    private final EventType eventType;
}
