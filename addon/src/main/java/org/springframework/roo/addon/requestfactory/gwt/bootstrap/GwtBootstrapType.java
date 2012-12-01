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

    public static final GwtBootstrapType MASTER_ACTIVITIES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, false, "", "masterActivities", "ApplicationMasterActivities", false, false, false);
    public static final GwtBootstrapType ACTIVITIES_MAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ActivitiesMapper", "activitiesMapper", "ActivitiesMapper", false, false, false);
    public static final GwtBootstrapType LIST_ACTIVITIES_MAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ListActivitiesMapper", "listActivitiesMapper", "ListActivitiesMapper", false, false, false);
    
    public static final GwtBootstrapType DETAIL_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "DetailsActivity", "detailsActivity", "DetailsActivity", false, true, false);
    public static final GwtBootstrapType DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI, true, "DetailsView", "detailsView", "DetailsView", false, false, false);
    public static final GwtBootstrapType DESKTOP_DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopDetailsView", "desktopDetailsView", "DesktopDetailsView", true, true, false);
    
    public static final GwtBootstrapType EDIT_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "EditActivity", "editActivity", "EditActivity", false, false, false);
    public static final GwtBootstrapType EDIT_ACTIVITY_WRAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "EditActivityWrapper", "editActivityWrapper", "EditActivityWrapper", false, true, false);
    public static final GwtBootstrapType CREATE_ACTIVITY_WRAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "CreateActivityWrapper", "createActivityWrapper", "CreateActivityWrapper", false, false, false);
    public static final GwtBootstrapType EDIT_RENDERER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_RENDERER, true, "ProxyRenderer", "renderer", "EditRenderer", false, false, false);
    public static final GwtBootstrapType EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI, true, "EditView", "editView", "EditView", false, false, false);
    public static final GwtBootstrapType DESKTOP_EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopEditView", "desktopEditView", "DesktopEditView", true, true, false);
    public static final GwtBootstrapType LIST_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ListActivity", "listActivity", "ListActivity", false, true, false);
    public static final GwtBootstrapType LIST_VISUALIZE_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "ListVisualizeActivity", "listVisualizeActivity", "ListVisualizeActivity", false, true, false);
    public static final GwtBootstrapType LIST_EDITOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_EDITOR, true, "ListEditor", "listEditor", "ListEditor", true, true, false);
    public static final GwtBootstrapType LIST_PLACE_RENDERER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_RENDERER, false, "", "listPlaceRenderer", "ApplicationListPlaceRenderer", false, true, false);
    public static final GwtBootstrapType PROXY_PLACE_RENDERER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_RENDERER, false, "", "proxyPlaceRenderer", "ApplicationProxyPlaceRenderer", false, true, false);
    public static final GwtBootstrapType DESKTOP_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopListView", "desktopListView", "DesktopListView", true, true, false);
    public static final GwtBootstrapType DESKTOP_LIST_VISUALIZE_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopListVisualizeView", "desktopListVisualizeView", "DesktopListVisualizeView", true, true, false);
    
    public static final GwtBootstrapType VISUALIZE_ACTIVITY = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, true, "VisualizeActivity", "visualizeActivity", "VisualizeActivity", false, true, false);
    public static final GwtBootstrapType VISUALIZE_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI, true, "VisualizeView", "visualizeView", "VisualizeView", false, false, false);
    public static final GwtBootstrapType DESKTOP_VISUALIZE_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_DESKTOP, true, "DesktopVisualizeView", "desktopVisualizeView", "DesktopVisualizeView", true, true, false);
    
    public static final GwtBootstrapType APPLICATION_MESSAGES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED, false, "", "applicationMessages", "ApplicationMessages", false, false, false);
    public static final GwtBootstrapType PROXY_MESSAGES = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_MESSAGES, true, "Messages", "proxyMessages", "Messages", false, false, false);

    public static final GwtBootstrapType ABSTRACT_DATA_PROVIDER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_PROVIDER, true, "ListDataProvider", "listDataProvider", "ListDataProvider", false, false, false);
    public static final GwtBootstrapType PROXY_DATA_PROVIDER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_PROVIDER, true, "ProxyDataProvider", "proxyDataProvider", "ProxyDataProvider", false, false, false);
    public static final GwtBootstrapType NODE_DATA_PROVIDER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_PROVIDER, true, "NodeDataProvider", "nodeDataProvider", "NodeDataProvider", false, false, false);
    public static final GwtBootstrapType IS_LEAF_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "isLeafProcessor", "IsLeafProcessor", false, false, false);
    public static final GwtBootstrapType PROXY_LIST_NODE_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "proxyListNodeProcessor", "ProxyListNodeProcessor", false, false, false);
    public static final GwtBootstrapType PROXY_NODE_PROCESSOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_TREE, false, "", "proxyNodeProcessor", "ProxyNodeProcessor", false, false, false);

    public static final GwtBootstrapType MOBILE_ACTIVITY_MAPPER = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_ACTIVITY, false, "", "mobileActivityMapper", "MobileActivityMapper", false, false, false);
    public static final GwtBootstrapType MOBILE_DETAILS_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileDetailsView", "mobileDetailsView", "MobileDetailsView", true, true, false);
    public static final GwtBootstrapType MOBILE_EDIT_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileEditView", "mobileEditView", "MobileEditView", true, true, false);
    public static final GwtBootstrapType MOBILE_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileListView", "mobileListView", "MobileListView", false, true, false);
    public static final GwtBootstrapType MOBILE_PROXY_LIST_VIEW = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD_UI, false, "", "mobileProxyListView", "MobileProxyListView", false, false, false);
    public static final GwtBootstrapType MOBILE_VISUALIZE_VIEW = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_MOBILE, true, "MobileVisualizeView", "mobileVisualizeView", "MobileVisualizeView", true, true, false);

    public static final GwtBootstrapType SET_EDITOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_EDITOR, true, "SetEditor", "setEditor", "SetEditor", true, true, false);
    public static final GwtBootstrapType INSTANCE_EDITOR = new GwtBootstrapType(GwtBootstrapPaths.MANAGED_UI_EDITOR, true, "Editor", "editor", "Editor", true, true, false);

    public static final GwtBootstrapType APPLICATION = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "application", "Application", false, false, false);
    public static final GwtBootstrapType DESKTOP_APPLICATION = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "desktopApplication", "DesktopApplication", false, false, false);
    public static final GwtBootstrapType MOBILE_APPLICATION = new GwtBootstrapType(GwtBootstrapPaths.SCAFFOLD, false, "", "mobileApplication", "MobileApplication", false, false, false);

    public static final GwtBootstrapType[] ALL_TYPES = new GwtBootstrapType[] {
        MASTER_ACTIVITIES, ACTIVITIES_MAPPER, LIST_ACTIVITIES_MAPPER,
        DETAIL_ACTIVITY, DETAILS_VIEW, DESKTOP_DETAILS_VIEW,
        EDIT_ACTIVITY, EDIT_ACTIVITY_WRAPPER, CREATE_ACTIVITY_WRAPPER,
        EDIT_RENDERER, EDIT_VIEW, DESKTOP_EDIT_VIEW,
        LIST_ACTIVITY, LIST_VISUALIZE_ACTIVITY, LIST_EDITOR, LIST_PLACE_RENDERER,
        PROXY_PLACE_RENDERER, DESKTOP_LIST_VIEW, DESKTOP_LIST_VISUALIZE_VIEW,
        VISUALIZE_ACTIVITY, VISUALIZE_VIEW, DESKTOP_VISUALIZE_VIEW,
        ABSTRACT_DATA_PROVIDER, PROXY_DATA_PROVIDER, NODE_DATA_PROVIDER, 
        IS_LEAF_PROCESSOR, PROXY_LIST_NODE_PROCESSOR,
        PROXY_NODE_PROCESSOR, MOBILE_ACTIVITY_MAPPER, MOBILE_DETAILS_VIEW,
        MOBILE_EDIT_VIEW, MOBILE_LIST_VIEW, MOBILE_PROXY_LIST_VIEW, MOBILE_VISUALIZE_VIEW,
        SET_EDITOR, INSTANCE_EDITOR, APPLICATION, DESKTOP_APPLICATION, MOBILE_APPLICATION,
        APPLICATION_MESSAGES, PROXY_MESSAGES
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
            for (JavaSymbolName field : proxyFieldTypeMap.keySet()) {
                watchedFieldNames.add(field);
                watchedFieldNames.add(new JavaSymbolName(field.getSymbolName() + "Label"));
            }
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy",
                    "displayRenderer", "messages"));
        } else if (this == MOBILE_DETAILS_VIEW) {
            for (JavaSymbolName field : proxyFieldTypeMap.keySet()) {
                watchedFieldNames.add(field);
                watchedFieldNames.add(new JavaSymbolName(field.getSymbolName() + "Label"));
            }
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy",
                    "displayRenderer", "title", "messages"));
        } else if (this == DESKTOP_VISUALIZE_VIEW) {
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy", "mapWidget"));
        } else if (this == MOBILE_VISUALIZE_VIEW) {
            watchedFieldNames.addAll(convertToJavaSymbolNames("proxy", "mapWidget"));
        } else if (this == DESKTOP_EDIT_VIEW) {
            for (JavaSymbolName field : proxyFieldTypeMap.keySet()) {
                watchedFieldNames.add(field);
                watchedFieldNames.add(new JavaSymbolName(field.getSymbolName() + "Label"));
            }
            watchedFieldNames.addAll(convertToJavaSymbolNames("messages"));
        } else if (this == MOBILE_EDIT_VIEW) {
            for (JavaSymbolName field : proxyFieldTypeMap.keySet()) {
                watchedFieldNames.add(field);
                watchedFieldNames.add(new JavaSymbolName(field.getSymbolName() + "Label"));
            }
            watchedFieldNames.addAll(convertToJavaSymbolNames("messages"));
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
            watchedMethods.put(new JavaSymbolName("initLabels"),
                    Collections.<JavaType> emptyList());
        } else  if (this == MOBILE_DETAILS_VIEW) {
            watchedMethods.put(new JavaSymbolName("setValue"),
                    Collections.singletonList(proxy));
            watchedMethods.put(new JavaSymbolName("initLabels"),
                    Collections.<JavaType> emptyList());
        } else if (this == DESKTOP_VISUALIZE_VIEW) {
            watchedMethods.put(new JavaSymbolName("setValue"),
                    Collections.singletonList(proxy));
        } else  if (this == MOBILE_VISUALIZE_VIEW) {
            watchedMethods.put(new JavaSymbolName("setValue"),
                    Collections.singletonList(proxy));
        } else if (this == DESKTOP_LIST_VISUALIZE_VIEW) {
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
                                    .getSetProviderMethodName()), params);
                }
            }
            watchedMethods.put(new JavaSymbolName("initLabels"),
                    Collections.<JavaType> emptyList());
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
                                    .getSetProviderMethodName()), params);
                }
            }
            watchedMethods.put(new JavaSymbolName("initLabels"),
                    Collections.<JavaType> emptyList());
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
                                    .getSetProviderMethodName()), params);
                }
            }
            watchedMethods.put(new JavaSymbolName("render"), Collections
                    .singletonList(new JavaType(topLevelPackage
                            .getFullyQualifiedPackageName()
                            + ".place.ProxyListPlace")));
        } else if (this == PROXY_PLACE_RENDERER) {
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
                                    .getSetProviderMethodName()), params);
                }
            }
            watchedMethods.put(new JavaSymbolName("render"), Arrays
                    .asList(new JavaType(topLevelPackage
                            .getFullyQualifiedPackageName()
                            + ".place.ProxyPlace"),
                            new JavaType("com.google.gwt.user.client.ui.HasWidgets"),
                            new JavaType("com.google.gwt.user.client.ui.HasText")));
        } else if (this == ACTIVITIES_MAPPER) {
            final List<JavaType> params = new ArrayList<JavaType>();
            params.add(new JavaType(topLevelPackage
                    .getFullyQualifiedPackageName()
                    + ".place.ProxyPlace"));
            watchedMethods.put(new JavaSymbolName("makeEditActivity"), params);
            watchedMethods.put(new JavaSymbolName("coerceId"), params);
            watchedMethods.put(new JavaSymbolName("makeCreateActivity"),
                    Collections.<JavaType>emptyList());
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
            return Collections.<JavaType>emptyList();
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
                    Collections.<JavaType>emptyList());
        } else if (type == VISUALIZE_ACTIVITY) {
            watchedMethods.put(new JavaSymbolName("find"),
                    Arrays.asList(RequestFactoryUtils.getReceiverType(ENTITY_PROXY)));
            watchedMethods.put(new JavaSymbolName("editClicked"),
                    Collections.<JavaType>emptyList());
        } else if (type == MOBILE_LIST_VIEW) {
            watchedMethods.put(new JavaSymbolName("init"),
                    Collections.<JavaType>emptyList());
        } else if (type == DESKTOP_LIST_VIEW) {
            watchedMethods.put(new JavaSymbolName("init"),
                    Collections.<JavaType>emptyList());
        } else if (type == DESKTOP_LIST_VISUALIZE_VIEW) {
        } else if (type == MASTER_ACTIVITIES) {
            watchedMethods.put(new JavaSymbolName("getActivity"),
                    Collections.singletonList(PLACE));
        } else if (type == LIST_ACTIVITY) {
            watchedMethods.put(new JavaSymbolName("fireCountRequest"),
                    Collections.singletonList(RECEIVER));
        } else if (type == LIST_VISUALIZE_ACTIVITY) {
            watchedMethods.put(new JavaSymbolName("find"),
                    Arrays.asList(RequestFactoryUtils.getReceiverType(ENTITY_PROXY)));
        }
        return watchedMethods;
    }

    @Override
    protected List<RequestFactoryType> resolveReferences(final RequestFactoryType type) {
        if (type == ACTIVITIES_MAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APPLICATION, DETAIL_ACTIVITY, VISUALIZE_ACTIVITY,
                    EDIT_ACTIVITY, EDIT_ACTIVITY_WRAPPER, REQUEST);
        } else if (type == LIST_ACTIVITIES_MAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APPLICATION, LIST_ACTIVITY, LIST_VISUALIZE_ACTIVITY,
                    REQUEST);
        } else if (type == DETAIL_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    DETAILS_VIEW, APPLICATION);
        } else if (type == VISUALIZE_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    VISUALIZE_VIEW, APPLICATION);
        } else if (type == EDIT_ACTIVITY) {
            return Arrays.asList(EDIT_VIEW,
                    APP_REQUEST_FACTORY, REQUEST);
        } else if (type == EDIT_ACTIVITY_WRAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    EDIT_VIEW, APPLICATION);
        } else if (type == CREATE_ACTIVITY_WRAPPER) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    EDIT_ACTIVITY_WRAPPER);
        } else if (type == LIST_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    MOBILE_APPLICATION, APPLICATION);
        } else if (type == LIST_VISUALIZE_ACTIVITY) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    DESKTOP_LIST_VISUALIZE_VIEW,
                    MOBILE_APPLICATION, APPLICATION);
        } else if (type == MOBILE_LIST_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    MOBILE_PROXY_LIST_VIEW, MOBILE_APPLICATION});
        } else if (type == DESKTOP_LIST_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    APPLICATION_MESSAGES, PROXY_MESSAGES});
        } else if (type == DESKTOP_LIST_VISUALIZE_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    APPLICATION_MESSAGES, PROXY_MESSAGES});
        } else if (type == DESKTOP_EDIT_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    EDIT_ACTIVITY_WRAPPER, EDIT_VIEW,
                    APPLICATION_MESSAGES, PROXY_MESSAGES});
        } else if (type == MOBILE_EDIT_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    EDIT_ACTIVITY_WRAPPER, EDIT_VIEW,
                    APPLICATION_MESSAGES, PROXY_MESSAGES});
        } else if (type == DESKTOP_DETAILS_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    DETAILS_VIEW, APPLICATION_MESSAGES,
                    PROXY_MESSAGES});
        } else if (type == MOBILE_DETAILS_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    DETAILS_VIEW, APPLICATION_MESSAGES,
                    PROXY_MESSAGES});
        } else if (type == DESKTOP_VISUALIZE_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    VISUALIZE_VIEW, APPLICATION_MESSAGES,
                    PROXY_MESSAGES});
        } else if (type == MOBILE_VISUALIZE_VIEW) {
            return Arrays.asList(new RequestFactoryType[] {
                    VISUALIZE_VIEW, APPLICATION_MESSAGES,
                    PROXY_MESSAGES});
        } else if (type == LIST_PLACE_RENDERER) {
            return Arrays.asList(APP_ENTITY_TYPES_PROCESSOR);
        } else if (type == PROXY_PLACE_RENDERER) {
            return Arrays.asList(APP_ENTITY_TYPES_PROCESSOR, APP_REQUEST_FACTORY);
        } else if (type == MASTER_ACTIVITIES) {
            return Arrays.asList(APP_REQUEST_FACTORY,
                    APP_ENTITY_TYPES_PROCESSOR, APPLICATION);
        } else if (type == ABSTRACT_DATA_PROVIDER) {
            return Arrays.asList(APP_REQUEST_FACTORY);
        } else if (type == PROXY_DATA_PROVIDER) {
            return Arrays.asList(APP_REQUEST_FACTORY);
        } else if (type == NODE_DATA_PROVIDER) {
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
                    "requests", "parentId");
        } else if (type == DETAIL_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests", "proxyId",
                    "placeController", "display", "view", "parentId");
        } else if (type == VISUALIZE_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests", "proxyId",
                    "placeController", "display", "view", "parentId");
        } else if (type == LIST_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests");
        } else if (type == LIST_VISUALIZE_ACTIVITY) {
            watchedFieldNames = convertToJavaSymbolNames("requests", "parentId");
        } else if (type == MOBILE_LIST_VIEW) {
            watchedFieldNames = convertToJavaSymbolNames("paths");
        } else if (type == DESKTOP_LIST_VIEW) {
            watchedFieldNames = convertToJavaSymbolNames("table", "paths");
        } else if (type == DESKTOP_LIST_VISUALIZE_VIEW) {
        } else if (type == MASTER_ACTIVITIES) {
            watchedFieldNames = convertToJavaSymbolNames("requests",
                    "placeController");
        } else if (type == PROXY_PLACE_RENDERER) {
            watchedFieldNames = convertToJavaSymbolNames("requestFactory");
        } else {
            watchedFieldNames = new ArrayList<JavaSymbolName>();
        }
        return watchedFieldNames;
    }

    public boolean isCreateUiXml() {
        return createUiXml;
    }
}
