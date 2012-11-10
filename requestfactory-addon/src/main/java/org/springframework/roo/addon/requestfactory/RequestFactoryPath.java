package org.springframework.roo.addon.requestfactory;

import java.io.File;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapPaths;
import org.springframework.roo.model.JavaPackage;

public class RequestFactoryPath {

    public static final RequestFactoryPath SERVER_LOCATOR = new RequestFactoryPath(
            "/server/locator",
            "module/server/locator/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SHARED_MANAGED_REQUEST = new RequestFactoryPath(
            "/shared/managed/request",
            "module/client/request/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SHARED_SCAFFOLD = new RequestFactoryPath(
            "/shared/scaffold",
            "module/shared/scaffold/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SERVER = new RequestFactoryPath(
            "/server",
            "module/server/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SERVER_ACCOUNT = new RequestFactoryPath(
            "/server/account",
            "module/server/account/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SHARED_ACCOUNT = new RequestFactoryPath(
            "/shared/account",
            "module/shared/account/" + RequestFactoryPath.templateSelector);


    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
            SHARED_MANAGED_REQUEST, SHARED_SCAFFOLD, SERVER, SERVER_ACCOUNT,
            SHARED_ACCOUNT
    };

    public static final RequestFactoryPath[] SERVER_PATHS = new RequestFactoryPath[] {
            SERVER, SERVER_LOCATOR, SERVER_ACCOUNT
    };

    public static final RequestFactoryPath[] SHARED_PATHS = new RequestFactoryPath[] {
            SHARED_MANAGED_REQUEST, SHARED_SCAFFOLD, SHARED_ACCOUNT
    };


    public static final String templateSelector = "*-template.*";
    public static final String wildCardSelector = "*";

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
    public RequestFactoryPath(final String segmentName, final String sourceAntPath) {
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
        if (GwtBootstrapPaths.WEB.equals(this)) {
            return "";
        }
        return topLevelPackage.getFullyQualifiedPackageName()
                + segmentName.replace('/', '.');
    }

    public String segmentPackage() {
        if (GwtBootstrapPaths.WEB.equals(this)) {
            return "";
        }
        return segmentName.substring(1).replace('/', '.');
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((segmentName == null) ? 0 : segmentName.hashCode());
        result = prime * result + ((sourceAntPath == null) ? 0 : sourceAntPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!getClass().isAssignableFrom(obj.getClass()))
            return false;
        RequestFactoryPath other = (RequestFactoryPath) obj;
        if (segmentName == null) {
            if (other.segmentName != null)
                return false;
        } else if (!segmentName.equals(other.segmentName))
            return false;
        if (sourceAntPath == null) {
            if (other.sourceAntPath != null)
                return false;
        } else if (!sourceAntPath.equals(other.sourceAntPath))
            return false;
        return true;
    }
}
