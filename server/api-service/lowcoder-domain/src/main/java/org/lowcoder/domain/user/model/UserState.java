package org.quickdev.domain.user.model;

public enum UserState {
    NEW, INVITED, ACTIVATED,
    DELETED// User can only be deleted in enterprise mode.
}
