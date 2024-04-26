package org.quickdev.plugin.oracle;

import static org.quickdev.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;
import static org.quickdev.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static org.quickdev.sdk.plugin.common.sql.StructureParser.QUERY_STRUCTURE_SQL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.quickdev.plugin.oracle.gui.OracleBulkInsertCommand;
import org.quickdev.plugin.oracle.gui.OracleBulkUpdateCommand;
import org.quickdev.plugin.oracle.gui.OracleDeleteCommand;
import org.quickdev.plugin.oracle.gui.OracleInsertCommand;
import org.quickdev.plugin.oracle.gui.OracleUpdateCommand;
import org.quickdev.plugin.sql.GeneralSqlExecutor;
import org.quickdev.plugin.sql.SqlBasedQueryExecutor;
import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.models.DatasourceStructure;
import org.quickdev.sdk.models.DatasourceStructure.Table;
import org.quickdev.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import org.quickdev.sdk.plugin.common.sql.StructureParser;
import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.pf4j.Extension;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Extension
public class OracleQueryExecutor extends SqlBasedQueryExecutor {

    public OracleQueryExecutor() {
        super(new GeneralSqlExecutor());
    }

    @Nonnull
    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection,
            SqlBasedDatasourceConnectionConfig connectionConfig) {
        try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(QUERY_STRUCTURE_SQL)) {
            List<Table> tables = StructureParser.parseColumns(resultSet);
            return new DatasourceStructure(tables);
        } catch (SQLException throwable) {
            throw new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR",
                    throwable.getMessage());
        }
    }

    @Override
    protected GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail) {
        return switch (guiStatementType.toUpperCase()) {
            case "INSERT" -> OracleInsertCommand.from(detail);
            case "UPDATE" -> OracleUpdateCommand.from(detail);
            case "DELETE" -> OracleDeleteCommand.from(detail);
            case "BULK_INSERT" -> OracleBulkInsertCommand.from(detail);
            case "BULK_UPDATE" -> OracleBulkUpdateCommand.from(detail);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_COMMAND_TYPE", guiStatementType);
        };
    }

}
