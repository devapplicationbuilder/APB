package org.quickdev.sdk.plugin.sqlcommand.command.postgres;

import static org.quickdev.sdk.plugin.sqlcommand.command.GuiConstants.POSTGRES_COLUMN_DELIMITER;
import static org.quickdev.sdk.util.SqlGuiUtils.POSTGRES_SQL_STR_ESCAPE;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.changeset.ChangeSet;
import org.quickdev.sdk.plugin.sqlcommand.command.InsertCommand;
import org.quickdev.sdk.util.SqlGuiUtils.GuiSqlValue.EscapeSql;

import com.google.common.annotations.VisibleForTesting;

public class PostgresInsertCommand extends InsertCommand {

    private PostgresInsertCommand(Map<String, Object> commandDetail) {
        super(commandDetail, POSTGRES_COLUMN_DELIMITER);
    }

    @VisibleForTesting
    protected PostgresInsertCommand(String table, ChangeSet changeSet) {
        super(table, changeSet, POSTGRES_COLUMN_DELIMITER, POSTGRES_COLUMN_DELIMITER);
    }

    public static PostgresInsertCommand from(Map<String, Object> commandDetail) {
        return new PostgresInsertCommand(commandDetail);
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
