package org.quickdev.plugin.oracle.gui;

import static org.quickdev.plugin.oracle.gui.GuiConstants.COLUMN_DELIMITER_FRONT;
import static org.quickdev.sdk.plugin.sqlcommand.filter.FilterSet.parseFilterSet;

import java.util.Map;

import org.quickdev.sdk.plugin.sqlcommand.GuiSqlCommand;
import org.quickdev.sdk.plugin.sqlcommand.command.DeleteCommand;
import org.quickdev.sdk.plugin.sqlcommand.filter.FilterSet;
import org.quickdev.sdk.plugin.sqlcommand.filter.FilterSet.RawFilterCondition;

public class OracleDeleteCommand extends DeleteCommand {

    protected OracleDeleteCommand(String table, FilterSet filterSet, boolean allowMultiModify) {
        super(table, filterSet, allowMultiModify, COLUMN_DELIMITER_FRONT);
    }

    public static DeleteCommand from(Map<String, Object> commandDetail) {
        String table = GuiSqlCommand.parseTable(commandDetail);
        FilterSet filterSet = parseFilterSet(commandDetail);
        boolean allowMultiModify = GuiSqlCommand.parseAllowMultiModify(commandDetail);
        return new OracleDeleteCommand(table, filterSet, allowMultiModify);
    }

    @Override
    public GuiSqlCommandRenderResult render(Map<String, Object> requestMap) {
        if (!allowMultiModify) {
            filterSet.addCondition(new RawFilterCondition("rownum", "=", 1));
        }

        return super.render(requestMap);
    }

    @Override
    protected void renderLimit(StringBuilder sb) {
        // do nothing
    }
}
