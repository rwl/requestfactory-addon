package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.project.GAV;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.packaging.AbstractPackagingProvider;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


@Component
@Service
public class ApkPackagingProvider extends AbstractPackagingProvider {

    public static final String NAME = "apk";

    @Reference private MetadataService metadataService;

    /**
     * Constructor invoked by the OSGi container
     */
    public ApkPackagingProvider() {
        super(NAME, NAME, "apk-pom-template.xml");
    }

    @Override
    protected void createOtherArtifacts(final JavaPackage topLevelPackage,
            final String module, final ProjectOperations projectOperations) {
        super.createOtherArtifacts(topLevelPackage, module, projectOperations);

        final String moduleName = getFullyQualifiedModuleName(module,
                projectOperations);
        final ProjectMetadata projectMetadata = (ProjectMetadata) metadataService
                .get(ProjectMetadata.getProjectIdentifier(moduleName));
        Validate.notNull(projectMetadata,
                "Project metadata required for module '" + moduleName + "'");
        final Document document = XmlUtils.readXml(FileUtils.getInputStream(
                getClass(), "AndroidManifest-template.xml"));
        final Element root = document.getDocumentElement();
        root.setAttribute("package",
                        topLevelPackage.getFullyQualifiedPackageName());
        fileManager.createOrUpdateTextFileIfRequired(pathResolver
                .getIdentifier(
                        Path.ROOT.getModulePathId(moduleName),
                        "AndroidManifest.xml"), XmlUtils
                .nodeToString(document), false);

        try {
            final String input = IOUtils.toString(FileUtils.getInputStream(
                    getClass(), "project-template.properties"));
            fileManager.createOrUpdateTextFileIfRequired(pathResolver
                    .getIdentifier(
                            Path.ROOT.getModulePathId(moduleName),
                            "project.properties"), input, false);
        } catch (final IOException e) {
            throw new IllegalStateException(
                    "Unable to create 'project.properties'", e);
        }

        fileManager.scan();
    }

    @Override
    protected String createPom(final JavaPackage topLevelPackage,
            final String nullableProjectName, final String javaVersion,
            final GAV parentPom, final String moduleName,
            final ProjectOperations projectOperations) {
        Validate.isTrue("1.6".equals(javaVersion),
                "Java version must be 1.6, but %s is specified", javaVersion);
        return super.createPom(topLevelPackage,
                nullableProjectName, javaVersion, parentPom, moduleName,
                projectOperations);
    }

    @Override
    public boolean isDefault() {
        return false;
    }

    @Override
    protected final void setPackagingProviderId(final Document pom) {
        // Not needed, as the provider uses the Maven packaging name as
        // the ID.
    }

    @Override
    public Collection<Path> getPaths() {
        return Arrays.asList(SRC_MAIN_JAVA);
    }
}
