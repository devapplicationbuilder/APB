package org.quickdev.domain.mongodb;

public interface AfterMongodbRead {

    void afterMongodbRead(MongodbInterceptorContext context);
}
