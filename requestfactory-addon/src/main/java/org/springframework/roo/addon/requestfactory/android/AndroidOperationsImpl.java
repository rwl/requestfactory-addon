package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ANDROID_SCAFFOLD;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.BaseOperationsImpl;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.maven.Pom;

/**
 * Implementation of {@link AndroidOperations}.
 */
@Component
@Service
public class AndroidOperationsImpl extends BaseOperationsImpl
    implements AndroidOperations {

    private static final JavaSymbolName MODULE_SYMBOL_NAME = new JavaSymbolName(
            RooAndroidScaffold.MODULE_ATTRIBUTE);

    @Reference private RequestFactoryTypeService requestFactoryTypeService;

    @Override
    public boolean isScaffoldAvailable() {
        return typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(
                        ROO_REQUEST_FACTORY_PROXY).size() > 0;
    }

    @Override
    public void scaffoldAll(Pom module) {
        if (module == null) {
            module = projectOperations.getFocusedModule();
        }
        for (final ClassOrInterfaceTypeDetails proxy : typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)) {
            final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                    .lookupRequestFromProxy(proxy);
            if (request == null) {
                throw new IllegalStateException(
                        "In order to scaffold, an entity must have a request");
            }
            createScaffold(proxy, module);
        }

    }

    @Override
    public void scaffoldType(JavaType type, Pom module) {
        if (module == null) {
            module = projectOperations.getFocusedModule();
        }
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
            createScaffold(proxy, module);
        }
    }

    private void createScaffold(final ClassOrInterfaceTypeDetails proxy,
            final Pom module) {
        Validate.notNull(module, "Android scaffold module required");
        final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                .getFirstAnnotation(proxy, ROO_ANDROID_SCAFFOLD);
        if (annotationMetadata == null) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                    new ClassOrInterfaceTypeDetailsBuilder(proxy);
            final AnnotationMetadataBuilder annotationMetadataBuilder =
                    new AnnotationMetadataBuilder(ROO_ANDROID_SCAFFOLD);
            final StringAttributeValue moduleAttributeValue = new StringAttributeValue(
                    MODULE_SYMBOL_NAME, module.getModuleName());
            annotationMetadataBuilder.addAttribute(moduleAttributeValue);
            cidBuilder.getAnnotations().add(annotationMetadataBuilder);
            typeManagementService.createOrUpdateTypeOnDisk(cidBuilder
                    .build());
        }
    }
}
