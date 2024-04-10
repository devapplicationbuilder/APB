package org.quickdev.sdk.plugin.sqlcommand.changeset;

import org.quickdev.sdk.util.SqlGuiUtils.GuiSqlValue;

public record ChangeSetItem(String column, GuiSqlValue guiSqlValue) {
}