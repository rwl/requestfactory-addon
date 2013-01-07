package org.springframework.roo.addon.requestfactory.rest;

import static org.springframework.roo.addon.requestfactory.rest.RestJavaType.REST_RESOURCE;
import static org.springframework.roo.project.Path.SRC_MAIN_WEBAPP;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Service
public class RestProjectOperationsImpl implements RestProjectOperations {
    
    private static final String WEBMVC_CONFIG_XML = "WEB-INF/spring/webmvc-config.xml";
    private static final String REST_MVC_CONFIG = "org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration";

    @Reference FileManager fileManager;
    @Reference PathResolver pathResolver;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;
    @Reference TypeManagementService typeManagementService;
    @Reference MetadataService metadataService;
    @Reference WebMvcOperations webMvcOperations;

    @Override
    public String getName() {
        return RestProjectOperations.FEATURE_NAME;
    }

    @Override
    public boolean isInstalledInModule(String moduleName) {
        final String webConfigFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, WEBMVC_CONFIG_XML);
        if (!fileManager.exists(webConfigFile)) {
            return false;
        }
        InputStream webMvcConfigInputStream = null;
        try {
            webMvcConfigInputStream = fileManager.getInputStream(webConfigFile);
            if (webMvcConfigInputStream == null) {
                return false;
            }
            final Document webMvcConfig = XmlUtils.readXml(
                    webMvcConfigInputStream);
            final Element root = webMvcConfig.getDocumentElement();
            return XmlUtils.findFirstElement("/beans/bean[@class='"
                    + REST_MVC_CONFIG + "']", root) != null;
        } finally {
            IOUtils.closeQuietly(webMvcConfigInputStream);
        }
    }

    @Override
    public boolean isSetupAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @Override
    public boolean isRestResourceAvailable() {
        return projectOperations.isFeatureInstalledInFocusedModule(
                RestProjectOperations.FEATURE_NAME);
    }
    
    @Override
    public void setup() {
        if (!fileManager.exists(projectOperations.getPathResolver()
                .getFocusedIdentifier(SRC_MAIN_WEBAPP, "WEB-INF/web.xml"))) {
            webMvcOperations.installAllWebMvcArtifacts();
        }
        
        final Element configuration = XmlUtils.getConfiguration(getClass());
        
        for (Element propertyElement : XmlUtils.findElements(
                "/configuration/batch/properties/*", configuration)) {
            projectOperations.addProperty(
                    projectOperations.getFocusedModuleName(),
                    new Property(propertyElement));
        }
        
        final List<Dependency> dependencies = new ArrayList<Dependency>();
        for (final Element dependencyElement : XmlUtils.findElements(
                "/configuration/batch/dependencies/*", configuration)) {
            dependencies.add(new Dependency(dependencyElement));
        }
        projectOperations.removeDependencies(projectOperations
                .getFocusedModuleName(), dependencies);
        metadataService.evict(ProjectMetadata.getProjectIdentifier(
                projectOperations.getFocusedModuleName()));
        projectOperations.addDependencies(projectOperations
                .getFocusedModuleName(), dependencies);

        
        final String webConfigFile = pathResolver.getFocusedIdentifier(
                Path.SRC_MAIN_WEBAPP, WEBMVC_CONFIG_XML);
        Validate.isTrue(fileManager.exists(webConfigFile),
                "Aborting: Unable to find " + webConfigFile);
        InputStream webMvcConfigInputStream = null;
        try {
            webMvcConfigInputStream = fileManager.getInputStream(webConfigFile);
            Validate.notNull(webMvcConfigInputStream,
                    "Aborting: Unable to acquire webmvc-config.xml file");
            final Document webMvcConfig = XmlUtils
                    .readXml(webMvcConfigInputStream);
            final Element root = webMvcConfig.getDocumentElement();
            if (XmlUtils.findFirstElement("/beans/bean[@class='"
                    + REST_MVC_CONFIG + "']", root) == null) {
                final Element config = webMvcConfig.createElement("bean");
                config.setAttribute("class", REST_MVC_CONFIG);
                root.appendChild(config);
                
                fileManager.createOrUpdateTextFileIfRequired(webConfigFile,
                        XmlUtils.nodeToString(webMvcConfig), true);
            }
        } finally {
            IOUtils.closeQuietly(webMvcConfigInputStream);
        }
    }

    @Override
    public void restResource(final JavaType type, final boolean hide,
            final String path, final String rel) {

        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(type);
        Validate.notNull(typeDetails, "The repository specified, '" + type
                + "'doesn't exist");

        
        final AnnotationMetadataBuilder annotationBuilder = new AnnotationMetadataBuilder(REST_RESOURCE);
        if (hide) {
            annotationBuilder.addBooleanAttribute("exported", false);
        }
        if (!StringUtils.isEmpty(path)) {
            annotationBuilder.addStringAttribute("path", path);
        }
        if (!StringUtils.isEmpty(rel)) {
            annotationBuilder.addStringAttribute("rel", rel);
        }

        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(typeDetails);
        
        if (MemberFindingUtils.getAnnotationOfType(typeDetails
                .getAnnotations(), REST_RESOURCE) != null) {
            cidBuilder.removeAnnotation(REST_RESOURCE);
        }

        cidBuilder.addAnnotation(annotationBuilder);

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
    }
}
