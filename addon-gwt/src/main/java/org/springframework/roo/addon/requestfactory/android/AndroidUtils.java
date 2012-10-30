package org.springframework.roo.addon.requestfactory.android;

import java.util.HashMap;
import java.util.Map;

import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

public final class AndroidUtils {

    public static Map<RequestFactoryType, JavaType> getMirrorTypeMap(
            final JavaType governorType, final JavaPackage topLevelPackage) {
        final Map<RequestFactoryType, JavaType> mirrorTypeMap = new HashMap<RequestFactoryType, JavaType>();
        for (final RequestFactoryType mirrorType : AndroidType.ALL_TYPES) {
            mirrorTypeMap.put(
                    mirrorType,
                    RequestFactoryUtils.convertGovernorTypeNameIntoKeyTypeName(governorType,
                            mirrorType, topLevelPackage));
        }
        return mirrorTypeMap;
    }

    private AndroidUtils() {
    }
}
