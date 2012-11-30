package org.springframework.roo.addon.requestfactory;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.annotations.account.RooAccount;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.FileUtils;

@Component
public class BaseOperationsImpl {

    /**
     * Use TypeManagementService to change types
     */
    @Reference protected TypeManagementService typeManagementService;

    /**
     * Use TypeLocationService to find types which are annotated with a given
     * annotation in the project
     */
    @Reference protected TypeLocationService typeLocationService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference protected ProjectOperations projectOperations;

    @Reference protected FileManager fileManager;

    @Reference protected MetadataService metadataService;

    protected ComponentContext context;

    protected void activate(final ComponentContext context) {
        this.context = context;
    }

    protected void updateFile(final String sourceAntPath, String targetDirectory,
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

    protected String processTemplate(String input, String segmentPackage) {
        if (segmentPackage == null) {
            segmentPackage = "";
        }
        final String topLevelPackage = projectOperations.getTopLevelPackage(
                projectOperations.getFocusedModuleName())
                .getFullyQualifiedPackageName();
        input = input.replace("__TOP_LEVEL_PACKAGE__", topLevelPackage);
        input = input.replace("__SHARED_TOP_LEVEL_PACKAGE__", getProxyTopLevelPackageName());
        input = input.replace("__SEGMENT_PACKAGE__", segmentPackage);
        input = input.replace("__PROJECT_NAME__", projectOperations
                .getProjectName(projectOperations.getFocusedModuleName()));

        if (typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() != 0) {
            input = input.replace("__ACCOUNT_IMPORT__", "import " + topLevelPackage
                    + ".client.scaffold.account.*;\n");
            input = input.replace("__ACCOUNT_HOOKUP__", getAccountHookup());
            input = input.replace("__ACCOUNT_REQUEST_TRANSPORT__",
                    ", new AccountAuthRequestTransport(eventBus)");
            input = input.replace("__IMPORT_ACCOUNT__", getImportAccountHookup());
            input = input.replace("__IMPORT_ROLE__", getImportRoleHookup());
        }
        else {
            input = input.replace("__ACCOUNT_IMPORT__", "");
            input = input.replace("__ACCOUNT_HOOKUP__", "");
            input = input.replace("__ACCOUNT_REQUEST_TRANSPORT__", "");
            input = input.replace("__IMPORT_ACCOUNT__", "");
            input = input.replace("__IMPORT_ROLE__", "");
        }
        return input;
    }

    protected CharSequence getProxyTopLevelPackageName() {
        final ClassOrInterfaceTypeDetails proxy = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)
                .iterator().next();
        final JavaPackage proxyTopLevelPackage = projectOperations
                .getTopLevelPackage(PhysicalTypeIdentifier.getPath(
                proxy.getDeclaredByMetadataId()).getModule());
        return proxyTopLevelPackage.getFullyQualifiedPackageName();
    }

    protected CharSequence getImportAccountHookup() {
        final JavaType account = typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT)
                .iterator().next();
        return "import " + account.getFullyQualifiedTypeName() + ";";
    }

    protected CharSequence getImportRoleHookup() {
        final ClassOrInterfaceTypeDetails account = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_ACCOUNT)
                .iterator().next();
        final AnnotationAttributeValue<String> sharedPackage = account
                .getAnnotation(ROO_ACCOUNT)
                .getAttribute(RooAccount.SHARED_PACKAGE_ATTRIBUTE);
        final String rolePackageName;
        if (sharedPackage == null || sharedPackage.getValue().isEmpty()) {
            rolePackageName = account.getType().getPackage()
                    .getFullyQualifiedPackageName();
        } else {
            rolePackageName = sharedPackage.getValue();
        }
        return "import " + rolePackageName + ".Role;";
    }

    protected CharSequence getAccountHookup() {
        final StringBuilder builder = new StringBuilder();
        builder.append("new AccountNavTextDriver(requestFactory).setWidget(shell.getNicknameWidget());\n");
        builder.append("\t\tnew LoginOnAuthenticationFailure().register(eventBus);");
        return builder.toString();
    }
}
