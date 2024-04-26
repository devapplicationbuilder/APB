package org.quickdev.sdk.plugin.sqlcommand.command.postgres;

import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static org.quickdev.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static org.quickdev.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import org.quickdev.sdk.plugin.sqlcommand.command.BulkInsertCommand;
import org.quickdev.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

public class PostgresBulkInsertCommand extends BulkInsertCommand {
    protected PostgresBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new PostgresBulkInsertCommand(table, bulkObjectChangeSet);
    }

    @Override
    public boolean isRenderWithRawSql() {
        return true;
    }

    @Override
    public EscapeSql escapeStrFunc() {
        return POSTGRES_SQL_STR_ESCAPE;
    }
}
