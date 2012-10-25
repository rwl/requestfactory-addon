package org.springframework.roo.addon.requestfactory.android.scaffold;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ANDROID_SCAFFOLD;

import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.android.AndroidUtils;
import org.springframework.roo.addon.requestfactory.android.RooAndroidScaffold;
import org.springframework.roo.addon.requestfactory.scaffold.RequestFactoryScaffoldMetadataProviderImpl;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;

/**
 * Monitors Java types and if necessary creates/updates/deletes the Android
 * files maintained for each mirror-compatible object.
 */
@Component(immediate = true)
@Service
public class AndroidScaffoldMetadataProviderImpl extends RequestFactoryScaffoldMetadataProviderImpl
        implements AndroidScaffoldMetadataProvider {

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
    protected MetadataItem createMetadataItem(final String metadataIdentificationString) {
        return new AndroidScaffoldMetadata(metadataIdentificationString);
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
