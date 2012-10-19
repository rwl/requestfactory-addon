package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ENTITY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.OLD_ENTITY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.OLD_REQUEST_CONTEXT;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.REQUEST_CONTEXT;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_MIRRORED_FROM;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_REQUEST;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapJavaType.ROO_GWT_BOOTSTRAP_SCAFFOLD;
import static org.springframework.roo.project.Path.ROOT;
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;
import static org.springframework.roo.project.Path.SRC_MAIN_WEBAPP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.account.RooAccount;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.Repository;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.DomUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.WebXmlUtils;
import org.springframework.roo.support.util.XmlElementBuilder;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Implementation of operations this add-on offers.
 *
 * @since 1.1
 */
@Component // Use these Apache Felix annotations to register your commands class in the Roo container
@Service
public class GwtBootstrapOperationsImpl implements GwtBootstrapOperations {

    private static final String FEATURE_NAME = "gwtbootstrap";
    private static final String GWT_BUILD_COMMAND = "com.google.gwt.eclipse.core.gwtProjectValidator";
    private static final String GWT_PROJECT_NATURE = "com.google.gwt.eclipse.core.gwtNature";
    private static final String MAVEN_ECLIPSE_PLUGIN = "/project/build/plugins/plugin[artifactId = 'maven-eclipse-plugin']";
    private static final String OUTPUT_DIRECTORY = "${project.build.directory}/${project.build.finalName}/WEB-INF/classes";

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
    @Reference protected RequestFactoryTemplateService requestFactoryTemplateService;
    @Reference protected GwtBootstrapTypeService gwtBootstrapTypeService;
    @Reference protected WebMvcOperations webMvcOperations;
    @Reference protected MetadataService metadataService;

    private Boolean wasGaeEnabled;
    private ComponentContext context;

    protected void activate(final ComponentContext context) {
        this.context = context;
    }

    public boolean isGwtInstallationPossible() {
        return projectOperations.isFocusedProjectAvailable()
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(FeatureNames.JSF);
    }

    public boolean isScaffoldAvailable() {
        return isGwtInstallationPossible()
                && isInstalledInModule(projectOperations.getFocusedModuleName());
    }

    public void setupGwtBootstrap() {
        // Install web pieces if not already installed
        if (!fileManager.exists(projectOperations.getPathResolver()
                .getFocusedIdentifier(SRC_MAIN_WEBAPP, "WEB-INF/web.xml"))) {
            webMvcOperations.installAllWebMvcArtifacts();
        }

        final String topPackageName = projectOperations.getTopLevelPackage(
                projectOperations.getFocusedModuleName())
                .getFullyQualifiedPackageName();
        final Set<FileDetails> gwtConfigs = fileManager
                .findMatchingAntPath(projectOperations.getPathResolver()
                        .getFocusedRoot(SRC_MAIN_JAVA)
                        + File.separatorChar
                        + topPackageName.replace('.', File.separatorChar)
                        + File.separator + "*.gwt.xml");
        final boolean gwtAlreadySetup = !gwtConfigs.isEmpty();

        if (!gwtAlreadySetup) {
            String sourceAntPath = "setup/*";
            final String targetDirectory = projectOperations.getPathResolver()
                    .getFocusedIdentifier(SRC_MAIN_JAVA,
                            topPackageName.replace('.', File.separatorChar));
            updateFile(sourceAntPath, targetDirectory, "", false);

            sourceAntPath = "setup/client/*";
            updateFile(sourceAntPath, targetDirectory + "/client", "", false);
        }

        for (final ClassOrInterfaceTypeDetails proxyOrRequest : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_MIRRORED_FROM)) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                    proxyOrRequest);
            if (proxyOrRequest.extendsType(ENTITY_PROXY)
                    || proxyOrRequest.extendsType(OLD_ENTITY_PROXY)) {
                final AnnotationMetadata annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(proxyOrRequest.getAnnotations(),
                                ROO_REQUEST_FACTORY_MIRRORED_FROM);
                if (annotationMetadata != null) {
                    final AnnotationMetadataBuilder annotationMetadataBuilder = new AnnotationMetadataBuilder(
                            annotationMetadata);
                    annotationMetadataBuilder.setAnnotationType(ROO_REQUEST_FACTORY_PROXY);
                    cidBuilder.removeAnnotation(ROO_REQUEST_FACTORY_MIRRORED_FROM);
                    cidBuilder.addAnnotation(annotationMetadataBuilder);
                    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder
                            .build());
                }
            }
            else if (proxyOrRequest.extendsType(REQUEST_CONTEXT)
                    || proxyOrRequest.extendsType(OLD_REQUEST_CONTEXT)) {
                final AnnotationMetadata annotationMetadata = MemberFindingUtils
                        .getAnnotationOfType(proxyOrRequest.getAnnotations(),
                                ROO_REQUEST_FACTORY_MIRRORED_FROM);
                if (annotationMetadata != null) {
                    final AnnotationMetadataBuilder annotationMetadataBuilder = new AnnotationMetadataBuilder(
                            annotationMetadata);
                    annotationMetadataBuilder
                            .setAnnotationType(ROO_REQUEST_FACTORY_REQUEST);
                    cidBuilder.removeAnnotation(ROO_REQUEST_FACTORY_MIRRORED_FROM);
                    cidBuilder.addAnnotation(annotationMetadataBuilder);
                    typeManagementService.createOrUpdateTypeOnDisk(cidBuilder
                            .build());
                }
            }
        }

        // Add GWT natures and builder names to maven eclipse plugin
        updateEclipsePlugin();

        // Add outputDirectory to build element of pom
        updateBuildOutputDirectory();

        final Element configuration = XmlUtils.getConfiguration(getClass());

        // Add POM properties
        updateProperties(configuration);

        // Add POM repositories
        updateRepositories(configuration);

        // Add dependencies
        updateDependencies(configuration);

        // Update web.xml
        updateWebXml();

        // Update gwt-maven-plugin and others
        updateBuildPlugins(projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.GAE));
    }

    public void scaffoldAll(final JavaPackage proxyPackage,
            final JavaPackage requestPackage) {
        updateScaffoldBoilerPlate();
//        proxyAll(proxyPackage);
//        requestAll(requestPackage);
        for (final ClassOrInterfaceTypeDetails proxy : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)) {
            final ClassOrInterfaceTypeDetails request = gwtBootstrapTypeService
                    .lookupRequestFromProxy(proxy);
            if (request == null) {
                throw new IllegalStateException(
                        "In order to scaffold, an entity must have a request");
            }
            createScaffold(proxy);
        }
    }

    public void scaffoldType(final JavaPackage proxyPackage,
            final JavaPackage requestPackage, final JavaType type) {
//        proxyType(proxyPackage, type);
//        requestType(requestPackage, type);
        final ClassOrInterfaceTypeDetails entity = typeLocationService
                .getTypeDetails(type);
        if (entity != null && !entity.isAbstract()) {
            final ClassOrInterfaceTypeDetails proxy = gwtBootstrapTypeService
                    .lookupProxyFromEntity(entity);
            final ClassOrInterfaceTypeDetails request = gwtBootstrapTypeService
                    .lookupRequestFromEntity(entity);
            if (proxy == null || request == null) {
                throw new IllegalStateException(
                        "In order to scaffold, an entity must have an associated proxy and request");
            }
            updateScaffoldBoilerPlate();
            createScaffold(proxy);
        }
    }

    public void updateGaeConfiguration() {
        final boolean isGaeEnabled = projectOperations
                .isFeatureInstalledInFocusedModule(FeatureNames.GAE);
        final boolean hasGaeStateChanged = wasGaeEnabled == null
                || isGaeEnabled != wasGaeEnabled;
        if (!isInstalledInModule(projectOperations.getFocusedModuleName())
                || !hasGaeStateChanged) {
            return;
        }

        wasGaeEnabled = isGaeEnabled;

        // Update the GaeHelper type
//        updateGaeHelper();

        gwtBootstrapTypeService.buildType(RequestFactoryType.APP_REQUEST_FACTORY,
                requestFactoryTemplateService.getStaticTemplateTypeDetails(
                        RequestFactoryType.APP_REQUEST_FACTORY, projectOperations
                                .getFocusedProjectMetadata().getModuleName()),
                projectOperations.getFocusedModuleName());

        // Ensure the gwt-maven-plugin appropriate to a GAE enabled or disabled
        // environment is updated
        updateBuildPlugins(isGaeEnabled);

        // If there is a class that could possibly import from the appengine
        // sdk, denoted here as having Gae in the type name,
        // then we need to add the appengine-api-1.0-sdk dependency to the
        // pom.xml file
        final String rootPath = projectOperations.getPathResolver()
                .getFocusedRoot(ROOT);
        final Set<FileDetails> files = fileManager.findMatchingAntPath(rootPath
                + "/**/*Gae*.java");
        if (!files.isEmpty()) {
            final Element configuration = XmlUtils.getConfiguration(getClass());
            final Element gaeDependency = XmlUtils
                    .findFirstElement(
                            "/configuration/gae/dependencies/dependency",
                            configuration);
            projectOperations.addDependency(projectOperations
                    .getFocusedModuleName(), new Dependency(gaeDependency));
        }

        // Copy across any missing files, only if GAE state has changed and is
        // now enabled
        if (isGaeEnabled) {
            copyDirectoryContents();
        }
    }

    public String getName() {
        return FEATURE_NAME;
    }

    public boolean isInstalledInModule(final String moduleName) {
        final Pom pom = projectOperations.getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }
        for (final Plugin buildPlugin : pom.getBuildPlugins()) {
            if ("gwt-maven-plugin".equals(buildPlugin.getArtifactId())) {
                return true;
            }
        }
        return false;
    }

    private void updateScaffoldBoilerPlate() {
        final String targetDirectory = projectOperations.getPathResolver()
                .getFocusedIdentifier(
                        SRC_MAIN_JAVA,
                        projectOperations
                                .getTopLevelPackage(
                                        projectOperations
                                                .getFocusedModuleName())
                                .getFullyQualifiedPackageName()
                                .replace('.', File.separatorChar));
        deleteUntouchedSetupFiles("setup/*", targetDirectory);
        deleteUntouchedSetupFiles("setup/client/*", targetDirectory + "/client");
        copyDirectoryContents();
//        updateGaeHelper();
    }

    private void createScaffold(final ClassOrInterfaceTypeDetails proxy) {
        final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                .getFirstAnnotation(proxy, ROO_GWT_BOOTSTRAP_SCAFFOLD);
        if (annotationMetadata == null) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                    new ClassOrInterfaceTypeDetailsBuilder(proxy);
            final AnnotationMetadataBuilder annotationMetadataBuilder =
                    new AnnotationMetadataBuilder(ROO_GWT_BOOTSTRAP_SCAFFOLD);
            cidBuilder.getAnnotations().add(annotationMetadataBuilder);
            typeManagementService.createOrUpdateTypeOnDisk(cidBuilder
                    .build());
        }
    }

    private void deleteUntouchedSetupFiles(final String sourceAntPath,
            String targetDirectory) {
        if (!targetDirectory.endsWith(File.separator)) {
            targetDirectory += File.separator;
        }
        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        final String path = FileUtils.getPath(getClass(), sourceAntPath);
        final Iterable<URL> uris = OSGiUtils.findEntriesByPattern(
                context.getBundleContext(), path);
        Validate.notNull(uris,
                "Could not search bundles for resources for Ant Path '" + path
                        + "'");

        for (final URL url : uris) {
            String fileName = url.getPath().substring(
                    url.getPath().lastIndexOf('/') + 1);
            fileName = fileName.replace("-template", "");
            final String targetFilename = targetDirectory + fileName;
            if (!fileManager.exists(targetFilename)) {
                continue;
            }
            try {
                String input = IOUtils.toString(url);
                input = processTemplate(input, null);
                final String existing = org.apache.commons.io.FileUtils
                        .readFileToString(new File(targetFilename));
                if (existing.equals(input)) {
                    // new File(targetFilename).delete();
                    fileManager.delete(targetFilename);
                }
            }
            catch (final IOException ignored) {
            }
        }
    }

    private String processTemplate(String input, String segmentPackage) {
        if (segmentPackage == null) {
            segmentPackage = "";
        }
        final String topLevelPackage = projectOperations.getTopLevelPackage(
                projectOperations.getFocusedModuleName())
                .getFullyQualifiedPackageName();
        input = input.replace("__TOP_LEVEL_PACKAGE__", topLevelPackage);
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

    private CharSequence getImportAccountHookup() {
        JavaType account = typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT)
                .iterator().next();
        return "import " + account.getFullyQualifiedTypeName() + ";";
    }

    private CharSequence getImportRoleHookup() {
        ClassOrInterfaceTypeDetails account = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_ACCOUNT)
                .iterator().next();
        AnnotationAttributeValue<String> sharedPackage = account
                .getAnnotation(ROO_ACCOUNT)
                .getAttribute(RooAccount.SHARED_PACKAGE_ATTRIBUTE);
        String rolePackageName;
        if (sharedPackage == null || sharedPackage.getValue().isEmpty()) {
            rolePackageName = account.getType().getPackage()
                    .getFullyQualifiedPackageName();
        } else {
            rolePackageName = sharedPackage.getValue();
        }
        return "import " + rolePackageName + ".Role;";
    }

    private CharSequence getAccountHookup() {
        final StringBuilder builder = new StringBuilder();
        builder.append("new AccountNavTextDriver(requestFactory).setWidget(shell.getNicknameWidget());\n");
        builder.append("\t\tnew LoginOnAuthenticationFailure().register(eventBus);");
        return builder.toString();
    }

    private void copyDirectoryContents() {
        for (final RequestFactoryPath path : GwtBootstrapPaths.ALL_PATHS) {
            copyDirectoryContents(path);
        }
    }

    private void copyDirectoryContents(final RequestFactoryPath requestFactoryPath) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("gae")
                && !projectOperations
                        .isFeatureInstalledInFocusedModule(FeatureNames.GAE)) {
            return;
        }
        if (sourceAntPath.contains("account")
                && typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        final String targetDirectory = (requestFactoryPath == GwtBootstrapPaths.WEB
                || requestFactoryPath == GwtBootstrapPaths.ACCOUNT_WEB) ? projectOperations
                .getPathResolver().getFocusedRoot(SRC_MAIN_WEBAPP)
                : projectOperations.getPathResolver().getFocusedIdentifier(
                        SRC_MAIN_JAVA,
                        requestFactoryPath.getPackagePath(projectOperations
                                .getFocusedTopLevelPackage()));
        updateFile(sourceAntPath, targetDirectory, requestFactoryPath.segmentPackage(),
                false);
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

    private void addPackageToGwtXml(final JavaPackage sourcePackage) {
        String gwtConfig = gwtBootstrapTypeService.getGwtModuleXml(projectOperations
                .getFocusedModuleName());
        gwtConfig = StringUtils.stripEnd(gwtConfig, File.separator);
        final String moduleRoot = projectOperations.getPathResolver()
                .getFocusedRoot(SRC_MAIN_JAVA);
        final String topLevelPackage = gwtConfig.replace(
                FileUtils.ensureTrailingSeparator(moduleRoot), "").replace(
                File.separator, ".");
        final String relativePackage = StringUtils.removeStart(
                sourcePackage.getFullyQualifiedPackageName(), topLevelPackage
                        + ".");
        gwtBootstrapTypeService.addSourcePath(
                relativePackage.replace(".", PATH_DELIMITER),
                projectOperations.getFocusedModuleName());
    }

    private String getPomPath() {
        return projectOperations.getPathResolver().getFocusedIdentifier(ROOT,
                "pom.xml");
    }

    private void updateBuildPlugins(final boolean isGaeEnabled) {
        // Update the POM
        final String xPathExpression = "/configuration/"
                + (isGaeEnabled ? "gae" : "gwt") + "/plugins/plugin";
        final List<Element> pluginElements = XmlUtils.findElements(
                xPathExpression, XmlUtils.getConfiguration(getClass()));
        for (final Element pluginElement : pluginElements) {
            final Plugin defaultPlugin = new Plugin(pluginElement);
            for (final Plugin plugin : projectOperations.getFocusedModule()
                    .getBuildPlugins()) {
                if ("gwt-maven-plugin".equals(plugin.getArtifactId())) {
                    projectOperations.removeBuildPluginImmediately(
                            projectOperations.getFocusedModuleName(),
                            defaultPlugin);
                    break;
                }
            }
            projectOperations.addBuildPlugin(
                    projectOperations.getFocusedModuleName(), defaultPlugin);
        }
    }

    /**
     * Sets the POM's output directory to {@value #OUTPUT_DIRECTORY}, if it's
     * not already set to something else.
     */
    private void updateBuildOutputDirectory() {
        // Read the POM
        final String pom = getPomPath();
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(pom));
        final Element root = document.getDocumentElement();

        Element outputDirectoryElement = XmlUtils.findFirstElement(
                "/project/build/outputDirectory", root);
        if (outputDirectoryElement == null) {
            // Create it
            final Element buildElement = XmlUtils.findRequiredElement(
                    "/project/build", root);
            outputDirectoryElement = DomUtils.createChildElement(
                    "outputDirectory", buildElement, document);
        }
        outputDirectoryElement.setTextContent(OUTPUT_DIRECTORY);

        fileManager.createOrUpdateTextFileIfRequired(pom,
                XmlUtils.nodeToString(document), false);
    }

    private void updateProperties(final Element configuration) {
        final List<Element> properties = XmlUtils.findElements(
                "/configuration/batch/properties/*", configuration);
        for (Element propertyElement : properties) {
            projectOperations.addProperty(
                    projectOperations.getFocusedModuleName(),
                    new Property(propertyElement));
        }
    }

    private void updateDependencies(final Element configuration) {
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        final List<Element> gwtDependencies = XmlUtils.findElements(
                "/configuration/gwt/dependencies/dependency", configuration);
        for (final Element dependencyElement : gwtDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.removeDependencies(
                projectOperations.getFocusedModuleName(), dependencies);
        metadataService
                .evict(ProjectMetadata.getProjectIdentifier(projectOperations
                        .getFocusedModuleName()));
        projectOperations.addDependencies(
                projectOperations.getFocusedModuleName(), dependencies);
    }

    /**
     * Updates the Eclipse plugin in the POM with the necessary GWT details
     */
    private void updateEclipsePlugin() {
        // Load the POM
        final String pom = getPomPath();
        final Document document = XmlUtils.readXml(fileManager
                .getInputStream(pom));
        final Element root = document.getDocumentElement();

        // Add the GWT "buildCommand"
        final Element additionalBuildCommandsElement = XmlUtils
                .findFirstElement(MAVEN_ECLIPSE_PLUGIN
                        + "/configuration/additionalBuildcommands", root);
        Validate.notNull(additionalBuildCommandsElement,
                "additionalBuildcommands element of the maven-eclipse-plugin required");
        Element gwtBuildCommandElement = XmlUtils.findFirstElement(
                "buildCommand[name = '" + GWT_BUILD_COMMAND + "']",
                additionalBuildCommandsElement);
        if (gwtBuildCommandElement == null) {
            gwtBuildCommandElement = DomUtils.createChildElement(
                    "buildCommand", additionalBuildCommandsElement, document);
            final Element nameElement = DomUtils.createChildElement("name",
                    gwtBuildCommandElement, document);
            nameElement.setTextContent(GWT_BUILD_COMMAND);
        }

        // Add the GWT "projectnature"
        final Element additionalProjectNaturesElement = XmlUtils
                .findFirstElement(MAVEN_ECLIPSE_PLUGIN
                        + "/configuration/additionalProjectnatures", root);
        Validate.notNull(additionalProjectNaturesElement,
                "additionalProjectnatures element of the maven-eclipse-plugin required");
        Element gwtProjectNatureElement = XmlUtils.findFirstElement(
                "projectnature[name = '" + GWT_PROJECT_NATURE + "']",
                additionalProjectNaturesElement);
        if (gwtProjectNatureElement == null) {
            gwtProjectNatureElement = new XmlElementBuilder("projectnature",
                    document).setText(GWT_PROJECT_NATURE).build();
            additionalProjectNaturesElement
                    .appendChild(gwtProjectNatureElement);
        }

        fileManager.createOrUpdateTextFileIfRequired(pom,
                XmlUtils.nodeToString(document), false);
    }

    private void updateRepositories(final Element configuration) {
        final List<Repository> repositories = new ArrayList<Repository>();

        final List<Element> gwtRepositories = XmlUtils.findElements(
                "/configuration/gwt/repositories/repository", configuration);
        for (final Element repositoryElement : gwtRepositories) {
            repositories.add(new Repository(repositoryElement));
        }
        projectOperations.addRepositories(
                projectOperations.getFocusedModuleName(), repositories);

        repositories.clear();
        final List<Element> gwtPluginRepositories = XmlUtils.findElements(
                "/configuration/gwt/pluginRepositories/pluginRepository",
                configuration);
        for (final Element repositoryElement : gwtPluginRepositories) {
            repositories.add(new Repository(repositoryElement));
        }
        projectOperations.addPluginRepositories(
                projectOperations.getFocusedModuleName(), repositories);
    }

    private void updateWebXml() {
        final String webXmlpath = projectOperations.getPathResolver()
                .getFocusedIdentifier(SRC_MAIN_WEBAPP, "WEB-INF/web.xml");
        final Document webXml = XmlUtils.readXml(fileManager
                .getInputStream(webXmlpath));
        final Element root = webXml.getDocumentElement();

        WebXmlUtils.addServlet(
                "requestFactory",
                projectOperations.getTopLevelPackage(projectOperations
                        .getFocusedModuleName())
                        + ".server.CustomRequestFactoryServlet", "/gwtRequest",
                null, webXml, null);
        if (typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() != 0) {
            WebXmlUtils
                    .addFilter(
                            "AccountAuthFilter",
                            GwtBootstrapPaths.SERVER_ACCOUNT.packageName(projectOperations
                                    .getTopLevelPackage(projectOperations
                                            .getFocusedModuleName()))
                                    + ".AccountAuthFilter",
                            "/gwtRequest/*",
                            webXml,
                            "This filter makes account authentication services visible to a RequestFactory client.");
            /*final String displayName = "Redirect to the login page if needed before showing any html pages";
            final WebXmlUtils.WebResourceCollection webResourceCollection = new WebXmlUtils.WebResourceCollection(
                    "Login required", null,
                    Collections.singletonList("*.html"),
                    new ArrayList<String>());
            final ArrayList<String> roleNames = new ArrayList<String>();
            roleNames.add("*");
            final String userDataConstraint = null;
            WebXmlUtils.addSecurityConstraint(displayName,
                    Collections.singletonList(webResourceCollection),
                    roleNames, userDataConstraint, webXml, null);*/
        }
        else {
            final Element filter = XmlUtils.findFirstElement(
                    "/web-app/filter[filter-name = 'AccountAuthFilter']", root);
            if (filter != null) {
                filter.getParentNode().removeChild(filter);
            }
            final Element filterMapping = XmlUtils.findFirstElement(
                    "/web-app/filter-mapping[filter-name = 'AccountAuthFilter']",
                    root);
            if (filterMapping != null) {
                filterMapping.getParentNode().removeChild(filterMapping);
            }
            /*final Element securityConstraint = XmlUtils.findFirstElement(
                    "security-constraint", root);
            if (securityConstraint != null) {
                securityConstraint.getParentNode().removeChild(
                        securityConstraint);
            }*/
        }

        removeIfFound("/web-app/error-page", root);

        fileManager.createOrUpdateTextFileIfRequired(webXmlpath,
                XmlUtils.nodeToString(webXml), false);
    }

    private void removeIfFound(final String xpath, final Element webXmlRoot) {
        for (Element toRemove : XmlUtils.findElements(xpath, webXmlRoot)) {
            if (toRemove != null) {
                toRemove.getParentNode().removeChild(toRemove);
                toRemove = null;
            }
        }
    }
}