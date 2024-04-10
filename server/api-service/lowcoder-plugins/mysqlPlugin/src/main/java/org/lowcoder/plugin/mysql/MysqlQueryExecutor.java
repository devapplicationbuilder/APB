package org.quickdev.plugin.mysql;

import static org.quickdev.plugin.mysql.utils.MysqlStructureParser.parseTableAndColumns;
import static org.quickdev.plugin.mysql.utils.MysqlStructureParser.parseTableKeys;
import static org.quickdev.sdk.exception.PluginCommonError.DATASOURCE_GET_STRUCTURE_ERROR;
import static org.quickdev.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.quickdev.plugin.sql.GeneralSqlExecutor;
import org.quickdev.plugin.sql.SqlBasedQueryExecutor;
import org.quickdev.sdk.exception.PluginException;
import org.quickdev.sdk.models.DatasourceStructure;
import org.quickdev.sdk.models.DatasourceStructure.Table;
import org.quickdev.sdk.plugin.common.sql.SqlBasedDatasourceConnectionConfig;
import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlBulkInsertCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlBulkUpdateCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlDeleteCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlInsertCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlUpdateCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.mysql.MysqlUpsertCommand;
import org.pf4j.Extension;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Extension
public class MysqlQueryExecutor extends SqlBasedQueryExecutor {

    public MysqlQueryExecutor() {
        super(new GeneralSqlExecutor());
    }

    @Nonnull
    @Override
    protected DatasourceStructure getDatabaseMetadata(Connection connection,
            SqlBasedDatasourceConnectionConfig connectionConfig) {
        Map<String, Table> tablesByName = new LinkedHashMap<>();
        try (Statement statement = connection.createStatement()) {
            parseTableAndColumns(tablesByName, statement);
            parseTableKeys(tablesByName, statement);
        } catch (SQLException throwable) {
            throw new PluginException(DATASOURCE_GET_STRUCTURE_ERROR, "DATASOURCE_GET_STRUCTURE_ERROR",
                    throwable.getMessage());
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
            case "INSERT" -> MysqlInsertCommand.from(detail);
            case "UPDATE" -> MysqlUpdateCommand.from(detail);
            case "UPSERT" -> MysqlUpsertCommand.from(detail);
            case "DELETE" -> MysqlDeleteCommand.from(detail);
            case "BULK_INSERT" -> MysqlBulkInsertCommand.from(detail);
            case "BULK_UPDATE" -> MysqlBulkUpdateCommand.from(detail);
            default -> throw new PluginException(QUERY_ARGUMENT_ERROR, "INVALID_GUI_COMMAND_TYPE", guiStatementType);
        };
    }

}
