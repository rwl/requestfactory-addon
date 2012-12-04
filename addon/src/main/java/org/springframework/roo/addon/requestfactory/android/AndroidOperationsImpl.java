package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ANDROID_SCAFFOLD;
import static org.springframework.roo.project.Path.SRC_MAIN_JAVA;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.BaseOperationsImpl;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.android.RooAndroidScaffold;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.ProjectOperations;
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

    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference AndroidTypeService androidTypeService;

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
        updateBoilerplate(module);
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
                        "Entity must have associated proxy and request");
            }
            updateBoilerplate(module);
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
    
    private void updateBoilerplate(final Pom module) {
        Validate.notNull(module);
        final String moduleName = module.getModuleName();
        copyDirectoryContents(moduleName);
        updateAndroidManifest(moduleName);
    }

    private void copyDirectoryContents(final String moduleName) {
        for (final RequestFactoryPath path : AndroidPaths.ALL_PATHS) {
            copyDirectoryContents(path, moduleName);
        }
    }

    private void copyDirectoryContents(final RequestFactoryPath requestFactoryPath,
            final String moduleName) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("account") && typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        final LogicalPath path = LogicalPath.getInstance(SRC_MAIN_JAVA, moduleName);
        final String targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, requestFactoryPath
                            .getPackagePath(projectOperations
                                    .getTopLevelPackage(moduleName)));
        updateFile(sourceAntPath, targetDirectory, requestFactoryPath.segmentPackage(),
                false);
    }
    
    private void updateAndroidManifest(final String moduleName) {
        androidTypeService.addActvity(moduleName, ".MainActivity", true);
    }
}
