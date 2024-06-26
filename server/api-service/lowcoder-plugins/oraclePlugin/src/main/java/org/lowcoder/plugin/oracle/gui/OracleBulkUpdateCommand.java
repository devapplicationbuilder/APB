package org.quickdev.plugin.oracle.gui;

import static org.quickdev.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand.parseTable;
import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;
import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parsePrimaryKey;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import org.quickdev.sdk.plugin.sqlcommand.command.BulkUpdateCommand;

public class OracleBulkUpdateCommand extends BulkUpdateCommand {

    protected OracleBulkUpdateCommand(String table, BulkObjectChangeSet bulkObjectChangeSet, String primaryKey) {
        super(table, bulkObjectChangeSet, primaryKey, COLUMN_DELIMITER_FRONT);
    }

    public static OracleBulkUpdateCommand from(Map<String, Object> commandDetail) {
        String table = parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new OracleBulkUpdateCommand(table, bulkObjectChangeSet, parsePrimaryKey(commandDetail));
    }

}
