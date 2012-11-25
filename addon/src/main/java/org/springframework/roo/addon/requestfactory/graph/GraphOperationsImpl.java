package org.springframework.roo.addon.requestfactory.graph;

import static org.springframework.roo.addon.requestfactory.graph.GraphJavaType.ROO_GRAPH_NODE;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ACTIVE_RECORD;
import static org.springframework.roo.model.RooJavaType.ROO_JPA_ENTITY;
import static org.springframework.roo.model.RooJavaType.ROO_MONGO_ENTITY;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.annotations.graph.RooGraphNode;
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
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Element;

@Component
@Service
public class GraphOperationsImpl implements GraphOperations {

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
    public void annotateNodeType(final JavaType javaType, final String x,
            final String y) {
        Validate.notNull(javaType, "Java type required");

        final ClassOrInterfaceTypeDetails existing = typeLocationService
                .getTypeDetails(javaType);

        if (existing != null && MemberFindingUtils.getAnnotationOfType(
                existing.getAnnotations(), ROO_GRAPH_NODE) == null) {
            final ClassOrInterfaceTypeDetailsBuilder cidBuilder =
                    new ClassOrInterfaceTypeDetailsBuilder(existing);

            final AnnotationMetadataBuilder annotationBuilder =
                    new AnnotationMetadataBuilder(ROO_GRAPH_NODE);
            if (x != null) {
                annotationBuilder.addStringAttribute(RooGraphNode
                        .X_FIELD_ATTRIBUTE, x);
            }
            if (y != null) {
                annotationBuilder.addStringAttribute(RooGraphNode
                        .Y_FIELD_ATTRIBUTE, y);
            }

            cidBuilder.addAnnotation(
                    annotationBuilder.build());

            typeManagementService.createOrUpdateTypeOnDisk(
                    cidBuilder.build());
        }
    }

    @Override
    public void setupGraph() {
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
    }
}
