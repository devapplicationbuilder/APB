package org.quickdev.domain.mongodb;

public interface BeforeMongodbWrite {

    void beforeMongodbWrite(MongodbInterceptorContext context);
}
