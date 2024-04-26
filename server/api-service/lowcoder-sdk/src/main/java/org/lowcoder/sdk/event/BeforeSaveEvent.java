package org.quickdev.sdk.event;

public record BeforeSaveEvent<T>(T source) {
}
