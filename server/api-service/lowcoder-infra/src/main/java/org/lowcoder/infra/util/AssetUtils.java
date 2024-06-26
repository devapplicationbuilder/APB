package org.quickdev.infra.util;

import org.apache.commons.lang3.StringUtils;
import org.quickdev.infra.constant.NewUrl;

public final class AssetUtils {

    public static String toAssetPath(CharSequence assetId) {
        return StringUtils.isBlank(assetId) ? "" : NewUrl.ASSET_URL + "/" + assetId;
    }
}
