package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ACCEPTS_ONE_WIDGET;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ENTITY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.EVENT_BUS;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.PLACE;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.RECEIVER;
import static org.springframework.roo.model.JdkJavaType.COLLECTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public class GwtBootstrapType extends RequestFactoryType {

    public static final GwtBootstrapType ACTIVITIES_MAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ActivitiesMapper", "activitiesMapper", "ActivitiesMapper", false, true, false);

    public static final GwtBootstrapType DETAIL_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "DetailsActivity", "detailsActivity", "DetailsActivity", false, true, false);
    public static final GwtBootstrapType DETAILS_ACTIVITIES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, false, "", "detailsActivities", "ApplicationDetailsActivities", false, true, false);
    public static final GwtBootstrapType DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI, true, "DetailsView", "detailsView", "DetailsView", false, false, false);
    public static final GwtBootstrapType DESKTOP_DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopDetailsView", "desktopDetailsView", "DesktopDetailsView", true, true, false);
    public static final GwtBootstrapType EDIT_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "EditActivity", "editActivity", "EditActivity", false, false, false);
    public static final GwtBootstrapType EDIT_ACTIVITY_WRAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "EditActivityWrapper", "editActivityWrapper", "EditActivityWrapper", false, true, false);
    public static final GwtBootstrapType EDIT_RENDERER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_RENDERER, true, "ProxyRenderer", "renderer", "EditRenderer", false, false, false);
    public static final GwtBootstrapType EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI, true, "EditView", "editView", "EditView", false, false, false);
    public static final GwtBootstrapType DESKTOP_EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopEditView", "desktopEditView", "DesktopEditView", true, true, false);
    public static final GwtBootstrapType IS_SCAFFOLD_MOBILE_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD_ACTIVITY, false, "", "isScaffoldMobileActivity", "IsScaffoldMobileActivity", false, false, false);
    public static final GwtBootstrapType LIST_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ListActivity", "listActivity", "ListActivity", false, true, false);
    public static final GwtBootstrapType LIST_EDITOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_EDITOR, true, "ListEditor", "listEditor", "ListEditor", true, true, false);
    public static final GwtBootstrapType LIST_PLACE_RENDERER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_RENDERER, false, "", "listPlaceRenderer", "ApplicationListPlaceRenderer", false, true, false);
    public static final GwtBootstrapType DESKTOP_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopListView", "desktopListView", "DesktopListView", true, true, false);
    public static final GwtBootstrapType MASTER_ACTIVITIES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, false, "", "masterActivities", "ApplicationMasterActivities", false, true, false);

    public static final GwtBootstrapType DATA_PROVIDER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, true, "DataProvider", "dataProvider", "DataProvider", false, false, false);
    public static final GwtBootstrapType IS_LEAF_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "isLeafProcessor", "IsLeafProcessor", false, false, false);
    public static final GwtBootstrapType PROXY_LIST_NODE_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "proxyListNodeProcessor", "ProxyListNodeProcessor", false, false, false);
    public static final GwtBootstrapType PROXY_NODE_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "proxyNodeProcessor", "ProxyNodeProcessor", false, false, false);

    public static final GwtBootstrapType MOBILE_ACTIVITIES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, false, "", "mobileActivities", "ScaffoldMobileActivities", false, false, false);
    public static final GwtBootstrapType MOBILE_DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileDetailsView", "mobileDetailsView", "MobileDetailsView", true, true, false);
    public static final GwtBootstrapType MOBILE_EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileEditView", "mobileEditView", "MobileEditView", true, true, false);
    public static final GwtBootstrapType MOBILE_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileListView", "mobileListView", "MobileListView", false, true, false);
    public static final GwtBootstrapType MOBILE_PROXY_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD_UI, false, "", "mobileProxyListView", "MobileProxyListView", false, false, false);

    public static final GwtBootstrapType SET_EDITOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_EDITOR, true, "SetEditor", "setEditor", "SetEditor", true, true, false);

    public static final GwtBootstrapType SCAFFOLD_APP = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "scaffoldApp", "ScaffoldApp", false, false, false);
    public static final GwtBootstrapType SCAFFOLD_DESKTOP_APP = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "scaffoldDesktopApp", "ScaffoldDesktopApp", false, false, false);
    public static final GwtBootstrapType SCAFFOLD_MOBILE_APP = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "scaffoldMobileApp", "ScaffoldMobileApp", false, false, false);

    public static final GwtBootstrapType[] ALL_TYPES = new GwtBootstrapType[] {
        ACTIVITIES_MAPPER, DETAIL_ACTIVITY, DETAILS_ACTIVITIES, DETAILS_VIEW,
        DESKTOP_DETAILS_VIEW, EDIT_ACTIVITY, EDIT_ACTIVITY_WRAPPER,
        EDIT_RENDERER, EDIT_VIEW, DESKTOP_EDIT_VIEW, IS_SCAFFOLD_MOBILE_ACTIVITY,
        LIST_ACTIVITY, LIST_EDITOR, LIST_PLACE_RENDERER, DESKTOP_LIST_VIEW,
        MASTER_ACTIVITIES, DATA_PROVIDER, IS_LEAF_PROCESSOR, PROXY_LIST_NODE_PROCESSOR,
        PROXY_NODE_PROCESSOR, MOBILE_ACTIVITIES, MOBILE_DETAILS_VIEW,
        MOBILE_EDIT_VIEW, MOBILE_LIST_VIEW, MOBILE_PROXY_LIST_VIEW,
        SET_EDITOR, SCAFFOLD_APP, SCAFFOLD_DESKTOP_APP, SCAFFOLD_MOBILE_APP
    };

    public static List<GwtBootstrapType> getGwtBootstrapMirrorTypes() {
        final List<GwtBootstrapType> mirrorTypes = new ArrayList<GwtBootstrapType>();
        for (final GwtBootstrapType requestFactoryType : GwtBootstrapType.ALL_TYPES) {
            if (requestFactoryType.isMirrorType()) {
                mirrorTypes.add(requestFactoryType);
            }
        }
        return mirrorTypes;
    }

    protected final boolean createUiXml;

    public GwtBootstrapType(final RequestFactoryPath path, final boolean mirrorType,
            final String suffix, final String name, final String template,
            final boolean createUiXml, final boolean createAbstract,
            final boolean overwriteConcrete) {
        super(path, mirrorType, suffix, name, template, createAbstract, overwriteConcrete);
        this.createUiXml = createUiXml;
    }

    @Override
    public void dynamicallyResolveFieldsToWatch(
            final Map<JavaSymbolName, RequestFactoryProxyProperty> proxyFieldTypeMap) {
        super.dynamicallyResolveFieldsToWatch(proxyFieldTypeMap);
        if (this == DESKTOP_DETAILS_VIEW) {
            watchedFieldNames.addAll(proxyFieldTypeMap.keySet());
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy",
                    "displayRenderer"));
        } else if (this == MOBILE_DETAILS_VIEW) {
            watchedFieldNames.addAll(proxyFieldTypeMap.keySet());
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy",
                    "displayRenderer"));
        } else if (this == DESKTOP_EDIT_VIEW) {
            watchedFieldNames.addAll(proxyFieldTypeMap.keySet());
        } else if (this == MOBILE_EDIT_VIEW) {
            watchedFieldNames.addAll(proxyFieldTypeMap.keySet());
        }
    }

    @Override
    public void dynamicallyResolveMethodsToWatch(final JavaType proxy,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> proxyFieldTypeMap,
            final JavaPackage topLevelPackage) {
        super.dynamicallyResolveMethodsToWatch(proxy, proxyFieldTypeMap, topLevelPackage);

        if (this == DESKTOP_DETAILS_VIEW) {
            watchedMethods.put(new JavaSymbolName("setValue"),
                    Collections.singletonList(proxy));
        } else  if (this == MOBILE_DETAILS_VIEW) {
            watchedMethods.put(new JavaSymbolName("setValue"),
                    Collections.singletonList(proxy));
        } else if (this == DESKTOP_EDIT_VIEW) {
            for (final RequestFactoryProxyProperty property : proxyFieldTypeMap.values()) {
                if (property.isEnum() || property.isProxy()
                        || property.isEmbeddable()
                        || property.isCollectionOfProxy()) {
                    final List<JavaType> params = new ArrayList<JavaType>();
                    final JavaType param = new JavaType(
                            COLLECTION.getFullyQualifiedTypeName(), 0,
                            DataType.TYPE, null,
                            Collections.singletonList(property.getValueType()));
                    params.add(param);
                    watchedMethods.put(
                            new JavaSymbolName(property
                                    .getSetValuePickerMethodName()), params);
                }
            }
        } else if (this == MOBILE_EDIT_VIEW) {
            for (final RequestFactoryProxyProperty property : proxyFieldTypeMap.values()) {
                if (property.isEnum() || property.isProxy()
                        || property.isEmbeddable()
                        || property.isCollectionOfProxy()) {
                    final List<JavaType> params = new ArrayList<JavaType>();
                    final JavaType param = new JavaType(
                            COLLECTION.getFullyQualifiedTypeName(), 0,
                            DataType.TYPE, null,
                            Collections.singletonList(property.getValueType()));
                    params.add(param);
                    watchedMethods.put(
                            new JavaSymbolName(property
                                    .getSetValuePickerMethodName()), params);
                }
            }
        } else if (this == LIST_PLACE_RENDERER) {
            for (final RequestFactoryProxyProperty property : proxyFieldTypeMap.values()) {
                if (property.isEnum() || property.isProxy()
                        || property.isEmbeddable()
                        || property.isCollectionOfProxy()) {
                    final List<JavaType> params = new ArrayList<JavaType>();
                    final JavaType param = new JavaType(
                            COLLECTION.getFullyQualifiedTypeName(), 0,
                            DataType.TYPE, null,
                            Collections.singletonList(property.getValueType()));
                    params.add(param);
                    watchedMethods.put(
                            new JavaSymbolName(property
                                    .getSetValuePickerMethodName()), params);
                }
            }
            watchedMethods.put(new JavaSymbolName("render"), Collections
                    .singletonList(new JavaType(topLevelPackage
                            .getFullyQualifiedPackageName()
                            + ".client.scaffold.place.ProxyListPlace")));
        } else if (this == ACTIVITIES_MAPPER) {
            final List<JavaType> params = new ArrayList<JavaType>();
            params.add(new JavaType(topLevelPackage
                    .getFullyQualifiedPackageName()
                    + ".client.scaffold.place.ProxyPlace"));
            watchedMethods.put(new JavaSymbolName("makeEditActivity"), params);
            watchedMethods.put(new JavaSymbolName("coerceId"), params);
            watchedMethods.put(new JavaSymbolName("makeCreateActivity"),
                    new ArrayList<JavaType>());
        } else if (this == EDIT_RENDERER) {
            watchedMethods.put(new JavaSymbolName("render"),
                    Collections.singletonList(proxy));
        }
    }

    @Override
    protected List<JavaType> resolveInnerTypesToWatch(final RequestFactoryType type) {
        if (type == EDIT_ACTIVITY_WRAPPER) {
            return Arrays.asList(new JavaType("View"));
        } else {
            return new ArrayList<JavaType>();
        }
    }

    @Override
    public Map<JavaSymbolName, List<JavaType>> resolveMethodsToWatch(
            final RequestFactoryType type) {
        super.resolveMethodsToWatch(type);
        if (type == EDIT_ACTIVITY_WRAPPER) {
            watchedMethods.put(new JavaSymbolName("start"),
                    Arrays.asList(ACCEPTS_ONE_WIDGET, EVENT_BUS));
        } else if (type == DETAIL_ACTIVITY) {
            watchedMethods.put(new JavaSymbolName("find"),
                    Arrays.asList(RequestFactoryUtils.getReceiverType(ENTITY_PROXY)));
            watchedMethods.put(new JavaSymbolName("deleteClicked"),
                    new ArrayList<JavaType>());
        } else if (type == MOBILE_LIST_VIEW) {
            watchedMethods.put(new JavaSymbolName("init"),
                    new ArrayList<JavaType>());
        } else if (type == DESKTOP_LIST_VIEW) {
            watchedMethods.put(new JavaSymbolName("init"),
                    new ArrayList<JavaType>());
        } else if (type == MASTER_ACTIVITIES) {
            watchedMethods.put(new JavaSymbolName("getActivity"),
                    Collections.singletonList(PLACE));
        } else if (type == DETAILS_ACTIVITIES) {
            watchedMethods.put(new JavaSymbolName("getActivity"),
                    Collections.singletonList(PLACE));
        } else if (type == LIST_ACTIVITY) {
            watchedMethods.put(new JavaSymbolName("fireCountRequest"),
                    Collections.singletonList(RECEIVER));
        }
        return watchedMethods;
    }

    @Override
    protected List<RequestFactoryType> resolveReferences(final RequestFactoryType type) {
        if (type == ACTIVITIES_MAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    SCAFFOLD_APP, DETAIL_ACTIVITY,
                    EDIT_ACTIVITY, EDIT_ACTIVITY_WRAPPER,
                    DESKTOP_LIST_VIEW, DESKTOP_DETAILS_VIEW,
                    MOBILE_DETAILS_VIEW, DESKTOP_EDIT_VIEW,
                    MOBILE_EDIT_VIEW, REQUEST);
        } else if (type == DETAIL_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    IS_SCAFFOLD_MOBILE_ACTIVITY, DETAILS_VIEW);
        } else if (type == EDIT_ACTIVITY) {
            return Arrays.asList(EDIT_VIEW,
                    APP_REQUEST_FACTORY, REQUEST);
        } else if (type == EDIT_ACTIVITY_WRAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    IS_SCAFFOLD_MOBILE_ACTIVITY, EDIT_VIEW);
        } else if (type == LIST_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    IS_SCAFFOLD_MOBILE_ACTIVITY,
                    SCAFFOLD_MOBILE_APP);
        } else if (type == MOBILE_LIST_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {MOBILE_PROXY_LIST_VIEW,
                    SCAFFOLD_MOBILE_APP});
        } else if (type == DESKTOP_EDIT_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {EDIT_ACTIVITY_WRAPPER,
                    EDIT_VIEW});
        } else if (type == MOBILE_EDIT_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {EDIT_ACTIVITY_WRAPPER,
                    EDIT_VIEW});
        } else if (type == DESKTOP_DETAILS_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {DETAILS_VIEW});
        } else if (type == MOBILE_DETAILS_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {DETAILS_VIEW});
        } else if (type == LIST_PLACE_RENDERER) {
            return Arrays.asList(APP_ENTITY_TYPES_PROCESSOR);
        } else if (type == MASTER_ACTIVITIES) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APP_ENTITY_TYPES_PROCESSOR, SCAFFOLD_APP);
        } else if (type == DETAILS_ACTIVITIES) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APP_ENTITY_TYPES_PROCESSOR);
        } else if (type == DATA_PROVIDER) {
            return Arrays.asList(APP_REQUEST_FACTORY);
        } else if (type == PROXY_NODE_PROCESSOR) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APP_ENTITY_TYPES_PROCESSOR);
        } else if (type == PROXY_LIST_NODE_PROCESSOR) {
            return Arrays.asList(APP_ENTITY_TYPES_PROCESSOR,
                    LIST_PLACE_RENDERER);
        } else if (type == IS_LEAF_PROCESSOR) {
            return Arrays.asList(APP_ENTITY_TYPES_PROCESSOR);
        } else {
            return new ArrayList<RequestFactoryType>();
        }
    }

    @Override
    public List<JavaSymbolName> resolveWatchedFieldNames(final RequestFactoryType type) {
        watchedFieldNames = super.resolveWatchedFieldNames(type);
        if (type == ACTIVITIES_MAPPER) {
            watchedFieldNames = convertToJavaSymbolNames("factory",
                    "placeController");
        } else if (type == EDIT_ACTIVITY_WRAPPER) {
            watchedFieldNames = convertToJavaSymbolNames("wrapped", "view",
                    "requests");
        } else if (type == DETAIL_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests", "proxyId",
                    "placeController", "display", "view");
        } else if (type == LIST_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests");
        } else if (type == MOBILE_LIST_VIEW) {
            watchedFieldNames = convertToJavaSymbolNames("paths");
        } else if (type == DESKTOP_LIST_VIEW) {
            watchedFieldNames = convertToJavaSymbolNames("table", "paths");
        } else if (type == MASTER_ACTIVITIES) {
            watchedFieldNames = convertToJavaSymbolNames("requests",
                    "placeController");
        } else if (type == DETAILS_ACTIVITIES) {
            watchedFieldNames = convertToJavaSymbolNames("requests",
                    "placeController");
        } else {
            watchedFieldNames = new ArrayList<JavaSymbolName>();
        }
        return watchedFieldNames;
    }

    public boolean isCreateUiXml() {
        return createUiXml;
    }
}
