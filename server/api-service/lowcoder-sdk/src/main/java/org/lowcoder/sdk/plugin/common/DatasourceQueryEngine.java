package org.quickdev.sdk.plugin.common;

import org.quickdev.sdk.models.DatasourceConnectionConfig;
import org.quickdev.sdk.query.QueryExecutionContext;

/**
 * This interface is responsible for:
 * 1. datasource config parsing & validation
 * 2. connection's life cycle management: creation/destroy/test
 * 3. query context building and execution(structure can be seen as a special case)
 */
public interface DatasourceQueryEngine<DatasourceConfig extends DatasourceConnectionConfig, Connection, Context extends QueryExecutionContext>
        extends DatasourceConnector<Connection, DatasourceConfig>, QueryExecutor<DatasourceConfig, Connection, Context> {

}
