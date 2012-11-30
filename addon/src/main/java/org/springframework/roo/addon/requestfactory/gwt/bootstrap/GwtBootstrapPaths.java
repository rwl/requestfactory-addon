package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class GwtBootstrapPaths {

    public static final RequestFactoryPath GWT_ROOT = new RequestFactoryPath("/", "module/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath IMAGES = new RequestFactoryPath("/style/images", "module/client/style/images/" + RequestFactoryPath.wildCardSelector);
    public static final RequestFactoryPath MANAGED = new RequestFactoryPath("/managed", "module/client/managed/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath MANAGED_ACTIVITY = new RequestFactoryPath("/managed/activity", "module/client/managed/activity/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD
    public static final RequestFactoryPath MANAGED_UI = new RequestFactoryPath("/managed/ui", "module/client/managed/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath MANAGED_TREE = new RequestFactoryPath("/managed/tree", "module/client/managed/tree/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_PROVIDER = new RequestFactoryPath("/managed/provider", "module/client/managed/provider/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_DESKTOP = new RequestFactoryPath("/managed/ui/desktop", "module/client/managed/ui/desktop/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_MOBILE = new RequestFactoryPath("/managed/ui/mobile", "module/client/managed/ui/mobile/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_RENDERER = new RequestFactoryPath("/managed/ui/renderer", "module/client/managed/ui/renderer/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_EDITOR = new RequestFactoryPath("/managed/ui/editor", "module/client/managed/ui/editor/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath SCAFFOLD = new RequestFactoryPath("/app", "module/client/scaffold/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_ACTIVITY = new RequestFactoryPath("/activity", "module/client/scaffold/activity/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_IOC = new RequestFactoryPath("/ioc", "module/client/scaffold/ioc/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_PLACE = new RequestFactoryPath("/place", "module/client/scaffold/place/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_REQUEST = new RequestFactoryPath("/request", "module/client/scaffold/request/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_UI = new RequestFactoryPath("/ui", "module/client/scaffold/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath STYLE = new RequestFactoryPath("/style", "module/client/style/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath WEB = new RequestFactoryPath("", "webapp/" + RequestFactoryPath.wildCardSelector);

    public static final RequestFactoryPath ACCOUNT_HELPER = new RequestFactoryPath("/account/helper", "module/client/scaffold/helper/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_ACCOUNT = new RequestFactoryPath("/account/scaffold", "module/client/scaffold/account/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_ROOT = new RequestFactoryPath("/account", "account/module/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_UI = new RequestFactoryPath("/account/client", "module/client/account/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_WEB = new RequestFactoryPath(".", "account/webapp/" + RequestFactoryPath.wildCardSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        GWT_ROOT, IMAGES, MANAGED, MANAGED_ACTIVITY,
        MANAGED_UI, MANAGED_TREE, MANAGED_PROVIDER, MANAGED_UI_DESKTOP,
        MANAGED_UI_MOBILE, MANAGED_UI_RENDERER, MANAGED_UI_EDITOR,
        SCAFFOLD, SCAFFOLD_ACTIVITY, SCAFFOLD_IOC, SCAFFOLD_PLACE,
        SCAFFOLD_REQUEST, SCAFFOLD_UI,
        STYLE, WEB, ACCOUNT_HELPER, SCAFFOLD_ACCOUNT,
        ACCOUNT_ROOT, ACCOUNT_UI, ACCOUNT_WEB
    };

    public static final String SHARED_MODULE_NAME = "Shared";
    public static final String DOMAIN_MODULE_NAME = "Domain";

    public static final RequestFactoryPath SHARED_MODULE = new RequestFactoryPath("/", "module/shared/" + SHARED_MODULE_NAME + ".gwt.xml");
    public static final RequestFactoryPath DOMAIN_MODULE = new RequestFactoryPath("/", "module/shared/" + DOMAIN_MODULE_NAME + ".gwt.xml");

    private GwtBootstrapPaths() {
    }
}
