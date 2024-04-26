package org.quickdev.plugin.oracle.gui;

import static org.quickdev.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet.parseBulkRecords;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.quickdev.sdk.plugin.sqlcommand.changeset.BulkObjectChangeSet;
import org.quickdev.sdk.plugin.sqlcommand.command.BulkInsertCommand;

public class OracleBulkInsertCommand extends BulkInsertCommand {
    protected OracleBulkInsertCommand(String table, BulkObjectChangeSet bulkObjectChangeSet) {
        super(table, bulkObjectChangeSet, COLUMN_DELIMITER_FRONT);
    }

    public static BulkInsertCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        String recordStr = parseBulkRecords(commandDetail);
        BulkObjectChangeSet bulkObjectChangeSet = new BulkObjectChangeSet(recordStr);
        return new OracleBulkInsertCommand(table, bulkObjectChangeSet);
    }
}
