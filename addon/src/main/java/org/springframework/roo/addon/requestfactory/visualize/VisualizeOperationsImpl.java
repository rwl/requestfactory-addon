package org.springframework.roo.addon.requestfactory.visualize;

import static org.springframework.roo.addon.requestfactory.visualize.VisualizeJavaType.ROO_MAP_MARKER;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.visualize.RooMapMarker;
import org.springframework.roo.addon.requestfactory.gwt.GwtTypeService;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.Property;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

@Component
@Service
public class VisualizeOperationsImpl implements VisualizeOperations {
    
    private static final String MAPS_MODULE = "com.google.gwt.maps.Maps";
    
    private static final Logger LOGGER = HandlerUtils
            .getLogger(VisualizeOperationsImpl.class);

    /**
     * Use ProjectOperations to install new dependencies, plugins,
     * properties, etc into the project configuration
     */
    @Reference private ProjectOperations projectOperations;

    /**
     * Use TypeLocationService to find types which are annotated with
     * a given annotation in the project
     */
    @Reference private TypeLocationService typeLocationService;

    /**
     * Use TypeManagementService to change types
     */
    @Reference private TypeManagementService typeManagementService;

    @Reference protected GwtTypeService gwtTypeService;

    @Override
    public boolean isAnnotateCommandAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @Override
    public boolean isSetupCommandAvailable() {
        if (!projectOperations.isFocusedProjectAvailable()) {
            return false;
        }
        if (typeLocationService.findTypesWithAnnotation(ROO_JPA_ENTITY,
                ROO_JPA_ACTIVE_RECORD, ROO_MONGO_ENTITY).size() == 0) {
            return false;
        }
        return true;
    }

    @Override
    public void annotateNodeType(final JavaType javaType, final String lat,
            final String lon) {
        Validate.notNull(javaType, "Java type required");

        final ClassOrInterfaceTypeDetails existing = typeLocationService
                .getTypeDetails(javaType);

        if (existing != null && MemberFindingUtils.getAnnotationOfType(
                existing.getAnnotations(), ROO_MAP_MARKER) == null) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                    new ClassOrInterfaceTypeDetailsBuilder(existing);

            final AnnotationMetadataBuilder annotationBuilder =
                    new AnnotationMetadataBuilder(ROO_MAP_MARKER);
            if (lat != null) {
                annotationBuilder.addStringAttribute(RooMapMarker
                        .LAT_FIELD_ATTRIBUTE, lat);
            }
            if (lon != null) {
                annotationBuilder.addStringAttribute(RooMapMarker
                        .LON_FIELD_ATTRIBUTE, lon);
            }

            cidBuilder.addAnnotation(
                    annotationBuilder.build());

            typeManagementService.createOrUpdateTypeOnDisk(
                    cidBuilder.build());
        }
    }

    @Override
    public void setupMapsGwt() {
        final String moduleName = projectOperations.getFocusedModuleName();
        for (Element propertyElement : XmlUtils.findElements(
                "/configuration/batch/properties/*",
                XmlUtils.getConfiguration(getClass()))) {
            projectOperations.addProperty(moduleName,
                    new Property(propertyElement));
        }

        final List<Dependency> dependencies = new ArrayList<Dependency>();
        for (Element dependencyElement : XmlUtils.findElements(
                "/configuration/batch/dependencies/dependency",
                XmlUtils.getConfiguration(getClass()))) {
            dependencies.add(new Dependency(dependencyElement));
        }

        projectOperations.addDependencies(moduleName, dependencies);
        
        try {
            gwtTypeService.addInheritsModule(MAPS_MODULE, moduleName);
        } catch (IllegalStateException e) {
            LOGGER.warning("Problem adding " + MAPS_MODULE
                    + " to inheritance (.gwt.xml file may not yet exist)");
        }
    }
}
