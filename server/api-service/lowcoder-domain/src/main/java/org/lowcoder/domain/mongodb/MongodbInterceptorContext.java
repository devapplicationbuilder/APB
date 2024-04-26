package org.quickdev.domain.mongodb;

import org.quickdev.domain.encryption.EncryptionService;

public record MongodbInterceptorContext(EncryptionService encryptionService) {
}
