package org.springframework.roo.addon.requestfactory;

import static java.lang.reflect.Modifier.PUBLIC;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ENTITY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.PROXY_FOR_NAME;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.REQUEST_CONTEXT;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_REQUEST;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_UNMANAGED_REQUEST;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.EXCLUDE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.READ_ONLY;
import static org.springframework.roo.classpath.PhysicalTypeCategory.INTERFACE;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.annotations.RooRequestFactoryProxy;
import org.springframework.roo.addon.requestfactory.annotations.account.RooAccount;
import org.springframework.roo.addon.requestfactory.request.RequestFactoryRequestMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ArrayAttributeValue;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.osgi.OSGiUtils;
import org.springframework.roo.support.util.CollectionUtils;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;


/**
 * Implementation of {@link RequestFactoryOperations}.
 *
 * @author Ben Alex
 * @author Alan Stewart
 * @author Stefan Schmidt
 * @author Ray Cromwell
 * @author Amit Manjhi
 * @since 1.1
 */
@Component
@Service
public class RequestFactoryOperationsImpl implements RequestFactoryOperations {

    private static final String FEATURE_NAME = "requestfactory";
    private static final String REQUEST_FACTORY_GROUP_ID = "com.google.web.bindery";

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaSymbolName SERVER_MODULE = new JavaSymbolName(
            RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE);

    /**
     * Use TypeManagementService to change types
     */
    @Reference TypeManagementService typeManagementService;

    /**
     * Use TypeLocationService to find types which are annotated with a given
     * annotation in the project
     */
    @Reference TypeLocationService typeLocationService;

    /**
     * Use ProjectOperations to install new dependencies, plugins, properties,
     * etc into the project configuration
     */
    @Reference ProjectOperations projectOperations;

    @Reference FileManager fileManager;
    @Reference MetadataService metadataService;
    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;

    private ComponentContext context;

    protected void activate(final ComponentContext context) {
        this.context = context;
    }

    @Override
    public boolean isRequestFactoryAddonInstallationPossible() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @Override
    public boolean isRequestFactoryServerInstallationPossible() {
        return projectOperations.isFocusedProjectAvailable()
                && !isRequestFactoryInstalled(projectOperations
                        .getFocusedModuleName(), true);
    }

    @Override
    public boolean isRequestFactoryClientInstallationPossible() {
        return projectOperations.isFocusedProjectAvailable()
                && !isRequestFactoryInstalled(projectOperations
                        .getFocusedModuleName(), false);
    }

    @Override
    public boolean isRequestFactoryCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable()
                && isInstalledInModule(projectOperations.getFocusedModuleName());
    }

    @Override
    public boolean isScaffoldAvailable() {
        return projectOperations.isFocusedProjectAvailable()
                && typeLocationService.findClassesOrInterfaceDetailsWithAnnotation(
                        ROO_REQUEST_FACTORY_PROXY).size() > 0;
    }

    @Override
    public void setupRequestFactoryAddon() {
        setupRequestFactory("addon");
    }

    @Override
    public void setupRequestFactoryServer() {
        setupRequestFactory("server");
    }

    @Override
    public void setupRequestFactoryClient() {
        setupRequestFactory("client");
    }

    @Override
    public void setupRequestFactory(final String section) {
        final Element configuration = XmlUtils.getConfiguration(getClass());
        final String focusedModuleName = projectOperations
                .getFocusedModuleName();

        // update properties
        final List<Element> properties = XmlUtils.findElements(
                "/configuration/" + section + "/properties/*", configuration);
        for (Element propertyElement : properties) {
            projectOperations.addProperty(focusedModuleName,
                    new Property(propertyElement));
        }

        // update dependencies
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        final List<Element> serverDependencies = XmlUtils.findElements(
                "/configuration/" + section
                + "/dependencies/dependency",
                configuration);
        for (final Element dependencyElement : serverDependencies) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.removeDependencies(focusedModuleName, dependencies);
        metadataService.evict(ProjectMetadata
                .getProjectIdentifier(focusedModuleName));
        projectOperations.addDependencies(focusedModuleName, dependencies);
    }

    @Override
    public void proxyAll(final JavaPackage proxyPackage, final Pom serverModule) {
        for (final ClassOrInterfaceTypeDetails entity : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_JPA_ENTITY,
                        ROO_JPA_ACTIVE_RECORD, ROO_MONGO_ENTITY)) {
            createProxy(entity, proxyPackage, serverModule);
        }
        //copyDirectoryContents(RequestFactoryPath.LOCATOR);
    }

    @Override
    public void proxyType(final JavaPackage proxyPackage, final JavaType type,
            final Pom serverModule) {
        final ClassOrInterfaceTypeDetails entity = typeLocationService
                .getTypeDetails(type);
        if (entity != null) {
            createProxy(entity, proxyPackage, serverModule);
        }
        //copyDirectoryContents(RequestFactoryPath.LOCATOR);
    }

    @Override
    public void requestAll(final JavaPackage proxyPackage) {
        for (final ClassOrInterfaceTypeDetails entity : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_JPA_ENTITY,
                        ROO_JPA_ACTIVE_RECORD, ROO_MONGO_ENTITY)) {
            createRequestInterfaceIfNecessary(entity, proxyPackage);
        }
    }

    @Override
    public void requestType(final JavaPackage requestPackage,
            final JavaType type) {
        createRequestInterfaceIfNecessary(
                typeLocationService.getTypeDetails(type), requestPackage);
    }

    @Override
    public void scaffoldAll() {
        final Set<ClassOrInterfaceTypeDetails> proxys = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);

        final String focusedModule = projectOperations.getFocusedModuleName();
        final String serverModule = RequestFactoryUtils.getStringAnnotationValue(proxys
                .iterator().next(), ROO_REQUEST_FACTORY_PROXY,
                RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE, focusedModule);

        copyServerDirectoryContents(serverModule);
        copySharedDirectoryContents(focusedModule);

        for (final ClassOrInterfaceTypeDetails proxy : proxys) {
            final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                    .lookupRequestFromProxy(proxy);
            if (request == null) {
                throw new IllegalStateException(
                        "In order to scaffold, an entity must have a request");
            }
            createScaffold(proxy);
        }
    }

    @Override
    public void scaffoldType(final JavaType type) {
        final ClassOrInterfaceTypeDetails entity = typeLocationService
                .getTypeDetails(type);
        if (entity != null && !entity.isAbstract()) {
            final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                    .lookupProxyFromEntity(entity);
            final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                    .lookupRequestFromEntity(entity);
            if (proxy == null || request == null) {
                throw new IllegalStateException(
                        "Entity must have an associated proxy and request");
            }
            createScaffold(proxy);
        }
    }

    @Override
    public String getName() {
        return FEATURE_NAME;
    }

    @Override
    public boolean isInstalledInModule(final String moduleName) {
        return isRequestFactoryInstalled(moduleName, true);
    }

    @Override
    public boolean isRequestFactoryInstalled(final String moduleName, boolean server) {
        final Pom pom = projectOperations.getPomFromModuleName(moduleName);
        if (pom == null) {
            return false;
        }
        for (final Dependency dependency : pom.getDependencies()) {
            if (REQUEST_FACTORY_GROUP_ID.equals(dependency.getGroupId())
                    && dependency.getArtifactId() != null
                    && dependency.getArtifactId().startsWith("requestfactory-"
                            + (server ? "server" : "client"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createProxy(final ClassOrInterfaceTypeDetails entity,
            final JavaPackage destinationPackage, final Pom serverModule) {
        final ClassOrInterfaceTypeDetails existingProxy = requestFactoryTypeService
                .lookupProxyFromEntity(entity);
        if (existingProxy != null || entity.isAbstract()) {
            return;
        }

        final JavaType proxyType = new JavaType(
                destinationPackage.getFullyQualifiedPackageName() + "."
                        + entity.getName().getSimpleTypeName() + "Proxy");
        final String focusedModule = projectOperations.getFocusedModuleName();
        final LogicalPath proxyLogicalPath = LogicalPath.getInstance(
                SRC_MAIN_JAVA, focusedModule);
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                PhysicalTypeIdentifier.createIdentifier(proxyType,
                        proxyLogicalPath));
        cidBuilder.setName(proxyType);
        cidBuilder.setExtendsTypes(Collections.singletonList(ENTITY_PROXY));
        cidBuilder.setPhysicalTypeCategory(INTERFACE);
        cidBuilder.setModifier(PUBLIC);
        final List<AnnotationAttributeValue<?>> attributeValues = new ArrayList<AnnotationAttributeValue<?>>();
        final StringAttributeValue stringAttributeValue = new StringAttributeValue(
                VALUE, entity.getName().getFullyQualifiedTypeName());
        attributeValues.add(stringAttributeValue);

        final String locator = projectOperations
                .getTopLevelPackage(serverModule.getModuleName()) + ".locator."
                + entity.getName().getSimpleTypeName() + "Locator";
        final StringAttributeValue locatorAttributeValue = new StringAttributeValue(
                new JavaSymbolName("locator"), locator);
        attributeValues.add(locatorAttributeValue);
        cidBuilder.updateTypeAnnotation(new AnnotationMetadataBuilder(
                PROXY_FOR_NAME, attributeValues));
        attributeValues.remove(locatorAttributeValue);

        if (serverModule != null) {
            final StringAttributeValue serverModuleAttributeValue = new StringAttributeValue(
                    SERVER_MODULE, serverModule.getModuleName());
            attributeValues.add(serverModuleAttributeValue);
        }

        final List<StringAttributeValue> readOnlyValues = new ArrayList<StringAttributeValue>();
        final List<StringAttributeValue> excludeValues = new ArrayList<StringAttributeValue>();

        final FieldMetadata versionField = persistenceMemberLocator
                .getVersionField(entity.getName());
        if (versionField != null) {
            readOnlyValues.add(new StringAttributeValue(VALUE, versionField
                    .getFieldName().getSymbolName()));
        }

        final List<FieldMetadata> idFields = persistenceMemberLocator
                .getIdentifierFields(entity.getName());
        if (!CollectionUtils.isEmpty(idFields)) {
            readOnlyValues.add(new StringAttributeValue(VALUE,
                    idFields.get(0).getFieldName().getSymbolName()));
            if (idFields.get(0).getFieldType().equals(KEY)) {
                readOnlyValues.add(new StringAttributeValue(VALUE, "stringId"));
            }
        }
        for (FieldMetadata fieldMetadata : entity.getFieldsWithAnnotation(READ_ONLY)) {
            readOnlyValues.add(new StringAttributeValue(VALUE, fieldMetadata.getFieldName().getSymbolName()));
        }
        final ArrayAttributeValue<StringAttributeValue> readOnlyAttribute = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("readOnly"), readOnlyValues);
        attributeValues.add(readOnlyAttribute);

        for (FieldMetadata fieldMetadata : entity.getFieldsWithAnnotation(EXCLUDE)) {
            excludeValues.add(new StringAttributeValue(VALUE, fieldMetadata.getFieldName().getSymbolName()));
        }
        for (MethodMetadata methodMetadata : entity.getMethods()) {
            if (methodMetadata.getAnnotation(EXCLUDE) != null) {
                excludeValues.add(new StringAttributeValue(VALUE, StringUtils.uncapitalize(BeanInfoUtils
                        .getPropertyNameForJavaBeanMethod(methodMetadata)
                        .getSymbolName())));
            }
        }
        final ArrayAttributeValue<StringAttributeValue> excludeAttribute = new ArrayAttributeValue<StringAttributeValue>(
                new JavaSymbolName("exclude"), excludeValues);
        attributeValues.add(excludeAttribute);

        cidBuilder.updateTypeAnnotation(new AnnotationMetadataBuilder(
                ROO_REQUEST_FACTORY_PROXY, attributeValues));
        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
//        addPackageToGwtXml(destinationPackage);
    }

    /**
     * Builds the given entity's managed RequestContext interface. Note that we
     * don't generate the entire interface here, only the @RooGwtRequest
     * annotation; we then invoke the metadata provider, which takes over and
     * generates the remaining code, namely the method declarations and the @ServiceName
     * annotation. This is analogous to how ITD-based addons work, e.g. adding a
     * trigger annotation and letting the metadata provider do the rest. This
     * allows for the metadata provider to correctly respond to project changes.
     *
     * @param entity the entity for which to create the GWT request interface
     *            (required)
     * @param destinationPackage the package in which to create the request
     *            interface (required)
     */
    @Override
    public void createRequestInterface(
            final ClassOrInterfaceTypeDetails entity,
            final JavaPackage destinationPackage) {
        final JavaType requestType = new JavaType(
                destinationPackage.getFullyQualifiedPackageName() + "."
                        + entity.getType().getSimpleTypeName()
                        + "Request_Roo");
        final LogicalPath focusedSrcMainJava = LogicalPath.getInstance(
                SRC_MAIN_JAVA, projectOperations.getFocusedModuleName());
        final ClassOrInterfaceTypeDetailsBuilder requestBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                PhysicalTypeIdentifier.createIdentifier(requestType,
                        focusedSrcMainJava));
        requestBuilder.setName(requestType);
        requestBuilder.addExtendsTypes(REQUEST_CONTEXT);
        requestBuilder.setPhysicalTypeCategory(INTERFACE);
        requestBuilder.setModifier(PUBLIC);
        requestBuilder.addAnnotation(getRooGwtRequestAnnotation(entity));
        typeManagementService.createOrUpdateTypeOnDisk(requestBuilder.build());
//        addPackageToGwtXml(destinationPackage);
        // Trigger the GwtRequestMetadataProvider to finish generating the code
        metadataService.get(RequestFactoryRequestMetadata.createIdentifier(requestType,
                focusedSrcMainJava));
    }

    /**
     * Builds the given entity's unmanaged RequestContext interface used for
     * adding custom methods. This interface extends the RequestContext
     * interface managed by Roo.
     *
     * @param entity the entity for which to create the GWT request interface
     *            (required)
     * @param destinationPackage the package in which to create the request
     *            interface (required)
     */
    @Override
    public void createUnmanagedRequestInterface(
            final ClassOrInterfaceTypeDetails entity,
            JavaPackage destinationPackage) {
        final ClassOrInterfaceTypeDetails managedRequest = requestFactoryTypeService
                .lookupRequestFromEntity(entity);

        if (managedRequest == null)
            return;

        final JavaType unmanagedRequestType = new JavaType(
                destinationPackage.getFullyQualifiedPackageName() + "."
                        + entity.getType().getSimpleTypeName() + "Request");

        final LogicalPath focusedSrcMainJava = LogicalPath.getInstance(
                SRC_MAIN_JAVA, projectOperations.getFocusedModuleName());
        final ClassOrInterfaceTypeDetailsBuilder unmanagedRequestBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                PhysicalTypeIdentifier.createIdentifier(unmanagedRequestType,
                        focusedSrcMainJava));
        unmanagedRequestBuilder.setName(unmanagedRequestType);
        unmanagedRequestBuilder.addExtendsTypes(managedRequest.getType());
        unmanagedRequestBuilder.setPhysicalTypeCategory(INTERFACE);
        unmanagedRequestBuilder.setModifier(PUBLIC);
        unmanagedRequestBuilder
                .addAnnotation(getRooGwtUnmanagedRequestAnnotation(entity));
        unmanagedRequestBuilder.addAnnotation(managedRequest
                .getAnnotation(RequestFactoryJavaType.SERVICE_NAME));
        typeManagementService.createOrUpdateTypeOnDisk(unmanagedRequestBuilder
                .build());

    }

    @Override
    public void createRequestInterfaceIfNecessary(
            final ClassOrInterfaceTypeDetails entity,
            final JavaPackage destinationPackage) {
        if (entity != null && !entity.isAbstract()
                && requestFactoryTypeService.lookupRequestFromEntity(entity) == null) {
            createRequestInterface(entity, destinationPackage);

            createUnmanagedRequestInterface(entity, destinationPackage);
        }
    }

    @Override
    public AnnotationMetadata getRooGwtRequestAnnotation(
            final ClassOrInterfaceTypeDetails entity) {
        // The GwtRequestMetadataProvider doesn't need to know excluded methods
        // any more because it actively adds the required CRUD methods itself.
        final StringAttributeValue entityAttributeValue = new StringAttributeValue(
                VALUE, entity.getType().getFullyQualifiedTypeName());
        final List<AnnotationAttributeValue<?>> gwtRequestAttributeValues = 
                new ArrayList<AnnotationAttributeValue<?>>();
        gwtRequestAttributeValues.add(entityAttributeValue);
        return new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_REQUEST,
                gwtRequestAttributeValues).build();
    }

    @Override
    public AnnotationMetadata getRooGwtUnmanagedRequestAnnotation(
            final ClassOrInterfaceTypeDetails entity) {
        final StringAttributeValue entityAttributeValue = new StringAttributeValue(
                VALUE, entity.getType().getFullyQualifiedTypeName());
        final List<AnnotationAttributeValue<?>> gwtRequestAttributeValues = 
                new ArrayList<AnnotationAttributeValue<?>>();
        gwtRequestAttributeValues.add(entityAttributeValue);
        return new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_UNMANAGED_REQUEST,
                gwtRequestAttributeValues).build();
    }

    @Override
    public void copyServerDirectoryContents(String module) {
        for (final RequestFactoryPath path : RequestFactoryPath.SERVER_PATHS) {
            copyDirectoryContents(path, module, getClass());
        }
    }

    @Override
    public void copySharedDirectoryContents(String module) {
        for (final RequestFactoryPath path : RequestFactoryPath.SHARED_PATHS) {
            copyDirectoryContents(path, module, getClass());
        }
    }

    @Override
    public void copyDirectoryContents(final RequestFactoryPath requestFactoryPath,
            final String moduleName, Class<?> loadingClass) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("account") && typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        final LogicalPath path = LogicalPath.getInstance(SRC_MAIN_JAVA, moduleName);
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final String relativePath = requestFactoryPath
                .getPackagePath(topLevelPackage);
        final String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(path, relativePath);
        updateFile(sourceAntPath, targetDirectory, requestFactoryPath
                .segmentPackage(), false, loadingClass, topLevelPackage
                .getFullyQualifiedPackageName());
    }

    @Override
    public void createScaffold(final ClassOrInterfaceTypeDetails proxy) {
        final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                .getFirstAnnotation(proxy, ROO_REQUEST_FACTORY_PROXY);
        if (annotationMetadata != null) {
            final AnnotationAttributeValue<Boolean> booleanAttributeValue =
                    annotationMetadata.getAttribute("scaffold");
            if (booleanAttributeValue == null
                    || !booleanAttributeValue.getValue()) {
                final ClassOrInterfaceTypeDetailsBuilder cidBuilder = 
                        new ClassOrInterfaceTypeDetailsBuilder(proxy);
                final AnnotationMetadataBuilder annotationMetadataBuilder =
                        new AnnotationMetadataBuilder(annotationMetadata);
                annotationMetadataBuilder.addBooleanAttribute("scaffold", true);
                for (final AnnotationMetadataBuilder existingAnnotation : cidBuilder
                        .getAnnotations()) {
                    if (existingAnnotation.getAnnotationType().equals(
                            annotationMetadata.getAnnotationType())) {
                        cidBuilder.getAnnotations().remove(existingAnnotation);
                        cidBuilder.getAnnotations().add(
                                annotationMetadataBuilder);
                        break;
                    }
                }
                typeManagementService.createOrUpdateTypeOnDisk(cidBuilder
                        .build());
            }
        }
    }

    @Override
    public void updateFile(final String sourceAntPath, String targetDirectory,
            final String segmentPackage, final boolean overwrite,
            Class<?> loadingClass, final String topLevelPackage) {
        if (!targetDirectory.endsWith(File.separator)) {
            targetDirectory += File.separator;
        }
        if (!fileManager.exists(targetDirectory)) {
            fileManager.createDirectory(targetDirectory);
        }

        final String path = FileUtils.getPath(loadingClass, sourceAntPath);
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
                    input = processTemplate(input, segmentPackage,
                            topLevelPackage);

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

    @Override
    public String processTemplate(String input, String segmentPackage,
            final String topLevelPackage) {
        if (segmentPackage == null) {
            segmentPackage = "";
        }
//        final String topLevelPackage = projectOperations.getTopLevelPackage(
//                projectOperations.getFocusedModuleName())
//                .getFullyQualifiedPackageName();
        input = input.replace("__TOP_LEVEL_PACKAGE__", topLevelPackage);
        input = input.replace("__SHARED_TOP_LEVEL_PACKAGE__", getSharedTopLevelPackageName());
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

    @Override
    public JavaPackage getServerTopLevelPackage() {
        final ClassOrInterfaceTypeDetails entity = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_JPA_ENTITY,
                        ROO_JPA_ACTIVE_RECORD, ROO_MONGO_ENTITY)
                .iterator().next();
        final JavaPackage entityTopLevelPackage = projectOperations
                .getTopLevelPackage(PhysicalTypeIdentifier.getPath(
                entity.getDeclaredByMetadataId()).getModule());
        return entityTopLevelPackage;
    }

    @Override
    public CharSequence getSharedTopLevelPackageName() {
        final ClassOrInterfaceTypeDetails proxy = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)
                .iterator().next();
        final JavaPackage proxyTopLevelPackage = projectOperations
                .getTopLevelPackage(PhysicalTypeIdentifier.getPath(
                proxy.getDeclaredByMetadataId()).getModule());
        return proxyTopLevelPackage.getFullyQualifiedPackageName();
    }

    @Override
    public CharSequence getImportAccountHookup() {
        final JavaType account = typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT)
                .iterator().next();
        return "import " + account.getFullyQualifiedTypeName() + ";";
    }

    @Override
    public CharSequence getImportRoleHookup() {
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

    @Override
    public CharSequence getAccountHookup() {
        final StringBuilder builder = new StringBuilder();
        builder.append("new AccountNavTextDriver(requestFactory).setWidget(shell.getNicknameWidget());\n");
        builder.append("\t\tnew LoginOnAuthenticationFailure().register(eventBus);");
        return builder.toString();
    }
}
