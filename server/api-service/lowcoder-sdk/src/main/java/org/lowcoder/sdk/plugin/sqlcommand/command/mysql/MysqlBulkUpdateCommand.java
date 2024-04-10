package org.quickdev.sdk.plugin.sqlcommand.command.mysql;

import static org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand.parseTable;
import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parsePrimaryKey;
import static org.quickdev.sdk.plugin.sqlcommand.command.GuiConstants.MYSQL_COLUMN_DELIMITER;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import org.quickdev.sdk.plugin.sqlcommand.command.BulkUpdateCommand;

public class MysqlBulkUpdateCommand extends BulkUpdateCommand {

    protected MysqlBulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet,
            String primaryKey) {
        super(table, bulkObjectChangeSet, primaryKey, MYSQL_COLUMN_DELIMITER, MYSQL_COLUMN_DELIMITER);
    }

    public static MysqlBulkUpdateCommand from(Map<String, Object> commandDetail) {
        String table = parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new MysqlBulkUpdateCommand(table, bulkObjectChangeSet, parsePrimaryKey(commandDetail));
    }

}
