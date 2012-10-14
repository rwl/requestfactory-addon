package roo.addon.requestfactory;

import java.io.File;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.model.JavaPackage;

public enum RequestFactoryPath {

    CLIENT("/client", "module/client/" + RequestFactoryPath.templateSelector),
    GWT_ROOT("/", "module/" + RequestFactoryPath.templateSelector),
    IMAGES("/client/style/images", "module/client/style/images/" + RequestFactoryPath.wildCardSelector),
    LOCATOR("/server/locator", "module/server/locator/" + RequestFactoryPath.templateSelector), // GWT_REQUEST
    MANAGED("/client/managed", "module/client/managed/" + RequestFactoryPath.templateSelector),
    MANAGED_ACTIVITY("/client/managed/activity", "module/client/managed/activity/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD
    MANAGED_REQUEST("/client/managed/request", "module/client/request/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_GENERATED
    MANAGED_UI("/client/managed/ui", "module/client/managed/ui/" + RequestFactoryPath.templateSelector),
    MANAGED_TREE("/client/managed/tree", "module/client/managed/tree/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_UI
    MANAGED_UI_DESKTOP("/client/managed/ui/desktop", "module/client/managed/ui/desktop/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_UI
    MANAGED_UI_MOBILE("/client/managed/ui/mobile", "module/client/managed/ui/mobile/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_UI
    MANAGED_UI_RENDERER("/client/managed/ui/renderer", "module/client/managed/ui/renderer/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_UI
    MANAGED_UI_EDITOR("/client/managed/ui/editor", "module/client/managed/ui/editor/" + RequestFactoryPath.templateSelector), // GWT_SCAFFOLD_UI
    SCAFFOLD("/client/scaffold", "module/client/scaffold/" + RequestFactoryPath.templateSelector),
    SCAFFOLD_ACTIVITY("/client/scaffold/activity", "module/client/scaffold/activity/" + RequestFactoryPath.templateSelector),
//    SCAFFOLD_GAE("/client/scaffold/gae", "module/client/scaffold/gae/" + GwtPath.templateSelector),
    SCAFFOLD_IOC("/client/scaffold/ioc", "module/client/scaffold/ioc/" + RequestFactoryPath.templateSelector),
    SCAFFOLD_PLACE("/client/scaffold/place", "module/client/scaffold/place/" + RequestFactoryPath.templateSelector),
    SCAFFOLD_REQUEST("/client/scaffold/request", "module/client/scaffold/request/" + RequestFactoryPath.templateSelector),
    SCAFFOLD_UI("/client/scaffold/ui", "module/client/scaffold/ui/" + RequestFactoryPath.templateSelector),
    SERVER("/server", "module/server/" + RequestFactoryPath.templateSelector), // IOC
//    SERVER_GAE("/server/gae", "module/server/gae/" + GwtPath.templateSelector), // PLACE
    SHARED("/shared", "module/shared/" + RequestFactoryPath.templateSelector),
//    SHARED_GAE("/shared/gae", "module/shared/gae/" + GwtPath.templateSelector),
    SHARED_SCAFFOLD("/shared/scaffold", "module/shared/scaffold/" + RequestFactoryPath.templateSelector),
    STYLE("/client/style", "module/client/style/" + RequestFactoryPath.templateSelector),
    WEB("", "webapp/" + RequestFactoryPath.wildCardSelector),

    ACCOUNT_HELPER("/client/scaffold/account/helper", "module/client/scaffold/helper/" + RequestFactoryPath.templateSelector),
    SCAFFOLD_ACCOUNT("/client/scaffold/account", "module/client/scaffold/account/" + RequestFactoryPath.templateSelector),
    SERVER_ACCOUNT("/server/account", "module/server/account/" + RequestFactoryPath.templateSelector),
    SHARED_ACCOUNT("/shared/account", "module/shared/account/" + RequestFactoryPath.templateSelector),
    ACCOUNT_ROOT("/account", "account/module/" + RequestFactoryPath.templateSelector),
    ACCOUNT_UI("/account/client", "module/client/account/ui/" + RequestFactoryPath.templateSelector),
    ACCOUNT_WEB(".", "account/webapp/" + RequestFactoryPath.wildCardSelector);

    private static final String templateSelector = "*-template.*";
    private static final String wildCardSelector = "*";

    private final String segmentName;
    private final String sourceAntPath;

    /**
     * Constructor
     *
     * @param segmentName
     * @param sourceAntPath the Ant-style path to the source files for this
     *            {@link RequestFactoryPath}, relative to the package in which this enum is
     *            located (required)
     */
    RequestFactoryPath(final String segmentName, final String sourceAntPath) {
        Validate.notBlank(sourceAntPath, "Source Ant path is required");
        this.segmentName = segmentName;
        this.sourceAntPath = sourceAntPath;
    }

    public String getPackagePath(final JavaPackage topLevelPackage) {
        return topLevelPackage.getFullyQualifiedPackageName().replace('.',
                File.separatorChar)
                + segmentName.replace('/', File.separatorChar);
    }

    /**
     * Package access for benefit of unit test
     *
     * @return
     */
    String getSegmentName() {
        return segmentName;
    }

    public String getSourceAntPath() {
        return sourceAntPath;
    }

    public String packageName(final JavaPackage topLevelPackage) {
        if (WEB.equals(this)) {
            return "";
        }
        return topLevelPackage.getFullyQualifiedPackageName()
                + segmentName.replace('/', '.');
    }

    public String segmentPackage() {
        if (WEB.equals(this)) {
            return "";
        }
        return segmentName.substring(1).replace('/', '.');
    }
}
