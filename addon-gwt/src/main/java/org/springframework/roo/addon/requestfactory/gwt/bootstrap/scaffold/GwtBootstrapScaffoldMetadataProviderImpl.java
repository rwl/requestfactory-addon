package org.springframework.roo.addon.requestfactory.gwt.bootstrap.scaffold;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapPaths;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapProxyProperty;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapTemplateService;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapType;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapTypeService;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapUtils;
import org.springframework.roo.addon.requestfactory.scaffold.RequestFactoryScaffoldMetadataProviderImpl;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;


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
public class GwtBootstrapScaffoldMetadataProviderImpl extends RequestFactoryScaffoldMetadataProviderImpl
        implements GwtBootstrapScaffoldMetadataProvider {

    @Reference protected GwtBootstrapTemplateService gwtBootstrapTemplateService;
    @Reference protected GwtBootstrapTypeService gwtBootstrapTypeService;

    @Override
    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    @Override
    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    @Override
    protected void buildType(final RequestFactoryType type, final String moduleName) {
        gwtBootstrapTypeService.buildType(type, gwtBootstrapTemplateService
                .getStaticTemplateTypeDetails(type, moduleName), moduleName);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return GwtBootstrapScaffoldMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected void buildTypes(final String moduleName) {
        buildType(GwtBootstrapType.LIST_PLACE_RENDERER, moduleName);
        buildType(GwtBootstrapType.MASTER_ACTIVITIES, moduleName);
        buildType(GwtBootstrapType.LIST_PLACE_RENDERER, moduleName);
        buildType(GwtBootstrapType.DETAILS_ACTIVITIES, moduleName);
        buildType(GwtBootstrapType.MOBILE_ACTIVITIES, moduleName);
        buildType(GwtBootstrapType.IS_LEAF_PROCESSOR, moduleName);
        buildType(GwtBootstrapType.PROXY_LIST_NODE_PROCESSOR, moduleName);
        buildType(GwtBootstrapType.PROXY_NODE_PROCESSOR, moduleName);
    }

    protected MetadataItem createMetadataItem(final String metadataIdentificationString) {
        return new GwtBootstrapScaffoldMetadata(
                metadataIdentificationString);
    }

    protected RequestFactoryProxyProperty createRequestFactoryProxyProperty(
            final JavaPackage topLevelPackage, final ClassOrInterfaceTypeDetails ptmd,
            final JavaType propertyType, final JavaSymbolName propertyName,
            final List<AnnotationMetadata> annotations,
            final MethodMetadata proxyMethod) {

        return new GwtBootstrapProxyProperty(
                topLevelPackage, ptmd, propertyType,
                propertyName.getSymbolName(), annotations, proxyMethod
                        .getMethodName().getSymbolName());
    }

    protected Map<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> buildTypesToBeWritten(
            final ClassOrInterfaceTypeDetails mirroredType, final JavaPackage topLevelPackage,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final ClassOrInterfaceTypeDetails proxy, final String moduleName,
            final RequestFactoryTemplateDataHolder templateDataHolder) {

        final Map<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> typesToBeWritten =
                new LinkedHashMap<RequestFactoryType, List<ClassOrInterfaceTypeDetails>>();

        final Map<GwtBootstrapType, JavaType> mirrorTypeMap = GwtBootstrapUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);

        for (final Map.Entry<GwtBootstrapType, JavaType> entry : mirrorTypeMap
                .entrySet()) {
            final GwtBootstrapType gwtBootstrapType = entry.getKey();
            if (!gwtBootstrapType.isMirrorType() || gwtBootstrapType.equals(RequestFactoryType.PROXY)
                    || gwtBootstrapType.equals(RequestFactoryType.REQUEST)) {
                continue;
            }
            gwtBootstrapType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            gwtBootstrapType.dynamicallyResolveMethodsToWatch(proxy.getName(),
                    clientSideTypeMap, topLevelPackage);

            final List<MemberHoldingTypeDetails> extendsTypes = gwtBootstrapTypeService
                    .getExtendsTypes(templateDataHolder
                            .getTemplateTypeDetailsMap().get(gwtBootstrapType));
            typesToBeWritten.put(gwtBootstrapType, gwtBootstrapTypeService
                    .buildType(gwtBootstrapType, templateDataHolder
                            .getTemplateTypeDetailsMap().get(gwtBootstrapType),
                            extendsTypes, moduleName));

        }
        return typesToBeWritten;
    }

    protected Map<String, String> buildXmlToBeWritten(
            final ClassOrInterfaceTypeDetails mirroredType, final JavaPackage topLevelPackage,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final ClassOrInterfaceTypeDetails proxy, final String moduleName,
            final RequestFactoryTemplateDataHolder templateDataHolder) {

        final Map<String, String> xmlToBeWritten = new LinkedHashMap<String, String>();

        final Map<GwtBootstrapType, JavaType> mirrorTypeMap = GwtBootstrapUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);

        for (final Map.Entry<GwtBootstrapType, JavaType> entry : mirrorTypeMap
                .entrySet()) {
            final GwtBootstrapType gwtBootstrapType = entry.getKey();
            final JavaType javaType = entry.getValue();
            if (!gwtBootstrapType.isMirrorType() || gwtBootstrapType.equals(RequestFactoryType.PROXY)
                    || gwtBootstrapType.equals(RequestFactoryType.REQUEST)) {
                continue;
            }
//            gwtBootstrapType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
//            gwtBootstrapType.dynamicallyResolveMethodsToWatch(proxy.getName(),
//                    clientSideTypeMap, topLevelPackage);

            if (gwtBootstrapType.isCreateUiXml()) {
                final RequestFactoryPath requestFactoryPath = gwtBootstrapType.getPath();
                final PathResolver pathResolver = projectOperations
                        .getPathResolver();
                final String webappPath = pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                                moduleName), moduleName);
                final String packagePath = pathResolver
                        .getIdentifier(LogicalPath.getInstance(
                                Path.SRC_MAIN_JAVA, moduleName), requestFactoryPath
                                .getPackagePath(topLevelPackage));

                final String targetDirectory = requestFactoryPath == GwtBootstrapPaths.WEB ? webappPath
                        : packagePath;
                final String destFile = targetDirectory + File.separatorChar
                        + javaType.getSimpleTypeName() + ".ui.xml";
                final String contents = gwtBootstrapTemplateService.buildUiXml(
                        templateDataHolder.getXmlTemplates().get(gwtBootstrapType),
                        destFile,
                        new ArrayList<MethodMetadata>(proxy
                                .getDeclaredMethods()));
                xmlToBeWritten.put(destFile, contents);
            }
        }
        return xmlToBeWritten;
    }

    @Override
    protected ClassOrInterfaceTypeDetails getGovernor(
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
}
