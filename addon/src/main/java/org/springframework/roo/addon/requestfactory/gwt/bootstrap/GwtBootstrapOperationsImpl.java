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
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;
import static org.springframework.roo.model.SpringJavaType.LOCAL_CONTAINER_ENTITY_MANAGER_FACTORY_BEAN;
import static org.springframework.roo.project.Path.ROOT;
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;
import static org.springframework.roo.project.Path.SRC_MAIN_WEBAPP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.roo.addon.jpa.GaeOperations;
import org.springframework.roo.addon.requestfactory.BaseOperationsImpl;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.gwt.bootstrap.RooGwtBootstrapScaffold;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.FeatureNames;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
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
 * Implementation of {@link GwtBootstrapOperations}.
 */
@Component
@Service
public class GwtBootstrapOperationsImpl extends BaseOperationsImpl
        implements GwtBootstrapOperations {

    private static final String FEATURE_NAME = "gwtbootstrap";
    private static final String GWT_BUILD_COMMAND = "com.google.gwt.eclipse.core.gwtProjectValidator";
    private static final String GWT_PROJECT_NATURE = "com.google.gwt.eclipse.core.gwtNature";
    private static final String MAVEN_ECLIPSE_PLUGIN = "/project/build/plugins/plugin[artifactId = 'maven-eclipse-plugin']";
    private static final String OUTPUT_DIRECTORY = "${project.build.directory}/${project.build.finalName}/WEB-INF/classes";

    private static final JavaSymbolName MODULE_SYMBOL_NAME = new JavaSymbolName(
            RooGwtBootstrapScaffold.MODULE_ATTRIBUTE);

    @Reference protected RequestFactoryTemplateService requestFactoryTemplateService;
    @Reference protected GwtBootstrapTypeService gwtBootstrapTypeService;
    @Reference protected WebMvcOperations webMvcOperations;
    @Reference protected PathResolver pathResolver;

    private Boolean wasGaeEnabled;

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

        boolean isGaeEnabled = projectOperations.isFeatureInstalledInModule(
                FeatureNames.GAE, getEntityModuleName());

        // Add POM properties
        updateProperties(configuration, isGaeEnabled);

        // Add POM repositories
        updateRepositories(configuration, isGaeEnabled);

        // Add dependencies
        updateDependencies(configuration, isGaeEnabled);

        // Update web.xml
        updateWebXml();

        // Update gwt-maven-plugin and others
        updateBuildPlugins(configuration, isGaeEnabled);
    }

    @Override
    public void scaffoldAll(Pom module) {
        if (module == null) {
            module = projectOperations.getFocusedModule();
        }
        updateScaffoldBoilerPlate(module);
        for (final ClassOrInterfaceTypeDetails proxy : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)) {
            final ClassOrInterfaceTypeDetails request = gwtBootstrapTypeService
                    .lookupRequestFromProxy(proxy);
            if (request == null) {
                throw new IllegalStateException(
                        "In order to scaffold, an entity must have a request");
            }
            createScaffold(proxy, module);
        }
    }

    @Override
    public void scaffoldType(final JavaType type, Pom module) {
        if (module == null) {
            module = projectOperations.getFocusedModule();
        }
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
            updateScaffoldBoilerPlate(module);
            createScaffold(proxy, module);
        }
    }

    private static final String PROPERTY = "property";
    private static final String ENTRY = "entry";
    private static final String NAME = "name";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String TRUE = "true";
    private static final String FALSE = "false";

    public void updateGaeConfiguration() {
        final boolean isGaeEnabled = projectOperations
                .isFeatureInstalled(FeatureNames.GAE);
        final boolean hasGaeStateChanged = wasGaeEnabled == null
                || isGaeEnabled != wasGaeEnabled;
        if (!isInstalledInModule(projectOperations.getFocusedModuleName())
                || !hasGaeStateChanged) {
            return;
        }

        final String moduleName = projectOperations
                .getFocusedProjectMetadata().getModuleName();

        wasGaeEnabled = isGaeEnabled;

        // Update the GaeHelper type
//        updateGaeHelper();

        /*gwtBootstrapTypeService.buildType(RequestFactoryType.APP_REQUEST_FACTORY,
                requestFactoryTemplateService.getStaticTemplateTypeDetails(
                        RequestFactoryType.APP_REQUEST_FACTORY, moduleName),
                        moduleName);*/

        // Ensure the gwt-maven-plugin appropriate to a GAE enabled or disabled
        // environment is updated
        final Element configuration = XmlUtils.getConfiguration(getClass());
        updateBuildPlugins(configuration, isGaeEnabled);

        // If there is a class that could possibly import from the appengine
        // sdk, denoted here as having Gae in the type name,
        // then we need to add the appengine-api-1.0-sdk dependency to the
        // pom.xml file
        final String rootPath = projectOperations.getPathResolver()
                .getFocusedRoot(ROOT);
        final Set<FileDetails> files = fileManager.findMatchingAntPath(rootPath
                + "/**/*Gae*.java");
        if (!files.isEmpty()) {
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
            copyDirectoryContents(moduleName);
        }


        final String entityModuleName = getEntityModuleName();
        final String focusedModuleName = projectOperations.getFocusedModuleName();

        if (!entityModuleName.equals(focusedModuleName) && isGaeEnabled) {
            final String appenginePath = pathResolver.getFocusedIdentifier(
                    Path.SRC_MAIN_WEBAPP, "WEB-INF/appengine-web.xml");
            final boolean appenginePathExists = fileManager.exists(appenginePath);

            final InputStream inputStream;
            if (appenginePathExists) {
                inputStream = fileManager.getInputStream(appenginePath);
            }
            else {
                inputStream = FileUtils.getInputStream(GaeOperations.class,
                        "appengine-web-template.xml");
            }
            final Document appengine = XmlUtils.readXml(inputStream);

            final Element root = appengine.getDocumentElement();
            final Element applicationElement = XmlUtils.findFirstElement(
                    "/appengine-web-app/application", root);
            final String artifactId = projectOperations
                    .getPomFromModuleName(moduleName).getArtifactId();
            final String textContent = StringUtils.defaultIfEmpty(artifactId,
                    projectOperations.getProjectName(moduleName));
            if (!textContent.equals(applicationElement.getTextContent())) {
                applicationElement.setTextContent(textContent);
                fileManager.createOrUpdateTextFileIfRequired(appenginePath,
                        XmlUtils.nodeToString(appengine), false);
            }


            final String persistenceXmlPath = projectOperations.getPathResolver()
                    .getIdentifier(LogicalPath.getInstance(Path.SRC_MAIN_RESOURCES,
                            entityModuleName), "META-INF/persistence.xml");

            final String contextPath = projectOperations.getPathResolver()
                    .getIdentifier(LogicalPath.getInstance(Path.SPRING_CONFIG_ROOT,
                            entityModuleName), "applicationContext.xml");

            final Document appCtx = XmlUtils.readXml(fileManager
                    .getInputStream(contextPath));
            final Element rootCtx = appCtx.getDocumentElement();
            final Element entityManagerFactory = XmlUtils.findFirstElement(
                    "/beans/bean[@id = 'entityManagerFactory']", rootCtx);

            if (entityManagerFactory != null && !entityManagerFactory
                    .getAttribute("class").equals(
                            LOCAL_CONTAINER_ENTITY_MANAGER_FACTORY_BEAN
                            .getFullyQualifiedTypeName())) {

                entityManagerFactory.setAttribute("class",
                        LOCAL_CONTAINER_ENTITY_MANAGER_FACTORY_BEAN
                        .getFullyQualifiedTypeName());

                final Element pacakgesToScan = appCtx.createElement(PROPERTY);
                pacakgesToScan.setAttribute(NAME, "packagesToScan");
                pacakgesToScan.setAttribute(VALUE, projectOperations
                        .getTopLevelPackage(entityModuleName)
                        .getFullyQualifiedPackageName());
                entityManagerFactory.appendChild(pacakgesToScan);

                final Element persistenceProviderClass = appCtx.createElement(PROPERTY);
                persistenceProviderClass.setAttribute(NAME, "persistenceProviderClass");
                persistenceProviderClass.setAttribute(VALUE, "org.datanucleus.api.jpa.PersistenceProviderImpl");
                entityManagerFactory.appendChild(persistenceProviderClass);

                final Element jpaPropertyMap = appCtx.createElement(PROPERTY);
                jpaPropertyMap.setAttribute(NAME, "jpaPropertyMap");
                entityManagerFactory.appendChild(jpaPropertyMap);

                final Element map = appCtx.createElement("map");
                jpaPropertyMap.appendChild(map);

                if (fileManager.exists(contextPath)) {
                    final Document persistenceXml = XmlUtils.readXml(fileManager
                            .getInputStream(persistenceXmlPath));
                    final Element rootPersistenceXml = persistenceXml.getDocumentElement();

                    for (final Element propertyElement : XmlUtils.findElements(
                            "/persistence/persistence-unit/properties/*",
                            rootPersistenceXml)) {
                        final Element property = appCtx.createElement(ENTRY);
                        property.setAttribute(KEY, propertyElement.getAttribute(NAME));
                        property.setAttribute(VALUE, propertyElement.getAttribute(VALUE));
                        map.appendChild(property);
                    }
                } else {
                    final Element nontransactionalRead = appCtx.createElement(ENTRY);
                    nontransactionalRead.setAttribute(KEY, "datanucleus.NontransactionalRead");
                    nontransactionalRead.setAttribute(VALUE, TRUE);
                    map.appendChild(nontransactionalRead);

                    final Element nontransactionalWrite = appCtx.createElement(ENTRY);
                    nontransactionalWrite.setAttribute(KEY, "datanucleus.NontransactionalWrite");
                    nontransactionalWrite.setAttribute(VALUE, TRUE);
                    map.appendChild(nontransactionalWrite);

                    final Element autoCreateSchema = appCtx.createElement(ENTRY);
                    autoCreateSchema.setAttribute(KEY, "datanucleus.autoCreateSchema");
                    autoCreateSchema.setAttribute(VALUE, FALSE);
                    map.appendChild(autoCreateSchema);

                    final Element connectionURL = appCtx.createElement(ENTRY);
                    connectionURL.setAttribute(KEY, "datanucleus.ConnectionURL");
                    connectionURL.setAttribute(VALUE, "appengine");
                    map.appendChild(connectionURL);

                    final Element connectionUserName = appCtx.createElement(ENTRY);
                    connectionUserName.setAttribute(KEY, "datanucleus.ConnectionUserName");
                    connectionUserName.setAttribute(VALUE, "");
                    map.appendChild(connectionUserName);

                    final Element connectionPassword = appCtx.createElement(ENTRY);
                    connectionPassword.setAttribute(KEY, "datanucleus.ConnectionPassword");
                    connectionPassword.setAttribute(VALUE, "");
                    map.appendChild(connectionPassword);

                    final Element autoCreateTables = appCtx.createElement(ENTRY);
                    autoCreateTables.setAttribute(KEY, "datanucleus.autoCreateTables");
                    autoCreateTables.setAttribute(VALUE, TRUE);
                    map.appendChild(autoCreateTables);

                    final Element autoCreateColumns = appCtx.createElement(ENTRY);
                    autoCreateColumns.setAttribute(KEY, "datanucleus.autoCreateColumns");
                    autoCreateColumns.setAttribute(VALUE, FALSE);
                    map.appendChild(autoCreateColumns);

                    final Element autoCreateConstraints = appCtx.createElement(ENTRY);
                    autoCreateConstraints.setAttribute(KEY, "datanucleus.autoCreateConstraints");
                    autoCreateConstraints.setAttribute(VALUE, FALSE);
                    map.appendChild(autoCreateConstraints);

                    final Element validateTables = appCtx.createElement(ENTRY);
                    validateTables.setAttribute(KEY, "datanucleus.validateTables");
                    validateTables.setAttribute(VALUE, FALSE);
                    map.appendChild(validateTables);

                    final Element validateConstraints = appCtx.createElement(ENTRY);
                    validateConstraints.setAttribute(KEY, "datanucleus.validateConstraints");
                    validateConstraints.setAttribute(VALUE, FALSE);
                    map.appendChild(validateConstraints);

                    final Element addClassTransformer = appCtx.createElement(ENTRY);
                    addClassTransformer.setAttribute(KEY, "datanucleus.jpa.addClassTransformer");
                    addClassTransformer.setAttribute(VALUE, FALSE);
                    map.appendChild(addClassTransformer);
                }
                fileManager.createOrUpdateTextFileIfRequired(contextPath,
                        XmlUtils.nodeToString(rootCtx), false);
            }

            fileManager.delete(persistenceXmlPath);
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

    private void updateScaffoldBoilerPlate(final Pom module) {
        Validate.notNull(module);
        final String moduleName = module.getModuleName();
        final LogicalPath path = LogicalPath.getInstance(SRC_MAIN_JAVA,
                moduleName);
        final String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(path, projectOperations
                                .getTopLevelPackage(moduleName)
                                .getFullyQualifiedPackageName()
                                .replace('.', File.separatorChar));
        deleteUntouchedSetupFiles("setup/*", targetDirectory);
        deleteUntouchedSetupFiles("setup/client/*", targetDirectory + "/client");
        copyDirectoryContents(moduleName);
//        updateGaeHelper();
        updateGwtModuleInheritance(moduleName);
    }

    private void createScaffold(final ClassOrInterfaceTypeDetails proxy, final Pom module) {
        Validate.notNull(module, "GWT scaffold module required");
        final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                .getFirstAnnotation(proxy, ROO_GWT_BOOTSTRAP_SCAFFOLD);
        if (annotationMetadata == null) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                    new ClassOrInterfaceTypeDetailsBuilder(proxy);
            final AnnotationMetadataBuilder annotationMetadataBuilder =
                    new AnnotationMetadataBuilder(ROO_GWT_BOOTSTRAP_SCAFFOLD);
            final StringAttributeValue moduleAttributeValue = new StringAttributeValue(
                    MODULE_SYMBOL_NAME, module.getModuleName());
            annotationMetadataBuilder.addAttribute(moduleAttributeValue);
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

    private void updateGwtModuleInheritance(final String scaffoldModule) {
        final String proxyModule = getProxyModuleName();
        if (!proxyModule.equals(scaffoldModule)) {
            copyDirectoryContents(GwtBootstrapPaths.SHARED_MODULE, proxyModule);

            String inherits = projectOperations.getTopLevelPackage(proxyModule)
                    + "." + GwtBootstrapPaths.SHARED_MODULE_NAME;
            gwtBootstrapTypeService.addInheritsModule(inherits, scaffoldModule);

            updatePropertiesAndPlugins(proxyModule);
        }

        final String entityModule = getEntityModuleName();
        if (!entityModule.equals(scaffoldModule)) {
            copyDirectoryContents(GwtBootstrapPaths.DOMAIN_MODULE, entityModule);

            String inherits = projectOperations.getTopLevelPackage(entityModule)
                    + "." + GwtBootstrapPaths.DOMAIN_MODULE_NAME;
            gwtBootstrapTypeService.addInheritsModule(inherits, scaffoldModule);

            updatePropertiesAndPlugins(entityModule);
        }
    }

    private String getProxyModuleName() {
        final ClassOrInterfaceTypeDetails proxy = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)
                .iterator().next();
        final String proxyModule = PhysicalTypeIdentifier.getPath(
                proxy.getDeclaredByMetadataId()).getModule();
        return proxyModule;
    }

    private String getEntityModuleName() {
        final ClassOrInterfaceTypeDetails entity = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_JPA_ENTITY,
                        ROO_JPA_ACTIVE_RECORD, ROO_MONGO_ENTITY).iterator().next();
        final String entityModule = PhysicalTypeIdentifier.getPath(
                entity.getDeclaredByMetadataId()).getModule();
        return entityModule;
    }

    private void updatePropertiesAndPlugins(final String moduleName) {
        final Element configuration = XmlUtils.getConfiguration(getClass());

        final List<Element> properties = XmlUtils.findElements(
                "/configuration/gwt/properties/*", configuration);
        for (Element propertyElement : properties) {
            projectOperations.addProperty(moduleName,
                    new Property(propertyElement));
        }

        final List<Element> plugins = XmlUtils.findElements(
                "/configuration/shared/plugins/plugin",
                configuration);
        for (final Element plugin : plugins) {
            projectOperations.addBuildPlugin(moduleName,
                    new Plugin(plugin));
        }
    }

    private void copyDirectoryContents(final String moduleName) {
        for (final RequestFactoryPath path : GwtBootstrapPaths.ALL_PATHS) {
            copyDirectoryContents(path, moduleName);
        }
    }

    private void copyDirectoryContents(final RequestFactoryPath requestFactoryPath,
            final String moduleName) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("gae")
                && !projectOperations
                        .isFeatureInstalledInModule(FeatureNames.GAE, moduleName)) {
            return;
        }
        if (sourceAntPath.contains("account")
                && typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        final String targetDirectory;
        final LogicalPath path;
        if (requestFactoryPath == GwtBootstrapPaths.WEB
                || requestFactoryPath == GwtBootstrapPaths.ACCOUNT_WEB) {
            path = LogicalPath.getInstance(SRC_MAIN_WEBAPP, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getRoot(path);
        } else {
            path = LogicalPath.getInstance(SRC_MAIN_JAVA, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, requestFactoryPath
                            .getPackagePath(projectOperations
                                    .getTopLevelPackage(moduleName)));
        }
        updateFile(sourceAntPath, targetDirectory, requestFactoryPath.segmentPackage(),
                false);
    }

    /*private void addPackageToGwtXml(final JavaPackage sourcePackage) {
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
    }*/

    private String getPomPath() {
        return projectOperations.getPathResolver().getFocusedIdentifier(ROOT,
                "pom.xml");
    }

    private void updateBuildPlugins(final Element configuration,
            final boolean isGaeEnabled) {
        // Update the POM
        final String xPathExpression = "/configuration/"
                + (isGaeEnabled ? "gae" : "gwt") + "/plugins/plugin";
        final List<Element> pluginElements = XmlUtils.findElements(
                xPathExpression, configuration);
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

    private void updateProperties(final Element configuration,
            final boolean isGaeEnabled) {
        for (Element propertyElement : XmlUtils.findElements(
                "/configuration/gwt/properties/*", configuration)) {
            projectOperations.addProperty(
                    projectOperations.getFocusedModuleName(),
                    new Property(propertyElement));
        }

        if (isGaeEnabled) {
            for (Element propertyElement : XmlUtils.findElements(
                    "/configuration/gae/properties/*", configuration)) {
                projectOperations.addProperty(
                        projectOperations.getFocusedModuleName(),
                        new Property(propertyElement));
            }
        }
    }

    private void updateDependencies(final Element configuration,
            final boolean isGaeEnabled) {
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        for (final Element dependencyElement : XmlUtils.findElements(
                "/configuration/gwt/dependencies/dependency", configuration)) {
            dependencies.add(new Dependency(dependencyElement));
        }
        if (isGaeEnabled) {
            for (final Element dependencyElement : XmlUtils.findElements(
                    "/configuration/gae/dependencies/dependency", configuration)) {
                dependencies.add(new Dependency(dependencyElement));
            }
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

    private void updateRepositories(final Element configuration,
            final boolean isGaeEnabled) {
        final List<Repository> repositories = new ArrayList<Repository>();

        for (final Element repositoryElement : XmlUtils.findElements(
                "/configuration/gwt/repositories/repository", configuration)) {
            repositories.add(new Repository(repositoryElement));
        }
        if (isGaeEnabled) {
            for (final Element repositoryElement : XmlUtils.findElements(
                    "/configuration/gae/repositories/repository", configuration)) {
                repositories.add(new Repository(repositoryElement));
            }
        }
        projectOperations.addRepositories(
                projectOperations.getFocusedModuleName(), repositories);

        repositories.clear();
        for (final Element repositoryElement : XmlUtils.findElements(
                "/configuration/gwt/pluginRepositories/pluginRepository",
                configuration)) {
            repositories.add(new Repository(repositoryElement));
        }
        if (isGaeEnabled) {
            for (final Element repositoryElement : XmlUtils.findElements(
                    "/configuration/gae/pluginRepositories/pluginRepository",
                    configuration)) {
                repositories.add(new Repository(repositoryElement));
            }
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
                            RequestFactoryPath.SERVER_ACCOUNT.packageName(projectOperations
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