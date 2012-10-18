package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

public class GwtBootstrapPaths {

    public static final RequestFactoryPath CLIENT = new RequestFactoryPath("/client", "module/client/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath GWT_ROOT = new RequestFactoryPath("/", "module/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath IMAGES = new RequestFactoryPath("/client/style/images", "module/client/style/images/" + RequestFactoryPath.wildCardSelector);
    public static final RequestFactoryPath MANAGED = new RequestFactoryPath("/client/managed", "module/client/managed/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath MANAGED_ACTIVITY = new RequestFactoryPath("/client/managed/activity", "module/client/managed/activity/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD
    public static final RequestFactoryPath MANAGED_UI = new RequestFactoryPath("/client/managed/ui", "module/client/managed/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath MANAGED_TREE = new RequestFactoryPath("/client/managed/tree", "module/client/managed/tree/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_DESKTOP = new RequestFactoryPath("/client/managed/ui/desktop", "module/client/managed/ui/desktop/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_MOBILE = new RequestFactoryPath("/client/managed/ui/mobile", "module/client/managed/ui/mobile/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_RENDERER = new RequestFactoryPath("/client/managed/ui/renderer", "module/client/managed/ui/renderer/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath MANAGED_UI_EDITOR = new RequestFactoryPath("/client/managed/ui/editor", "module/client/managed/ui/editor/" + RequestFactoryPath.templateSelector); // GWT_SCAFFOLD_UI
    public static final RequestFactoryPath SCAFFOLD = new RequestFactoryPath("/client/scaffold", "module/client/scaffold/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_ACTIVITY = new RequestFactoryPath("/client/scaffold/activity", "module/client/scaffold/activity/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_IOC = new RequestFactoryPath("/client/scaffold/ioc", "module/client/scaffold/ioc/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_PLACE = new RequestFactoryPath("/client/scaffold/place", "module/client/scaffold/place/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_REQUEST = new RequestFactoryPath("/client/scaffold/request", "module/client/scaffold/request/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_UI = new RequestFactoryPath("/client/scaffold/ui", "module/client/scaffold/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SERVER = new RequestFactoryPath("/server", "module/server/" + RequestFactoryPath.templateSelector); // IOC
    public static final RequestFactoryPath SHARED = new RequestFactoryPath("/shared", "module/shared/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SHARED_SCAFFOLD = new RequestFactoryPath("/shared/scaffold", "module/shared/scaffold/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath STYLE = new RequestFactoryPath("/client/style", "module/client/style/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath WEB = new RequestFactoryPath("", "webapp/" + RequestFactoryPath.wildCardSelector);

    public static final RequestFactoryPath ACCOUNT_HELPER = new RequestFactoryPath("/client/scaffold/account/helper", "module/client/scaffold/helper/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SCAFFOLD_ACCOUNT = new RequestFactoryPath("/client/scaffold/account", "module/client/scaffold/account/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SERVER_ACCOUNT = new RequestFactoryPath("/server/account", "module/server/account/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath SHARED_ACCOUNT = new RequestFactoryPath("/shared/account", "module/shared/account/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_ROOT = new RequestFactoryPath("/account", "account/module/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_UI = new RequestFactoryPath("/account/client", "module/client/account/ui/" + RequestFactoryPath.templateSelector);
    public static final RequestFactoryPath ACCOUNT_WEB = new RequestFactoryPath(".", "account/webapp/" + RequestFactoryPath.wildCardSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
        CLIENT, GWT_ROOT, IMAGES, MANAGED, MANAGED_ACTIVITY,
        MANAGED_UI, MANAGED_TREE, MANAGED_UI_DESKTOP,
        MANAGED_UI_MOBILE, MANAGED_UI_RENDERER, MANAGED_UI_EDITOR,
        SCAFFOLD, SCAFFOLD_ACTIVITY, SCAFFOLD_IOC, SCAFFOLD_PLACE,
        SCAFFOLD_REQUEST, SCAFFOLD_UI, SERVER, SHARED, SHARED_SCAFFOLD,
        STYLE, WEB, ACCOUNT_HELPER, SCAFFOLD_ACCOUNT, SERVER_ACCOUNT,
        SHARED_ACCOUNT, ACCOUNT_ROOT, ACCOUNT_UI, ACCOUNT_WEB
    };

    private GwtBootstrapPaths() {
    }
}
