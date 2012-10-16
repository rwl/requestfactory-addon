package org.springframework.roo.addon.requestfactory.gwt.bootstrap.scaffold;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.RequestFactoryFileManager;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;


/**
 * Monitors Java types and if necessary creates/updates/deletes the GWT files
 * maintained for each mirror-compatible object. You can find a list of
 * mirror-compatible objects in
 * {@link org.springframework.roo.addon.requestfactory.RequestFactoryType}.
 * <p/>
 * <p/>
 * For now only @RooJpaEntity instances will be mirror-compatible.
 * <p/>
 * <p/>
 * Like all Roo add-ons, this provider aims to expose potentially-useful
 * contents of the above files via {@link GwtBootstrapScaffoldMetadata}. It also attempts
 * to avoiding writing to disk unless actually necessary.
 * <p/>
 * <p/>
 * A separate type monitors the creation/deletion of the aforementioned files to
 * maintain "global indexes".
 *
 * @author Ben Alex
 * @author Alan Stewart
 * @author Ray Cromwell
 * @author Amit Manjhi
 * @since 1.1
 */
@Component(immediate = true)
@Service
public class GwtBootstrapScaffoldMetadataProviderImpl implements
        GwtBootstrapScaffoldMetadataProvider {

    @Reference protected RequestFactoryFileManager requestFactoryFileManager;
    @Reference protected RequestFactoryTemplateService requestFactoryTemplateService;
    @Reference protected RequestFactoryTypeService requestFactoryTypeService;
    @Reference protected MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference protected MetadataService metadataService;
    @Reference protected ProjectOperations projectOperations;
    @Reference protected TypeLocationService typeLocationService;

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    private void buildType(final RequestFactoryType type, final String moduleName) {
        requestFactoryTypeService.buildType(type, requestFactoryTemplateService
                .getStaticTemplateTypeDetails(type, moduleName), moduleName);
    }

    private String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return GwtBootstrapScaffoldMetadata.createIdentifier(javaType, path);
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    public MetadataItem get(final String metadataIdentificationString) {
        // Obtain the governor's information
        final ClassOrInterfaceTypeDetails mirroredType = getGovernor(metadataIdentificationString);
        if (mirroredType == null
                || Modifier.isAbstract(mirroredType.getModifier())) {
            return null;
        }

        final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                .lookupProxyFromEntity(mirroredType);
        if (proxy == null || proxy.getDeclaredMethods().isEmpty()) {
            return null;
        }

        final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                .lookupRequestFromEntity(mirroredType);
        if (request == null) {
            return null;
        }

        if (!RequestFactoryUtils.getBooleanAnnotationValue(proxy,
                RooJavaType.ROO_GWT_PROXY, "scaffold", false)) {
            return null;
        }

        final String moduleName = PhysicalTypeIdentifier.getPath(
                proxy.getDeclaredByMetadataId()).getModule();
        buildType(RequestFactoryType.APP_ENTITY_TYPES_PROCESSOR, moduleName);
        buildType(RequestFactoryType.APP_REQUEST_FACTORY, moduleName);
        buildType(RequestFactoryType.LIST_PLACE_RENDERER, moduleName);
        buildType(RequestFactoryType.MASTER_ACTIVITIES, moduleName);
        buildType(RequestFactoryType.LIST_PLACE_RENDERER, moduleName);
        buildType(RequestFactoryType.DETAILS_ACTIVITIES, moduleName);
        buildType(RequestFactoryType.MOBILE_ACTIVITIES, moduleName);
        buildType(RequestFactoryType.IS_LEAF_PROCESSOR, moduleName);
        buildType(RequestFactoryType.PROXY_LIST_NODE_PROCESSOR, moduleName);
        buildType(RequestFactoryType.PROXY_NODE_PROCESSOR, moduleName);

        final GwtBootstrapScaffoldMetadata gwtBootstrapScaffoldMetadata = new GwtBootstrapScaffoldMetadata(
                metadataIdentificationString);

        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap = new LinkedHashMap<JavaSymbolName, RequestFactoryProxyProperty>();
        for (final MethodMetadata proxyMethod : proxy.getDeclaredMethods()) {
            if (!proxyMethod.getMethodName().getSymbolName().startsWith("get")) {
                continue;
            }
            final JavaSymbolName propertyName = new JavaSymbolName(
                    StringUtils.uncapitalize(BeanInfoUtils
                            .getPropertyNameForJavaBeanMethod(proxyMethod)
                            .getSymbolName()));
            final JavaType propertyType = proxyMethod.getReturnType();
            ClassOrInterfaceTypeDetails ptmd = typeLocationService
                    .getTypeDetails(propertyType);
            if (propertyType.isCommonCollectionType()
                    && !propertyType.getParameters().isEmpty()) {
                ptmd = typeLocationService.getTypeDetails(propertyType
                        .getParameters().get(0));
            }

            final FieldMetadata field = proxy.getDeclaredField(propertyName);
            final List<AnnotationMetadata> annotations = field != null ? field
                    .getAnnotations() : Collections
                    .<AnnotationMetadata> emptyList();

            final RequestFactoryProxyProperty requestFactoryProxyProperty = new RequestFactoryProxyProperty(
                    topLevelPackage, ptmd, propertyType,
                    propertyName.getSymbolName(), annotations, proxyMethod
                            .getMethodName().getSymbolName());
            clientSideTypeMap.put(propertyName, requestFactoryProxyProperty);
        }

        final RequestFactoryTemplateDataHolder templateDataHolder = requestFactoryTemplateService
                .getMirrorTemplateTypeDetails(mirroredType, clientSideTypeMap,
                        moduleName);
        final Map<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> typesToBeWritten = new LinkedHashMap<RequestFactoryType, List<ClassOrInterfaceTypeDetails>>();
        final Map<String, String> xmlToBeWritten = new LinkedHashMap<String, String>();

        final Map<RequestFactoryType, JavaType> mirrorTypeMap = RequestFactoryUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);
        mirrorTypeMap.put(RequestFactoryType.PROXY, proxy.getName());
        mirrorTypeMap.put(RequestFactoryType.REQUEST, request.getName());

        for (final Map.Entry<RequestFactoryType, JavaType> entry : mirrorTypeMap
                .entrySet()) {
            final RequestFactoryType requestFactoryType = entry.getKey();
            final JavaType javaType = entry.getValue();
            if (!requestFactoryType.isMirrorType() || requestFactoryType.equals(RequestFactoryType.PROXY)
                    || requestFactoryType.equals(RequestFactoryType.REQUEST)) {
                continue;
            }
            requestFactoryType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            requestFactoryType.dynamicallyResolveMethodsToWatch(proxy.getName(),
                    clientSideTypeMap, topLevelPackage);

            final List<MemberHoldingTypeDetails> extendsTypes = requestFactoryTypeService
                    .getExtendsTypes(templateDataHolder
                            .getTemplateTypeDetailsMap().get(requestFactoryType));
            typesToBeWritten.put(requestFactoryType, requestFactoryTypeService
                    .buildType(requestFactoryType, templateDataHolder
                            .getTemplateTypeDetailsMap().get(requestFactoryType),
                            extendsTypes, moduleName));

            if (requestFactoryType.isCreateUiXml()) {
                final RequestFactoryPath requestFactoryPath = requestFactoryType.getPath();
                final PathResolver pathResolver = projectOperations
                        .getPathResolver();
                final String webappPath = pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                                moduleName), moduleName);
                final String packagePath = pathResolver
                        .getIdentifier(LogicalPath.getInstance(
                                Path.SRC_MAIN_JAVA, moduleName), requestFactoryPath
                                .getPackagePath(topLevelPackage));

                final String targetDirectory = requestFactoryPath == RequestFactoryPath.WEB ? webappPath
                        : packagePath;
                final String destFile = targetDirectory + File.separatorChar
                        + javaType.getSimpleTypeName() + ".ui.xml";
                final String contents = requestFactoryTemplateService.buildUiXml(
                        templateDataHolder.getXmlTemplates().get(requestFactoryType),
                        destFile,
                        new ArrayList<MethodMetadata>(proxy
                                .getDeclaredMethods()));
                xmlToBeWritten.put(destFile, contents);
            }
        }

        // Our general strategy is to instantiate GwtScaffoldMetadata, which
        // offers a conceptual representation of what should go into the 4
        // key-specific types; after that we do comparisons and write to disk if
        // needed
        for (final Map.Entry<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> entry : typesToBeWritten
                .entrySet()) {
            requestFactoryFileManager.write(typesToBeWritten.get(entry.getKey()), entry
                    .getKey().isOverwriteConcrete());
        }
        for (final ClassOrInterfaceTypeDetails type : templateDataHolder
                .getTypeList()) {
            requestFactoryFileManager.write(type, false);
        }
        for (final Map.Entry<String, String> entry : xmlToBeWritten.entrySet()) {
            requestFactoryFileManager.write(entry.getKey(), entry.getValue());
        }
        for (final Map.Entry<String, String> entry : templateDataHolder
                .getXmlMap().entrySet()) {
            requestFactoryFileManager.write(entry.getKey(), entry.getValue());
        }

        return gwtBootstrapScaffoldMetadata;
    }

    private ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = GwtBootstrapScaffoldMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = GwtBootstrapScaffoldMetadata
                .getPath(metadataIdentificationString);

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    public String getProvidesType() {
        return GwtBootstrapScaffoldMetadata.getMetadataIdentifierType();
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Validate.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(PhysicalTypeIdentifier
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for PhysicalTypeIdentifier (not '"
                            + upstreamDependency + "')");
            final ClassOrInterfaceTypeDetails cid = typeLocationService
                    .getTypeDetails(upstreamDependency);
            if (cid == null) {
                return;
            }

            if (cid.getAnnotation(RooJavaType.ROO_GWT_PROXY) != null) {
                final ClassOrInterfaceTypeDetails entityType = requestFactoryTypeService
                        .lookupEntityFromProxy(cid);
                if (entityType != null) {
                    upstreamDependency = entityType.getDeclaredByMetadataId();
                }
            }
            else if (cid.getAnnotation(RooJavaType.ROO_GWT_REQUEST) != null) {
                final ClassOrInterfaceTypeDetails entityType = requestFactoryTypeService
                        .lookupEntityFromRequest(cid);
                if (entityType != null) {
                    upstreamDependency = entityType.getDeclaredByMetadataId();
                }
            }
            else if (cid.getAnnotation(RooJavaType.ROO_GWT_LOCATOR) != null) {
                final ClassOrInterfaceTypeDetails entityType = requestFactoryTypeService
                        .lookupEntityFromLocator(cid);
                if (entityType != null) {
                    upstreamDependency = entityType.getDeclaredByMetadataId();
                }
            }

            // A physical Java type has changed, and determine what the
            // corresponding local metadata identification string would have
            // been
            final JavaType typeName = PhysicalTypeIdentifier
                    .getJavaType(upstreamDependency);
            final LogicalPath typePath = PhysicalTypeIdentifier
                    .getPath(upstreamDependency);
            downstreamDependency = createLocalIdentifier(typeName, typePath);
        }

        // We only need to proceed if the downstream dependency relationship is
        // not already registered
        // (if it's already registered, the event will be delivered directly
        // later on)
        if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                .contains(downstreamDependency)) {
            return;
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        metadataService.evictAndGet(downstreamDependency);
    }
}
