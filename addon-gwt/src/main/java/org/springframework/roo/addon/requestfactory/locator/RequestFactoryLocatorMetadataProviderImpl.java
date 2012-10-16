package org.springframework.roo.addon.requestfactory.locator;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.LOCATOR;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_LOCATOR;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_REQUEST;
import static org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType.KEY;
import static org.springframework.roo.addon.requestfactory.scaffold.ScaffoldJavaType.KEY_FACTORY;
import static org.springframework.roo.model.JavaType.CLASS;
import static org.springframework.roo.model.JavaType.STRING;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.RooRequestFactoryProxy;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.customdata.CustomDataKeys;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetailsBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.classpath.layers.LayerService;
import org.springframework.roo.classpath.layers.LayerType;
import org.springframework.roo.classpath.layers.MemberTypeAdditions;
import org.springframework.roo.classpath.layers.MethodParameter;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.RooJavaType;
import org.springframework.roo.model.SpringJavaType;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectOperations;


@Component(immediate = true)
@Service
public class RequestFactoryLocatorMetadataProviderImpl implements
        RequestFactoryLocatorMetadataProvider {

    private static final int LAYER_POSITION = LayerType.HIGHEST.getPosition();

    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference LayerService layerService;
    @Reference MetadataDependencyRegistry metadataDependencyRegistry;
    @Reference MetadataService metadataService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;
    @Reference TypeManagementService typeManagementService;

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

    public MetadataItem get(final String metadataIdentificationString) {
        final ClassOrInterfaceTypeDetails proxy = getGovernor(metadataIdentificationString);
        if (proxy == null) {
            return null;
        }

        final AnnotationMetadata proxyAnnotation = RequestFactoryUtils.getFirstAnnotation(
                proxy, RequestFactoryUtils.PROXY_ANNOTATIONS);
        if (proxyAnnotation == null) {
            return null;
        }

        final String locatorType = RequestFactoryUtils.getStringValue(proxyAnnotation
                .getAttribute("locator"));
        if (StringUtils.isBlank(locatorType)) {
            return null;
        }

        final ClassOrInterfaceTypeDetails entityType = requestFactoryTypeService
                .lookupEntityFromProxy(proxy);
        if (entityType == null || Modifier.isAbstract(entityType.getModifier())) {
            return null;
        }

        final JavaType entity = entityType.getName();
        final MethodMetadata identifierAccessor = persistenceMemberLocator
                .getIdentifierAccessor(entity);
        final MethodMetadata versionAccessor = persistenceMemberLocator
                .getVersionAccessor(entity);
        if (identifierAccessor == null || versionAccessor == null) {
            return null;
        }

        final JavaType identifierType = RequestFactoryUtils.convertPrimitiveType(
                identifierAccessor.getReturnType(), true);

        LogicalPath locatorPath = null;
        AnnotationMetadata rooProxyAnnotation = proxy.getAnnotation(ROO_REQUEST_FACTORY_PROXY);
        if (rooProxyAnnotation != null) {
            AnnotationAttributeValue<String> locatorModuleAttributeValue = rooProxyAnnotation
                    .getAttribute(RooRequestFactoryProxy.LOCATOR_MODULE_ATTRIBUTE);
            if (locatorModuleAttributeValue != null) {
                String locatorModule = locatorModuleAttributeValue.getValue();
                if (!locatorModule.isEmpty()) {
                    locatorPath = LogicalPath.getInstance(Path.SRC_MAIN_JAVA, locatorModule);
                }
            }
        }
        if (locatorPath == null) {
            locatorPath = PhysicalTypeIdentifier.getPath(proxy
                    .getDeclaredByMetadataId());
        }
        final String locatorPhysicalTypeId = PhysicalTypeIdentifier
                .createIdentifier(new JavaType(locatorType), locatorPath);
        final ClassOrInterfaceTypeDetailsBuilder cidBuilder = new ClassOrInterfaceTypeDetailsBuilder(
                locatorPhysicalTypeId);
        final AnnotationMetadataBuilder annotationMetadataBuilder = new AnnotationMetadataBuilder(
                ROO_REQUEST_FACTORY_LOCATOR);
        annotationMetadataBuilder.addStringAttribute("value",
                entity.getFullyQualifiedTypeName());
        cidBuilder.addAnnotation(annotationMetadataBuilder);

        cidBuilder.addAnnotation(new AnnotationMetadataBuilder(
                SpringJavaType.COMPONENT));
        cidBuilder.setName(new JavaType(locatorType));
        cidBuilder.setModifier(Modifier.PUBLIC);
        cidBuilder.setPhysicalTypeCategory(PhysicalTypeCategory.CLASS);
        cidBuilder.addExtendsTypes(new JavaType(LOCATOR
                .getFullyQualifiedTypeName(), 0, DataType.TYPE, null, Arrays
                .asList(entity, identifierType.equals(KEY) ? STRING : identifierType)));
        cidBuilder.addMethod(getCreateMethod(locatorPhysicalTypeId, entity));

        final MemberTypeAdditions findMethodAdditions = layerService
                .getMemberTypeAdditions(locatorPhysicalTypeId,
                        CustomDataKeys.FIND_METHOD.name(), entity,
                        identifierType, LAYER_POSITION, new MethodParameter(
                                identifierType, "id"));
        Validate.notNull(
                findMethodAdditions,
                "Find method not available for entity '"
                        + entity.getFullyQualifiedTypeName() + "'");
        cidBuilder.addMethod(getFindMethod(findMethodAdditions, cidBuilder,
                locatorPhysicalTypeId, entity, identifierType));

        cidBuilder
                .addMethod(getDomainTypeMethod(locatorPhysicalTypeId, entity));
        cidBuilder.addMethod(getIdMethod(locatorPhysicalTypeId, entity,
                identifierAccessor));
        cidBuilder.addMethod(getIdTypeMethod(locatorPhysicalTypeId, entity,
                identifierType.equals(KEY) ? STRING : identifierType));
        cidBuilder.addMethod(getVersionMethod(locatorPhysicalTypeId, entity,
                versionAccessor));

        typeManagementService.createOrUpdateTypeOnDisk(cidBuilder.build());
        return null;
    }

    private MethodMetadataBuilder getCreateMethod(final String declaredById,
            final JavaType targetType) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();
        invocableMemberBodyBuilder.append("return new "
                + targetType.getSimpleTypeName() + "();");
        final MethodMetadataBuilder createMethodBuilder = new MethodMetadataBuilder(
                declaredById, Modifier.PUBLIC, new JavaSymbolName("create"),
                targetType, invocableMemberBodyBuilder);
        final JavaType wildEntityType = new JavaType(
                targetType.getFullyQualifiedTypeName(), 0, DataType.VARIABLE,
                JavaType.WILDCARD_EXTENDS, null);
        final JavaType classParameterType = new JavaType(
                JavaType.CLASS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(wildEntityType));
        createMethodBuilder.addParameter("clazz", classParameterType);
        return createMethodBuilder;
    }

    private MethodMetadataBuilder getDomainTypeMethod(
            final String declaredById, final JavaType targetType) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();
        invocableMemberBodyBuilder.append("return "
                + targetType.getSimpleTypeName() + ".class;");
        final JavaType returnType = new JavaType(
                CLASS.getFullyQualifiedTypeName(), 0, DataType.TYPE, null,
                Arrays.asList(targetType));
        return new MethodMetadataBuilder(declaredById, Modifier.PUBLIC,
                new JavaSymbolName("getDomainType"), returnType,
                invocableMemberBodyBuilder);
    }

    private MethodMetadataBuilder getFindMethod(
            final MemberTypeAdditions findMethodAdditions,
            final ClassOrInterfaceTypeDetailsBuilder locatorBuilder,
            final String declaredById, final JavaType targetType,
            final JavaType idType) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();

        if (idType.equals(KEY)) {
            invocableMemberBodyBuilder.append("return ")
                .append(targetType.getSimpleTypeName())
                .append(".")
                .append(findMethodAdditions.getMethodName())
                .append("(")
                .append(KEY_FACTORY.getFullyQualifiedTypeName())
                .append(".")
                .append("stringToKey")
                .append("(")
                .append(findMethodAdditions.getMethodParameters().get(0).getRight().getSymbolName())
                .append(")")
                .append(")")
                .append(";");
        } else {
            invocableMemberBodyBuilder.append("return ")
                    .append(findMethodAdditions.getMethodCall()).append(";");
        }

        findMethodAdditions.copyAdditionsTo(locatorBuilder,
                locatorBuilder.build());
        final MethodMetadataBuilder findMethodBuilder = new MethodMetadataBuilder(
                declaredById, Modifier.PUBLIC, new JavaSymbolName("find"),
                targetType, invocableMemberBodyBuilder);
        final JavaType wildEntityType = new JavaType(
                targetType.getFullyQualifiedTypeName(), 0, DataType.VARIABLE,
                JavaType.WILDCARD_EXTENDS, null);
        final JavaType classParameterType = new JavaType(
                JavaType.CLASS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(wildEntityType));
        findMethodBuilder.addParameter("clazz", classParameterType);
        findMethodBuilder.addParameter("id", idType.equals(KEY) ? STRING : idType);
        return findMethodBuilder;
    }

    private ClassOrInterfaceTypeDetails getGovernor(
            final String metadataIdentificationString) {
        final JavaType governorTypeName = RequestFactoryLocatorMetadata
                .getJavaType(metadataIdentificationString);
        final LogicalPath governorTypePath = RequestFactoryLocatorMetadata
                .getPath(metadataIdentificationString);
        final String physicalTypeId = PhysicalTypeIdentifier.createIdentifier(
                governorTypeName, governorTypePath);
        return typeLocationService.getTypeDetails(physicalTypeId);
    }

    private MethodMetadataBuilder getIdMethod(final String declaredById,
            final JavaType targetType, final MethodMetadata idAccessor) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();
        String methodCall = StringUtils.uncapitalize(targetType.getSimpleTypeName()).toString()
                + "." + idAccessor.getMethodName() + "()";
        if (idAccessor.getReturnType().equals(KEY)) {
            invocableMemberBodyBuilder
                    .append("if (" + methodCall + " != null) {\n")
                    .append("return ")
                    .append(KEY_FACTORY.getFullyQualifiedTypeName())
                    .append(".keyToString(" + methodCall + ");\n")
                    .append("\t\t} else {\n")
                    .append("return null;\n")
                    .append("\t}");
                    /*.append("return ")
                    .append(KEY_FACTORY.getFullyQualifiedTypeName())
                    .append(".keyToString(" + methodCall + ");");*/
        } else {
            invocableMemberBodyBuilder.append("return " + methodCall + ";");
        }
        final MethodMetadataBuilder getIdMethod = new MethodMetadataBuilder(
                declaredById,
                Modifier.PUBLIC,
                new JavaSymbolName("getId"),
                RequestFactoryUtils.convertPrimitiveType(idAccessor.getReturnType().equals(KEY) ? STRING : idAccessor.getReturnType(), true),
                invocableMemberBodyBuilder);
        getIdMethod.addParameter(
                StringUtils.uncapitalize(targetType.getSimpleTypeName()),
                targetType);
        return getIdMethod;
    }

    private MethodMetadataBuilder getIdTypeMethod(final String declaredById,
            final JavaType targetType, final JavaType idType) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();
        invocableMemberBodyBuilder.append("return "
                + idType.getSimpleTypeName() + ".class;");
        final JavaType returnType = new JavaType(
                JavaType.CLASS.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                null, Arrays.asList(idType));
        return new MethodMetadataBuilder(declaredById, Modifier.PUBLIC,
                new JavaSymbolName("getIdType"), returnType,
                invocableMemberBodyBuilder);
    }

    public String getProvidesType() {
        return RequestFactoryLocatorMetadata.getMetadataIdentifierType();
    }

    private MethodMetadataBuilder getVersionMethod(final String declaredById,
            final JavaType targetType, final MethodMetadata versionAccessor) {
        final InvocableMemberBodyBuilder invocableMemberBodyBuilder = InvocableMemberBodyBuilder
                .getInstance();
        invocableMemberBodyBuilder.append("return "
                + StringUtils.uncapitalize(targetType.getSimpleTypeName())
                + "." + versionAccessor.getMethodName() + "();");
        final MethodMetadataBuilder getIdMethodBuilder = new MethodMetadataBuilder(
                declaredById, Modifier.PUBLIC,
                new JavaSymbolName("getVersion"), JavaType.OBJECT,
                invocableMemberBodyBuilder);
        getIdMethodBuilder.addParameter(
                StringUtils.uncapitalize(targetType.getSimpleTypeName()),
                targetType);
        return getIdMethodBuilder;
    }

    public void notify(final String upstreamDependency,
            String downstreamDependency) {
        if (MetadataIdentificationUtils
                .isIdentifyingClass(downstreamDependency)) {
            Validate.isTrue(
                    MetadataIdentificationUtils.getMetadataClass(
                            upstreamDependency).equals(
                            MetadataIdentificationUtils
                                    .getMetadataClass(PhysicalTypeIdentifier
                                            .getMetadataIdentiferType())),
                    "Expected class-level notifications only for PhysicalTypeIdentifier (not '"
                            + upstreamDependency + "')");

            final ClassOrInterfaceTypeDetails cid = typeLocationService
                    .getTypeDetails(upstreamDependency);
            if (cid == null) {
                return;
            }
            boolean processed = false;
            if (cid.getAnnotation(ROO_REQUEST_FACTORY_REQUEST) != null) {
                final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                        .lookupProxyFromRequest(cid);
                if (proxy != null) {
                    final JavaType typeName = PhysicalTypeIdentifier
                            .getJavaType(proxy.getDeclaredByMetadataId());
                    final LogicalPath typePath = PhysicalTypeIdentifier
                            .getPath(proxy.getDeclaredByMetadataId());
                    downstreamDependency = RequestFactoryLocatorMetadata.createIdentifier(
                            typeName, typePath);
                    processed = true;
                }
            }
            if (!processed
                    && cid.getAnnotation(ROO_REQUEST_FACTORY_PROXY) == null) {
                boolean found = false;
                for (final ClassOrInterfaceTypeDetails proxyCid : typeLocationService
                        .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY)) {
                    final AnnotationMetadata annotationMetadata = RequestFactoryUtils
                            .getFirstAnnotation(proxyCid,
                                    RequestFactoryUtils.ROO_PROXY_REQUEST_ANNOTATIONS);
                    if (annotationMetadata != null) {
                        final AnnotationAttributeValue<?> attributeValue = annotationMetadata
                                .getAttribute("value");
                        if (attributeValue != null) {
                            final String mirrorName = RequestFactoryUtils
                                    .getStringValue(attributeValue);
                            if (mirrorName != null
                                    && cid.getName()
                                            .getFullyQualifiedTypeName()
                                            .equals(attributeValue.getValue())) {
                                found = true;
                                final JavaType typeName = PhysicalTypeIdentifier
                                        .getJavaType(proxyCid
                                                .getDeclaredByMetadataId());
                                final LogicalPath typePath = PhysicalTypeIdentifier
                                        .getPath(proxyCid
                                                .getDeclaredByMetadataId());
                                downstreamDependency = RequestFactoryLocatorMetadata
                                        .createIdentifier(typeName, typePath);
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    return;
                }
            }
            else if (!processed) {
                // A physical Java type has changed, and determine what the
                // corresponding local metadata identification string would have
                // been
                final JavaType typeName = PhysicalTypeIdentifier
                        .getJavaType(upstreamDependency);
                final LogicalPath typePath = PhysicalTypeIdentifier
                        .getPath(upstreamDependency);
                downstreamDependency = RequestFactoryLocatorMetadata.createIdentifier(
                        typeName, typePath);
            }

            // We only need to proceed if the downstream dependency relationship
            // is not already registered
            // (if it's already registered, the event will be delivered directly
            // later on)
            if (metadataDependencyRegistry.getDownstream(upstreamDependency)
                    .contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that
        // can be processed by this class
        Validate.isTrue(
                MetadataIdentificationUtils.getMetadataClass(
                        downstreamDependency).equals(
                        MetadataIdentificationUtils
                                .getMetadataClass(getProvidesType())),
                "Unexpected downstream notification for '"
                        + downstreamDependency
                        + "' to this provider (which uses '"
                        + getProvidesType() + "'");

        metadataService.evictAndGet(downstreamDependency);
    }
}
