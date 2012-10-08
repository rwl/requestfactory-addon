package org.springframework.roo.addon.gwt.account;

import java.io.File;

import org.apache.commons.lang3.Validate;
import org.springframework.roo.model.JavaPackage;

public enum AccountPath {

    ACCOUNT("/account", "account/" + AccountPath.templateSelector),
    WEB("", "webapp/" + AccountPath.wildCardSelector);

    private static final String templateSelector = "*-template.*";
    private static final String wildCardSelector = "*";

    private final String segmentName;
    private final String sourceAntPath;

    /**
     * Constructor
     *
     * @param segmentName
     * @param sourceAntPath the Ant-style path to the source files for this
     *            {@link AccountPath}, relative to the package in which this enum is
     *            located (required)
     */
    AccountPath(final String segmentName, final String sourceAntPath) {
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
