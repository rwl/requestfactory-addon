package org.springframework.roo.addon.requestfactory;

import java.io.File;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapPaths;
import org.springframework.roo.model.JavaPackage;

public class RequestFactoryPath {

    public static final RequestFactoryPath LOCATOR = new RequestFactoryPath(
            "/server/locator",
            "module/server/locator/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath MANAGED_REQUEST = new RequestFactoryPath(
            "/client/managed/request",
            "module/client/request/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath SHARED_SCAFFOLD = new RequestFactoryPath(
            "/shared/scaffold",
            "module/shared/scaffold/" + RequestFactoryPath.templateSelector);

    public static final RequestFactoryPath[] ALL_PATHS = new RequestFactoryPath[] {
            LOCATOR, MANAGED_REQUEST, SHARED_SCAFFOLD
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
}
