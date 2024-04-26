package org.quickdev.sdk.util;

import org.quickdev.sdk.exception.BizError;
import org.quickdev.sdk.exception.BizException;
import org.quickdev.sdk.exception.PluginError;
import org.quickdev.sdk.exception.PluginException;

public class Preconditions {

    public static void check(boolean condition, BizError errorCode, String messageKey, Object... args) {
        if (!condition) {
            throw new BizException(errorCode, messageKey, args);
        }
    }

    public static void check(boolean condition, PluginError errorCode, String messageKey, Object... args) {
        if (!condition) {
            throw new PluginException(errorCode, messageKey, args);
        }
    }
}
