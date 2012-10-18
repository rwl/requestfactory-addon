package org.springframework.roo.addon.requestfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public class RequestFactoryType {

    public static final RequestFactoryType APP_ENTITY_TYPES_PROCESSOR = new RequestFactoryType(
            RequestFactoryPath.MANAGED_REQUEST, false, "", "entityTypes",
            "ApplicationEntityTypesProcessor", false, true);

    public static final RequestFactoryType APP_REQUEST_FACTORY = new RequestFactoryType(
            RequestFactoryPath.MANAGED_REQUEST, false, "", "requestFactory",
            "ApplicationRequestFactory", false, true);

    public static final RequestFactoryType PROXY = new RequestFactoryType(
            RequestFactoryPath.MANAGED_REQUEST, true, "Proxy", "proxy", null, false, true);

    public static final RequestFactoryType REQUEST = new RequestFactoryType(
            RequestFactoryPath.MANAGED_REQUEST, true, "Request", "request", null, false, true);

    public static final RequestFactoryType[] ALL_TYPES = new RequestFactoryType[] {
        APP_ENTITY_TYPES_PROCESSOR, APP_REQUEST_FACTORY, PROXY, REQUEST
    };

    public static List<RequestFactoryType> getRequestFactoryMirrorTypes() {
        final List<RequestFactoryType> mirrorTypes = new ArrayList<RequestFactoryType>();
        for (final RequestFactoryType requestFactoryType : RequestFactoryType.ALL_TYPES) {
            if (requestFactoryType.isMirrorType()) {
                mirrorTypes.add(requestFactoryType);
            }
        }
        return mirrorTypes;
    }

    protected boolean createAbstract = false;
    protected boolean mirrorType = false;
    protected final String name;
    protected boolean overwriteConcrete = false;
    protected final RequestFactoryPath path;
    protected final String suffix;
    protected final String template;
    protected List<JavaSymbolName> watchedFieldNames = new ArrayList<JavaSymbolName>();

    protected Map<JavaSymbolName, List<JavaType>> watchedMethods = new LinkedHashMap<JavaSymbolName, List<JavaType>>();

    public RequestFactoryType(final RequestFactoryPath path, final boolean mirrorType,
            final String suffix, final String name, final String template,
            final boolean createAbstract, final boolean overwriteConcrete) {
        this.path = path;
        this.mirrorType = mirrorType;
        this.suffix = suffix;
        this.name = name;
        this.template = template;
        this.createAbstract = createAbstract;
        this.overwriteConcrete = overwriteConcrete;
    }

    protected List<JavaSymbolName> convertToJavaSymbolNames(final String... names) {
        final List<JavaSymbolName> javaSymbolNames = new ArrayList<JavaSymbolName>();
        for (final String name : names) {
            if (!javaSymbolNames.contains(new JavaSymbolName(name))) {
                javaSymbolNames.add(new JavaSymbolName(name));
            }
        }
        return javaSymbolNames;
    }

    public void dynamicallyResolveFieldsToWatch(
            final Map<JavaSymbolName, RequestFactoryProxyProperty> proxyFieldTypeMap) {
        watchedFieldNames = resolveWatchedFieldNames(this);
    }

    public void dynamicallyResolveMethodsToWatch(final JavaType proxy,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> proxyFieldTypeMap,
            final JavaPackage topLevelPackage) {
        watchedMethods = resolveMethodsToWatch(this);
    }

    public String getName() {
        return name;
    }

    public RequestFactoryPath getPath() {
        return path;
    }

    public List<RequestFactoryType> getReferences() {
        return resolveReferences(this);
    }

    public String getSuffix() {
        return suffix;
    }

    public String getTemplate() {
        return template;
    }

    public List<JavaSymbolName> getWatchedFieldNames() {
        return watchedFieldNames;
    }

    public List<JavaType> getWatchedInnerTypes() {
        return resolveInnerTypesToWatch(this);
    }

    public Map<JavaSymbolName, List<JavaType>> getWatchedMethods() {
        return watchedMethods;
    }

    public boolean isCreateAbstract() {
        return createAbstract;
    }

    public boolean isMirrorType() {
        return mirrorType;
    }

    public boolean isOverwriteConcrete() {
        return overwriteConcrete;
    }

    protected List<JavaType> resolveInnerTypesToWatch(final RequestFactoryType type) {
        return new ArrayList<JavaType>();
    }

    public Map<JavaSymbolName, List<JavaType>> resolveMethodsToWatch(
            final RequestFactoryType type) {
        watchedMethods = new HashMap<JavaSymbolName, List<JavaType>>();
        return watchedMethods;
    }

    protected List<RequestFactoryType> resolveReferences(final RequestFactoryType type) {
        return new ArrayList<RequestFactoryType>();
    }

    public List<JavaSymbolName> resolveWatchedFieldNames(final RequestFactoryType type) {
        watchedFieldNames = new ArrayList<JavaSymbolName>();
        return watchedFieldNames;
    }

    public void setWatchedMethods(final Map<JavaSymbolName,
            List<JavaType>> watchedMethods) {
        this.watchedMethods = watchedMethods;
    }
}
