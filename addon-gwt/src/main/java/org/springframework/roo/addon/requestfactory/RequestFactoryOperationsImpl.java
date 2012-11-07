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
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.request.RequestFactoryRequestMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
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
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.util.CollectionUtils;
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
public class RequestFactoryOperationsImpl extends BaseOperationsImpl
        implements RequestFactoryOperations {

    private static final String FEATURE_NAME = "requestfactory";
    private static final String REQUEST_FACTORY_GROUP_ID = "com.google.web.bindery";

    private static final JavaSymbolName VALUE = new JavaSymbolName("value");
    private static final JavaSymbolName SERVER_MODULE = new JavaSymbolName(
            RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE);

    @Reference private RequestFactoryTypeService requestFactoryTypeService;
    @Reference private PersistenceMemberLocator persistenceMemberLocator;

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
    public void setupRequestFactoryServer() {
        setupRequestFactory(true);
    }

    @Override
    public void setupRequestFactoryClient() {
        setupRequestFactory(false);
    }

    private void setupRequestFactory(boolean server) {
        final Element configuration = XmlUtils.getConfiguration(getClass());
        final String focusedModuleName = projectOperations
                .getFocusedModuleName();

        // update properties
        final List<Element> properties = XmlUtils.findElements(
                "/configuration/batch/properties/*", configuration);
        for (Element propertyElement : properties) {
            projectOperations.addProperty(focusedModuleName,
                    new Property(propertyElement));
        }

        // update dependencies
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        final List<Element> serverDependencies = XmlUtils.findElements(
                "/configuration/" + (server ? "server" : "client")
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
                        ROO_JPA_ACTIVE_RECORD)) {
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
                        ROO_JPA_ACTIVE_RECORD)) {
            createRequestInterfaceIfNecessary(entity, proxyPackage);
        }
    }

    @Override
    public void requestType(final JavaPackage requestPackage,
            final JavaType type) {
        createRequestInterfaceIfNecessary(
                typeLocationService.getTypeDetails(type), requestPackage);
    }

    public void scaffoldAll() {
        final Set<ClassOrInterfaceTypeDetails> proxys = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);

        final String serverModule = RequestFactoryUtils.getStringAnnotationValue(proxys
                .iterator().next(),
                ROO_REQUEST_FACTORY_PROXY, RooRequestFactoryProxy.SERVER_MODULE_ATTRIBUTE,
                "");
        final String focusedModule = projectOperations.getFocusedModuleName();

        copyServerDirectoryContents(serverModule.isEmpty() ? focusedModule : serverModule);
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
                        "In order to scaffold, an entity must have an associated proxy and request");
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
        return isRequestFactoryInstalled(moduleName, false);
    }

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

    private void createProxy(final ClassOrInterfaceTypeDetails entity,
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
                .getTopLevelPackage(focusedModule)
                + ".server.locator."
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
    private void createRequestInterface(
            final ClassOrInterfaceTypeDetails entity,
            final JavaPackage destinationPackage) {
        final JavaType requestType = new JavaType(
                destinationPackage.getFullyQualifiedPackageName() + "."
                        + entity.getType().getSimpleTypeName()
                        + "Request_Roo_Gwt");
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
    private void createUnmanagedRequestInterface(
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

    private void createRequestInterfaceIfNecessary(
            final ClassOrInterfaceTypeDetails entity,
            final JavaPackage destinationPackage) {
        if (entity != null && !entity.isAbstract()
                && requestFactoryTypeService.lookupRequestFromEntity(entity) == null) {
            createRequestInterface(entity, destinationPackage);

            createUnmanagedRequestInterface(entity, destinationPackage);
        }
    }

    private AnnotationMetadata getRooGwtRequestAnnotation(
            final ClassOrInterfaceTypeDetails entity) {
        // The GwtRequestMetadataProvider doesn't need to know excluded methods
        // any more because it actively adds the required CRUD methods itself.
        final StringAttributeValue entityAttributeValue = new StringAttributeValue(
                VALUE, entity.getType().getFullyQualifiedTypeName());
        final List<AnnotationAttributeValue<?>> gwtRequestAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
        gwtRequestAttributeValues.add(entityAttributeValue);
        return new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_REQUEST,
                gwtRequestAttributeValues).build();
    }

    private AnnotationMetadata getRooGwtUnmanagedRequestAnnotation(
            final ClassOrInterfaceTypeDetails entity) {
        final StringAttributeValue entityAttributeValue = new StringAttributeValue(
                VALUE, entity.getType().getFullyQualifiedTypeName());
        final List<AnnotationAttributeValue<?>> gwtRequestAttributeValues = new ArrayList<AnnotationAttributeValue<?>>();
        gwtRequestAttributeValues.add(entityAttributeValue);
        return new AnnotationMetadataBuilder(ROO_REQUEST_FACTORY_UNMANAGED_REQUEST,
                gwtRequestAttributeValues).build();
    }

    private void copyServerDirectoryContents(String module) {
        for (final RequestFactoryPath path : RequestFactoryPath.SERVER_PATHS) {
            copyDirectoryContents(path, module);
        }
    }

    private void copySharedDirectoryContents(String module) {
        for (final RequestFactoryPath path : RequestFactoryPath.SHARED_PATHS) {
            copyDirectoryContents(path, module);
        }
    }

    private void copyDirectoryContents(final RequestFactoryPath requestFactoryPath,
            String module) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("account")
                && typeLocationService.findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        LogicalPath path = LogicalPath.getInstance(SRC_MAIN_JAVA, module);
        String relativePath = requestFactoryPath.getPackagePath(projectOperations
                .getFocusedTopLevelPackage());
        final String targetDirectory = projectOperations.getPathResolver()
                .getIdentifier(path, relativePath);
        updateFile(sourceAntPath, targetDirectory, requestFactoryPath.segmentPackage(),
                false);
    }

    private void createScaffold(final ClassOrInterfaceTypeDetails proxy) {
        final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                .getFirstAnnotation(proxy, ROO_REQUEST_FACTORY_PROXY);
        if (annotationMetadata != null) {
            final AnnotationAttributeValue<Boolean> booleanAttributeValue = annotationMetadata
                    .getAttribute("scaffold");
            if (booleanAttributeValue == null
                    || !booleanAttributeValue.getValue()) {
                final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                        proxy);
                final AnnotationMetadataBuilder annotationMetadataBuilder = new AnnotationMetadataBuilder(
                        annotationMetadata);
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
}
