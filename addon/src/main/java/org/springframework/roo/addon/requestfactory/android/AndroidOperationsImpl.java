package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.account.AccountJavaType.ROO_ACCOUNT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ANDROID_SCAFFOLD;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.RequestFactoryOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.annotations.android.RooRequestFactoryAndroid;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.StringAttributeValue;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

/**
 * Implementation of {@link AndroidOperations}.
 */
@Component
@Service
public class AndroidOperationsImpl implements AndroidOperations {

    private static final JavaSymbolName MODULE_SYMBOL_NAME = new JavaSymbolName(
            RooRequestFactoryAndroid.MODULE_ATTRIBUTE);

    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference AndroidTypeService androidTypeService;
    @Reference TypeManagementService typeManagementService;
    @Reference TypeLocationService typeLocationService;
    @Reference ProjectOperations projectOperations;
    @Reference MetadataService metadataService;
    @Reference RequestFactoryOperations requestFactoryOperations;

    @Override
    public void setupAndroid() {
        final String moduleName = projectOperations.getFocusedModuleName();
        projectOperations.addProperty(moduleName,
                new Property("shared.package", requestFactoryOperations
                        .getSharedTopLevelPackageName().toString()));

        final Element configuration = XmlUtils.getConfiguration(getClass());

        final List<AndroidDependency> dependencies = new ArrayList<AndroidDependency>();
        for (final Element dependencyElement : XmlUtils.findElements(
                "/configuration/batch/dependencies/dependency", configuration)) {
            dependencies.add(new AndroidDependency(dependencyElement));
        }
        projectOperations.removeDependencies(moduleName, dependencies);
        metadataService.evict(ProjectMetadata.getProjectIdentifier(
                moduleName));
        androidTypeService.addDependencies(moduleName, dependencies);


        final List<Plugin> plugins = new ArrayList<Plugin>();
        final String xPathExpression = "/configuration/batch/plugins/plugin";
        final List<Element> pluginElements = XmlUtils.findElements(
                xPathExpression, configuration);
        for (final Element pluginElement : pluginElements) {
            plugins.add(new Plugin(pluginElement));
        }
        projectOperations.addBuildPlugins(moduleName, plugins);
    }

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
            final StringAttributeValue moduleAttributeValue =
                    new StringAttributeValue(MODULE_SYMBOL_NAME,
                            module.getModuleName());
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

    private void updateAndroidManifest(final String moduleName) {
        final String topLevelPackageName = projectOperations
                .getTopLevelPackage(moduleName).getFullyQualifiedPackageName();
        androidTypeService.addActvity(moduleName, topLevelPackageName
                + ".activity.MainActivity", true);
        androidTypeService.setApplicationName(moduleName, topLevelPackageName
                + ".application.AndroidApplication");
        androidTypeService.addPermission(moduleName,
                "android.permission.INTERNET");
    }

    private void copyDirectoryContents(
            final RequestFactoryPath requestFactoryPath,
            final String moduleName) {
        final String sourceAntPath = requestFactoryPath.getSourceAntPath();
        if (sourceAntPath.contains("account") && typeLocationService
                .findTypesWithAnnotation(ROO_ACCOUNT).size() == 0) {
            return;
        }
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final String targetDirectory;
        final LogicalPath path;
        if (requestFactoryPath == AndroidPaths.ASSET) {
            path = LogicalPath.getInstance(Path.ROOT, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, AndroidPaths.ASSET_PATH);
        } else if (requestFactoryPath == AndroidPaths.LAYOUT) {
            path = LogicalPath.getInstance(Path.ROOT, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, AndroidPaths.LAYOUT_PATH);
        } else if (requestFactoryPath == AndroidPaths.DRAWABLE) {
            path = LogicalPath.getInstance(Path.ROOT, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, AndroidPaths.DRAWABLE_PATH);
        } else {
            path = LogicalPath.getInstance(Path.ROOT, moduleName);
            targetDirectory = projectOperations.getPathResolver()
                    .getIdentifier(path, AndroidPaths.SRC_PATH
                            + AndroidPaths.SEP + requestFactoryPath
                            .getPackagePath(topLevelPackage));
        }
        requestFactoryOperations.updateFile(sourceAntPath, targetDirectory,
                requestFactoryPath.segmentPackage(), false, getClass(),
                topLevelPackage.getFullyQualifiedPackageName());
    }
}
