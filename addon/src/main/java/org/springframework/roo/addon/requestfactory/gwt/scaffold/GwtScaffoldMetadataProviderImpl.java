package org.springframework.roo.addon.requestfactory.gwt.scaffold;

import static org.springframework.roo.addon.requestfactory.gwt.GwtJavaType.ROO_REQUEST_FACTORY_GWT;

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
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.gwt.RooRequestFactoryGwt;
import org.springframework.roo.addon.requestfactory.gwt.GwtPaths;
import org.springframework.roo.addon.requestfactory.gwt.GwtProxyProperty;
import org.springframework.roo.addon.requestfactory.gwt.GwtTemplateService;
import org.springframework.roo.addon.requestfactory.gwt.GwtType;
import org.springframework.roo.addon.requestfactory.gwt.GwtTypeService;
import org.springframework.roo.addon.requestfactory.gwt.GwtUtils;
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
 * contents of the above files via {@link GwtScaffoldMetadata}. It also attempts
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
public class GwtScaffoldMetadataProviderImpl extends RequestFactoryScaffoldMetadataProviderImpl
        implements GwtScaffoldMetadataProvider {

    @Reference protected GwtTemplateService gwtTemplateService;
    @Reference protected GwtTypeService gwtTypeService;

    protected void activate(final ComponentContext context) {
        metadataDependencyRegistry.registerDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    protected void deactivate(final ComponentContext context) {
        metadataDependencyRegistry.deregisterDependency(
                PhysicalTypeIdentifier.getMetadataIdentiferType(),
                getProvidesType());
    }

    @Override
    protected boolean isScaffoldEnabled(final ClassOrInterfaceTypeDetails proxy) {
        return proxy.getAnnotation(ROO_REQUEST_FACTORY_GWT) != null;
    }

    @Override
    protected void buildType(final RequestFactoryType type, final String moduleName) {
        gwtTypeService.buildType(type, gwtTemplateService
                .getStaticTemplateTypeDetails(type, moduleName), moduleName);
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return GwtScaffoldMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected void buildTypes(final String moduleName) {
        buildType(GwtType.LIST_PLACE_RENDERER, moduleName);
        buildType(GwtType.PROXY_PLACE_RENDERER, moduleName);
        buildType(GwtType.MASTER_ACTIVITIES, moduleName);
        buildType(GwtType.MOBILE_ACTIVITY_MAPPER, moduleName);
        buildType(GwtType.IS_LEAF_PROCESSOR, moduleName);
        buildType(GwtType.PROXY_LIST_NODE_PROCESSOR, moduleName);
        buildType(GwtType.PROXY_NODE_PROCESSOR, moduleName);
    }

    @Override
    protected String getModuleName(ClassOrInterfaceTypeDetails proxy) {
        String moduleName = RequestFactoryUtils.getStringAnnotationValue(proxy,
                ROO_REQUEST_FACTORY_GWT,
                RooRequestFactoryGwt.MODULE_ATTRIBUTE, "");
        if (moduleName.isEmpty()) {
            return super.getModuleName(proxy);
        } else {
            return moduleName;
        }
    }

    @Override
    protected MetadataItem createMetadataItem(final String metadataIdentificationString) {
        return new GwtScaffoldMetadata(metadataIdentificationString);
    }

    @Override
    protected RequestFactoryProxyProperty createRequestFactoryProxyProperty(
            final JavaPackage topLevelPackage, final ClassOrInterfaceTypeDetails ptmd,
            final JavaType propertyType, final JavaSymbolName propertyName,
            final List<AnnotationMetadata> annotations,
            final MethodMetadata proxyMethod) {

        return new GwtProxyProperty(
                topLevelPackage, ptmd, propertyType,
                propertyName.getSymbolName(), annotations, proxyMethod
                        .getMethodName().getSymbolName());
    }

    @Override
    protected RequestFactoryTemplateDataHolder buildTemplateDataHolder(
            final ClassOrInterfaceTypeDetails mirroredType,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {

        return gwtTemplateService
                .getMirrorTemplateTypeDetails(mirroredType, clientSideTypeMap,
                        moduleName);
    }

    @Override
    protected Map<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> getTypesToBeWritten(
            final ClassOrInterfaceTypeDetails mirroredType, final JavaPackage topLevelPackage,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final ClassOrInterfaceTypeDetails proxy, final String moduleName,
            final RequestFactoryTemplateDataHolder templateDataHolder) {

        final Map<RequestFactoryType, List<ClassOrInterfaceTypeDetails>> typesToBeWritten =
                new LinkedHashMap<RequestFactoryType, List<ClassOrInterfaceTypeDetails>>();

        final Map<RequestFactoryType, JavaType> mirrorTypeMap = getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);

        for (final Map.Entry<RequestFactoryType, JavaType> entry : mirrorTypeMap
                .entrySet()) {
            final RequestFactoryType requestFactoryType = entry.getKey();
            if (!(requestFactoryType instanceof GwtType)) {
                continue;
            }
            final GwtType gwtType = (GwtType) entry.getKey();
            if (!gwtType.isMirrorType()
//                    || gwtType.equals(RequestFactoryType.PROXY)
//                    || gwtType.equals(RequestFactoryType.REQUEST)
                    ) {
                continue;
            }
            gwtType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            gwtType.dynamicallyResolveMethodsToWatch(proxy.getName(),
                    clientSideTypeMap, topLevelPackage);

            final List<MemberHoldingTypeDetails> extendsTypes = requestFactoryTypeService
                    .getExtendsTypes(templateDataHolder
                            .getTemplateTypeDetailsMap().get(gwtType));
            typesToBeWritten.put(gwtType, requestFactoryTypeService
                    .buildType(gwtType, templateDataHolder
                            .getTemplateTypeDetailsMap().get(gwtType),
                            extendsTypes, moduleName));

        }
        return typesToBeWritten;
    }

    @Override
    protected Map<String, String> getXmlToBeWritten(
            final ClassOrInterfaceTypeDetails mirroredType, final JavaPackage topLevelPackage,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final ClassOrInterfaceTypeDetails proxy, final String moduleName,
            final RequestFactoryTemplateDataHolder templateDataHolder) {

        final Map<String, String> xmlToBeWritten = new LinkedHashMap<String, String>();

        final Map<RequestFactoryType, JavaType> mirrorTypeMap = getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage);

        for (final Map.Entry<RequestFactoryType, JavaType> entry : mirrorTypeMap
                .entrySet()) {
            final RequestFactoryType requestFactoryType = entry.getKey();
            if (!(requestFactoryType instanceof GwtType)) {
                continue;
            }
            final GwtType gwtType = (GwtType) entry.getKey();
            final JavaType javaType = entry.getValue();
            if (!gwtType.isMirrorType()
//                    || gwtType.equals(RequestFactoryType.PROXY)
//                    || gwtType.equals(RequestFactoryType.REQUEST)
                    ) {
                continue;
            }
//            gwtType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
//            gwtType.dynamicallyResolveMethodsToWatch(proxy.getName(),
//                    clientSideTypeMap, topLevelPackage);

            if (gwtType.isCreateUiXml()) {
                final RequestFactoryPath requestFactoryPath = gwtType.getPath();
                final PathResolver pathResolver = projectOperations
                        .getPathResolver();
                final String webappPath = pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.SRC_MAIN_WEBAPP,
                                moduleName), moduleName);
                final String packagePath = pathResolver
                        .getIdentifier(LogicalPath.getInstance(
                                Path.SRC_MAIN_JAVA, moduleName), requestFactoryPath
                                .getPackagePath(topLevelPackage));

                final String targetDirectory = requestFactoryPath == GwtPaths.WEB ? webappPath
                        : packagePath;
                final String destFile = targetDirectory + File.separatorChar
                        + javaType.getSimpleTypeName() + ".ui.xml";
                final String contents = gwtTemplateService.buildUiXml(
                        templateDataHolder.getXmlTemplates().get(gwtType)[0],
                        destFile,
                        new ArrayList<MethodMetadata>(proxy
                                .getDeclaredMethods()));
                xmlToBeWritten.put(destFile, contents);
            }
        }
        return xmlToBeWritten;
    }

    @Override
    protected Map<RequestFactoryType, JavaType> getMirrorTypeMap(
            final JavaType governorType, final JavaPackage topLevelPackage) {
        return GwtUtils.getMirrorTypeMap(governorType,
                topLevelPackage);
    }

    @Override
    protected ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = GwtScaffoldMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = GwtScaffoldMetadata
                .getPath(metadataIdentificationString);

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    @Override
    public String getProvidesType() {
        return GwtScaffoldMetadata.getMetadataIdentifierType();
    }
}
