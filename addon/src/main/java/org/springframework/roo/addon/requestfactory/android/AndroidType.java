package org.springframework.roo.addon.requestfactory.android;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;

public class AndroidType extends RequestFactoryType {

    public static final AndroidType PROXY_DETAIL_ACTIVITY = new AndroidType(AndroidPaths.ACTIVITY, true, "ProxyDetailActivity", "proxyDetailActivity", "ProxyDetailActivity", Arrays.asList("proxy_detail_activity_view"), false, false, true);
    public static final AndroidType PROXY_DETAIL_FRAGMENT = new AndroidType(AndroidPaths.FRAGMENT, true, "ProxyDetailFragment", "proxyDetailFragment", "ProxyDetailFragment", Arrays.asList("proxy_detail_fragment_view"), false, false, true);
    public static final AndroidType PROXY_LIST_ACTIVITY = new AndroidType(AndroidPaths.ACTIVITY, true, "ProxyListActivity", "proxyListActivity", "ProxyListActivity", Arrays.asList("proxy_list_activity_view", "proxy_list_activity_twopane"), false, false, true);
    public static final AndroidType PROXY_LIST_FRAGMENT = new AndroidType(AndroidPaths.FRAGMENT, true, "ProxyListFragment", "proxyListFragment", "ProxyListFragment", Collections.<String>emptyList(), false, false, false);

    public static final AndroidType PROXY_ARRAY_ADAPTER = new AndroidType(AndroidPaths.ADAPTER, true, "ProxyArrayAdapter", "proxyArrayAdapter", "ProxyArrayAdapter", Arrays.asList("proxy_listview_item_row"), false, false, true);

    public static final AndroidType LIST_ACTIVITY_PROCESSOR = new AndroidType(AndroidPaths.PROCESSOR, false, "ListActivityProcessor", "listActivityProcessor", "ListActivityProcessor", Collections.<String>emptyList(), false, true, false);
    public static final AndroidType PLURAL_PROCESSOR = new AndroidType(AndroidPaths.PROCESSOR, false, "PluralProcessor", "pluralProcessor", "PluralProcessor", Collections.<String>emptyList(), false, true, false);

    public static final AndroidType[] ALL_TYPES = new AndroidType[] {
        PROXY_DETAIL_ACTIVITY, PROXY_DETAIL_FRAGMENT, PROXY_LIST_ACTIVITY,
        PROXY_LIST_FRAGMENT, PROXY_ARRAY_ADAPTER, LIST_ACTIVITY_PROCESSOR,
        PLURAL_PROCESSOR
    };

    public static List<AndroidType> getAndroidMirrorTypes() {
        final List<AndroidType> mirrorTypes = new ArrayList<AndroidType>();
        for (final AndroidType androidType : AndroidType.ALL_TYPES) {
            if (androidType.isMirrorType()) {
                mirrorTypes.add(androidType);
            }
        }
        return mirrorTypes;
    }

    protected final List<String> viewTemplates;
    
    protected final boolean createViewXml;

    public AndroidType(final RequestFactoryPath path,
            final boolean mirrorType, final String suffix,
            final String name, final String template,
            final List<String> viewTemplates,
            final boolean createAbstract, final boolean overwriteConcrete,
            final boolean createViewXml) {
        super(path, mirrorType, suffix, name, template, createAbstract,
                overwriteConcrete);
        this.viewTemplates = viewTemplates;
        this.createViewXml = createViewXml;
    }
    
    public List<String> getViewTemplates() {
        return viewTemplates;
    }

    public boolean isCreateViewXml() {
        return createViewXml;
    }

    @Override
    protected List<RequestFactoryType> resolveReferences(
            final RequestFactoryType type) {
        if (type == PROXY_DETAIL_ACTIVITY) {
            return Arrays.<RequestFactoryType>asList(PROXY_DETAIL_FRAGMENT,
                    PROXY_LIST_ACTIVITY);
        } else if (type == PROXY_LIST_ACTIVITY) {
            return Arrays.<RequestFactoryType>asList(PROXY_DETAIL_ACTIVITY,
                    PROXY_DETAIL_FRAGMENT, PROXY_LIST_FRAGMENT);
        } else if (type == LIST_ACTIVITY_PROCESSOR) {
            return Arrays.<RequestFactoryType>asList(
                    APP_ENTITY_TYPES_PROCESSOR, PROXY_LIST_ACTIVITY);
        } else if (type == PLURAL_PROCESSOR) {
            return Arrays.<RequestFactoryType>asList(
                    APP_ENTITY_TYPES_PROCESSOR);
        } else {
            return new ArrayList<RequestFactoryType>();
        }
    }
}
