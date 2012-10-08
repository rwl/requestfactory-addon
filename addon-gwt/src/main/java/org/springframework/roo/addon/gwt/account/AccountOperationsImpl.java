package org.springframework.roo.addon.gwt.account;

import static org.springframework.roo.addon.gwt.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;
import static org.springframework.roo.project.Path.SRC_MAIN_WEBAPP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.finder.FinderOperationsImpl;
import org.springframework.roo.addon.gwt.GwtPath;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.DependencyScope;
import org.springframework.roo.project.DependencyType;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class AccountOperationsImpl implements AccountOperations {

    private static final Logger LOGGER = HandlerUtils
            .getLogger(AccountOperationsImpl.class);

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    @Reference protected FileManager fileManager;
    @Reference private PathResolver pathResolver;

    private ComponentContext context;

    protected void activate(final ComponentContext context) {
        this.context = context;
    }

    /** {@inheritDoc} */
    public boolean isCommandAvailable() {

        // Check if a project has been created
        if (!projectOperations.isFocusedProjectAvailable()) {
            return false;
        }
        if (typeLocationService.findTypesWithAnnotation(ROO_JPA_ACTIVE_RECORD, ROO_JPA_ENTITY).size() == 0) {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    public void annotateAccountType(JavaType javaType, final JavaPackage sharedPackage) {
        // Use Roo's Assert type for null checks
        Validate.notNull(javaType, "Java type required");

        // Obtain ClassOrInterfaceTypeDetails for this java type
        ClassOrInterfaceTypeDetails existing = typeLocationService.getTypeDetails(javaType);

        // Test if the annotation already exists on the target type
        if (existing != null && MemberFindingUtils.getAnnotationOfType(existing.getAnnotations(), ROO_ACCOUNT) == null) {
            ClassOrInterfaceTypeDetailsBuilder classOrInterfaceTypeDetailsBuilder = new ClassOrInterfaceTypeDetailsBuilder(existing);

            // Create Annotation metadata
            AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(ROO_ACCOUNT);
            if (sharedPackage != null) {
                annotationBuilder.addStringAttribute(RooAccount.SHARED_PACKAGE_ATTRIBUTE, sharedPackage.getFullyQualifiedPackageName());
            }

            // Add annotation to target type
            classOrInterfaceTypeDetailsBuilder.addAnnotation(annotationBuilder.build());

            // Save changes to disk
            typeManagementService.createOrUpdateTypeOnDisk(classOrInterfaceTypeDetailsBuilder.build());
        }
    }

    private JavaPackage accountPackage;
    private JavaType accountType;
    private JavaType roleType;

    @Override
    public void setupSecurity(JavaType accountType, JavaPackage accountPackage) {
        this.accountPackage = accountPackage;
        this.accountType = accountType;

        ClassOrInterfaceTypeDetails accountDetails = typeLocationService.getTypeDetails(accountType);
        AnnotationMetadata annotationMetadata = accountDetails.getAnnotation(ROO_ACCOUNT);
        if (annotationMetadata == null) {
            LOGGER.warning("Specified type is not a valid user details account");
            return;
        }

        AnnotationAttributeValue<String> sharedPackage = annotationMetadata.getAttribute(RooAccount.SHARED_PACKAGE_ATTRIBUTE);
        if (sharedPackage == null || sharedPackage.getValue().isEmpty()) {
            roleType = new JavaType(accountType.getPackage().getFullyQualifiedPackageName() + ".Role");
        } else {
            roleType = new JavaType(sharedPackage.getValue() + ".Role");
        }

        final String accountDirectory = projectOperations.getPathResolver().getFocusedIdentifier(
                SRC_MAIN_JAVA, accountPackage.getFullyQualifiedPackageName().replace('.',
                        File.separatorChar));

        updateFile("*-template.*", accountDirectory, null, false);

        String moduleName = projectOperations.getFocusedModuleName();
        for (Element propertyElement : XmlUtils.findElements("/configuration/batch/properties/*", XmlUtils.getConfiguration(getClass()))) {
            projectOperations.addProperty(moduleName, new Property(propertyElement));
        }
        List<Dependency> dependencies = new ArrayList<Dependency>();
        for (Element dependencyElement : XmlUtils.findElements("/configuration/batch/dependencies/dependency", XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.addDependencies(moduleName, dependencies);


        final String springDirectory = pathResolver.getFocusedIdentifier(
                Path.SPRING_CONFIG_ROOT, "");

        if (projectOperations.isFeatureInstalledInFocusedModule(FeatureNames.GAE)) {
            updateFile("context/*security-gae-template.xml", springDirectory, null, false);
            updateFile("context/*-template.java", accountDirectory, null, false);
        } else {
            updateFile("context/*security-template.xml", springDirectory, null, false);
        }
    }

    private void updateFile(final String sourceAntPath, String targetDirectory,
            final String segmentPackage, final boolean overwrite) {
        if (!targetDirectory.endsWith(File.separator)) {
            targetDirectory += File.separator;
        }
        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        final String path = FileUtils.getPath(getClass(), sourceAntPath);
        final Iterable<URL> urls = OSGiUtils.findEntriesByPattern(
                context.getBundleContext(), path);
        Validate.notNull(urls,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");

        for (final URL url : urls) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf('/') + 1);
            fileName = fileName.replace("-template", "");
            final String targetFilename = targetDirectory + fileName;

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                if (fileManager.exists(targetFilename) && !overwrite) {
                    continue;
                }
                if (targetFilename.endsWith("png")) {
                    inputStream = url.openStream();
                    outputStream = fileManager.createFile(targetFilename)
                            .getOutputStream();
                    IOUtils.copy(inputStream, outputStream);
                }
                else {
                    // Read template and insert the user's package
                    String input = IOUtils.toString(url);
                    input = processTemplate(input, segmentPackage);

                    // Output the file for the user
                    fileManager.createOrUpdateTextFileIfRequired(
                            targetFilename, input, true);
                }
            }
            catch (final IOException e) {
                throw new IllegalStateException("Unable to create '"
                        + targetFilename + "'", e);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
                IOUtils.closeQuietly(outputStream);
            }
        }
    }

    private String processTemplate(String input, String segmentPackage) {
        if (segmentPackage == null) {
            segmentPackage = "";
        }

        if (accountPackage != null) {
            input = input.replace("__ACCOUNT_PACKAGE__", accountPackage.getFullyQualifiedPackageName());
        }

        if (accountType != null) {
            input = input.replace("__FULL_ACCOUNT_NAME__", accountType.getFullyQualifiedTypeName());
            input = input.replace("__ACCOUNT_NAME__", accountType.getSimpleTypeName());
        }
        if (roleType != null) {
            input = input.replace("__FULL_ROLE_NAME__", roleType.getFullyQualifiedTypeName());
            input = input.replace("__ROLE_NAME__", roleType.getSimpleTypeName());
        }

        return input;
    }
}