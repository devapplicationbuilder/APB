package org.quickdev.plugin.mssql;

import static org.quickdev.plugin.mssql.util.MssqlStructureParser.parseTableAndColumns;
import static org.quickdev.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;
import static org.quickdev.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quickdev.plugin.mssql.gui.MssqlBulkInsertCommand;
import org.quickdev.plugin.mssql.gui.MssqlBulkUpdateCommand;
import org.quickdev.plugin.mssql.gui.MssqlDeleteCommand;
import org.quickdev.plugin.mssql.gui.MssqlInsertCommand;
import org.quickdev.plugin.mssql.gui.MssqlUpdateCommand;
import org.quickdev.plugin.mssql.util.MssqlResultParser;
import org.quickdev.plugin.sql.GeneralSqlExecutor;
import org.quickdev.plugin.sql.SqlBasedQueryExecutor;
import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.models.DatasourceStructure;
import org.quickdev.sdk.models.DatasourceStructure.Table;
import org.quickdev.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.pf4j.Extension;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Extension
public class MssqlQueryExecutor extends SqlBasedQueryExecutor {


    protected MssqlQueryExecutor() {
        super(new GeneralSqlExecutor() {
            @Override
            protected List<Map<String, Object>> parseDataRows(ResultSet resultSet) throws SQLException {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                List<Map<String, Object>> result = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = MssqlResultParser.parseRowValue(resultSet, metaData, columnCount);
                    result.add(row);
                }
                return result;
            }
        });
    }

    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection, SqlBasedDatasourceConnectionConfig connectionConfig) {
        Map<String, Table> tablesByName = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement()) {
            parseTableAndColumns(tablesByName, statement);
        } catch (SQLException throwable) {
            throw new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR", throwable.getMessage());
        }

        DatasourceStructure structure = new DatasourceStructure(new ArrayList<>(tablesByName.values()));
        for (Table table : structure.getTables()) {
            table.getKeys().sort(Comparator.naturalOrder());
        }
        return structure;
    }

    @Override
    protected GuiSqlCommand parseSqlCommand(String guiStatementType, Map<String, Object> detail) {
        return switch (guiStatementType.toUpperCase()) {
            case "INSERT" -> MssqlInsertCommand.from(detail);
            case "UPDATE" -> MssqlUpdateCommand.from(detail);
            case "DELETE" -> MssqlDeleteCommand.from(detail);
            case "BULK_INSERT" -> MssqlBulkInsertCommand.from(detail);
            case "BULK_UPDATE" -> MssqlBulkUpdateCommand.from(detail);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_COMMAND_TYPE", guiStatementType);
        };
    }
}
