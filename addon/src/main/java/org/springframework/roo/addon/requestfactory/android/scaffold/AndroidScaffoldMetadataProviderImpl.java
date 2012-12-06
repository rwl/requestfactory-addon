package org.springframework.roo.addon.requestfactory.android.scaffold;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ANDROID_SCAFFOLD;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import org.springframework.roo.addon.requestfactory.android.AndroidPaths;
import org.springframework.roo.addon.requestfactory.android.AndroidTemplateService;
import org.springframework.roo.addon.requestfactory.android.AndroidType;
import org.springframework.roo.addon.requestfactory.android.AndroidUtils;
import org.springframework.roo.addon.requestfactory.annotations.android.RooAndroidScaffold;
import org.springframework.roo.addon.requestfactory.scaffold.RequestFactoryScaffoldMetadataProviderImpl;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;

/**
 * Monitors Java types and if necessary creates/updates/deletes the Android
 * files maintained for each mirror-compatible object.
 */
@Component(immediate = true)
@Service
public class AndroidScaffoldMetadataProviderImpl extends RequestFactoryScaffoldMetadataProviderImpl
        implements AndroidScaffoldMetadataProvider {

    @Reference AndroidTemplateService androidTemplateService;

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
        return proxy.getAnnotation(ROO_ANDROID_SCAFFOLD) != null;
    }

    @Override
    protected String createLocalIdentifier(final JavaType javaType,
            final LogicalPath path) {
        return AndroidScaffoldMetadata.createIdentifier(javaType, path);
    }

    @Override
    protected void buildType(final RequestFactoryType type, final String moduleName) {
        requestFactoryTypeService.buildType(type, androidTemplateService
                .getStaticTemplateTypeDetails(type, moduleName), moduleName);
    }

    @Override
    protected void buildTypes(final String moduleName) {
    }

    @Override
    protected String getModuleName(ClassOrInterfaceTypeDetails proxy) {
        String moduleName = RequestFactoryUtils.getStringAnnotationValue(proxy,
                ROO_ANDROID_SCAFFOLD,
                RooAndroidScaffold.MODULE_ATTRIBUTE, "");
        if (moduleName.isEmpty()) {
            return super.getModuleName(proxy);
        } else {
            return moduleName;
        }
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
            if (!(requestFactoryType instanceof AndroidType)) {
                continue;
            }
            final AndroidType androidType = (AndroidType) entry.getKey();
            final JavaType javaType = entry.getValue();
            if (!androidType.isMirrorType()) {
                continue;
            }
            androidType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            androidType.dynamicallyResolveMethodsToWatch(proxy.getName(),
                    clientSideTypeMap, topLevelPackage);

            if (androidType.isCreateViewXml()) {
                final RequestFactoryPath path = androidType.getPath();
                final PathResolver pathResolver = projectOperations
                        .getPathResolver();
                final String layoutPath = pathResolver.getIdentifier(
                        LogicalPath.getInstance(Path.ROOT,
                                moduleName), AndroidPaths.LAYOUT_PATH);
                final String packagePath = pathResolver
                        .getIdentifier(LogicalPath.getInstance(
                                Path.SRC_MAIN_JAVA, moduleName), path
                                .getPackagePath(topLevelPackage));

                final String targetDirectory = path == AndroidPaths.ACTIVITY ?
                        layoutPath : packagePath;
                final String destFile = targetDirectory + File.separatorChar
                        + javaType.getSimpleTypeName()
                        .replaceAll("(\\p{Ll})(\\p{Lu})", "$1_$2").toLowerCase()
                        + "_view.xml";
                final String contents = androidTemplateService.buildViewXml(
                        templateDataHolder.getXmlTemplates().get(androidType),
                        destFile, new ArrayList<MethodMetadata>(proxy
                                .getDeclaredMethods()));
                xmlToBeWritten.put(destFile, contents);
            }
        }
        return xmlToBeWritten;
    }

    @Override
    protected MetadataItem createMetadataItem(final String metadataIdentificationString) {
        return new AndroidScaffoldMetadata(metadataIdentificationString);
    }

    @Override
    protected RequestFactoryTemplateDataHolder buildTemplateDataHolder(
            final ClassOrInterfaceTypeDetails mirroredType,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        return androidTemplateService
                .getMirrorTemplateTypeDetails(mirroredType, clientSideTypeMap,
                        moduleName);
    }

    @Override
    protected Map<RequestFactoryType, JavaType> getMirrorTypeMap(
            final JavaType governorType, final JavaPackage topLevelPackage) {
        return AndroidUtils.getMirrorTypeMap(governorType,
                topLevelPackage);
    }

    @Override
    protected ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = AndroidScaffoldMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = AndroidScaffoldMetadata
                .getPath(metadataIdentificationString);

        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    @Override
    public String getProvidesType() {
        return AndroidScaffoldMetadata.getMetadataIdentifierType();
    }
}
