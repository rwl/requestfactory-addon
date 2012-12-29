package org.springframework.roo.addon.requestfactory.android.project.metadata;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_BUNDLE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_RESOURCES;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_CONTEXT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_VIEW;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_VIEW_GROUP;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_LAYOUT_INFLATER;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ON_CREATE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_STRING;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_SYSTEM_SERVICE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_VIEW;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ON_CREATE_VIEW;
import static org.springframework.roo.model.JavaType.VOID_PRIMITIVE;
import static org.springframework.roo.model.JavaType.INT_PRIMITIVE;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.android.types.SystemService;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MethodMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotatedJavaType;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.itd.AbstractItdTypeDetailsProvidingMetadataItem;
import org.springframework.roo.classpath.itd.InvocableMemberBodyBuilder;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.ImportRegistrationResolver;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.LogicalPath;


public class AndroidFragmentMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = AndroidFragmentMetadata.class.getName();

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName ON_CREATE_METHOD = new JavaSymbolName("onCreate");
    private static final JavaSymbolName ON_CREATE_VIEW_METHOD = new JavaSymbolName("onCreateView");
    private static final JavaSymbolName INIT_VIEWS_METHOD = new JavaSymbolName("initViews");
    private static final JavaSymbolName INIT_RESOURCES_METHOD = new JavaSymbolName("initResources");
    private static final JavaSymbolName INIT_SERVICES_METHOD = new JavaSymbolName("initServices");
    private static final JavaSymbolName FIND_VIEW_BY_ID_METHOD = new JavaSymbolName("findViewById");

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(JavaType javaType, LogicalPath path) {
        return PhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, javaType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final LogicalPath getPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    private final FragmentAnnotationValues fragmentAnnotationValues;

    private final JavaType layoutType;
    private final JavaType idType;
    private final JavaType stringType;

    private final ImportRegistrationResolver resolver;

    private FieldMetadata contentViewField;

    public AndroidFragmentMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final FragmentAnnotationValues fragmentAnnotationValues,
            final String topLevelPackage) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.fragmentAnnotationValues = fragmentAnnotationValues;

        this.layoutType = new JavaType(topLevelPackage + ".R.layout");
        this.idType = new JavaType(topLevelPackage + ".R.id");
        this.stringType = new JavaType(topLevelPackage + ".R.string");

        this.resolver = builder.getImportRegistrationResolver();

        if (!isValid()) {
            return;
        }

        builder.addMethod(getOnCreateMethod());

        contentViewField = getContentViewField();
        builder.addField(contentViewField);
        builder.addMethod(getOnCreateViewMethod());

        builder.addMethod(getInitViewsMethod());
        builder.addMethod(getInitResourcesMethod());
        builder.addMethod(getInitServicesMethod());

        builder.addMethod(getFindViewById());

        itdTypeDetails = builder.build();
    }

    private MethodMetadata getOnCreateMethod() {
        final MethodMetadata method = getGovernorMethod(ON_CREATE_METHOD,
                ANDROID_BUNDLE);
        if (method != null) {
            return method;
        }

        final List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(ANDROID_BUNDLE);
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("savedInstanceState"));

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        if (governorTypeDetails.getFieldsWithAnnotation(ROO_STRING)
                .size() > 0) {
            bodyBuilder.appendFormalLine(INIT_RESOURCES_METHOD.getSymbolName()
                    + "();");
        }
        if (governorTypeDetails.getFieldsWithAnnotation(ROO_SYSTEM_SERVICE)
                .size() > 0) {
            bodyBuilder.appendFormalLine(INIT_SERVICES_METHOD.getSymbolName()
                    + "();");
        }

        bodyBuilder.appendFormalLine("super.onCreate(savedInstanceState);");

        for (MethodMetadata methodMetadata : governorTypeDetails
                .getDeclaredMethods()) {
            final AnnotationMetadata onCreate = methodMetadata.getAnnotation(
                    ROO_ON_CREATE);
            if (onCreate == null) {
                continue;
            }
            final List<AnnotatedJavaType> paramTypes = methodMetadata
                    .getParameterTypes();
            final String call;
            if (paramTypes.size() == 0) {
                call = "();";
            } else if (paramTypes.size() == 1 && paramTypes.get(0)
                    .getJavaType().equals(ANDROID_BUNDLE)) {
                call = "(savedInstanceState);";
            } else {
                continue;
            }
            bodyBuilder.appendFormalLine(methodMetadata.getMethodName()
                    .getSymbolName() + call);
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, ON_CREATE_METHOD, VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private FieldMetadata getContentViewField() {
        final JavaSymbolName fieldName = new JavaSymbolName("contentView");
        final FieldMetadata existing = governorTypeDetails.getField(
                fieldName);
        if (existing != null) {
            return existing;
        }
        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                getId(), 0, Collections.<AnnotationMetadataBuilder>emptyList(),
                fieldName, ANDROID_VIEW);
        return fieldBuilder.build();
    }

    private MethodMetadata getOnCreateViewMethod() {
        final MethodMetadata method = getGovernorMethod(ON_CREATE_VIEW_METHOD,
                ANDROID_LAYOUT_INFLATER, ANDROID_VIEW_GROUP, ANDROID_BUNDLE);
        if (method != null) {
            return method;
        }

        final List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(ANDROID_LAYOUT_INFLATER,
                        ANDROID_VIEW_GROUP, ANDROID_BUNDLE);
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("inflater"),
                        new JavaSymbolName("container"),
                        new JavaSymbolName("savedInstanceState"));

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        final String contentViewFieldName = contentViewField.getFieldName()
                .getSymbolName();
        bodyBuilder.appendFormalLine(contentViewFieldName
                + " = super.onCreateView(inflater, container, savedInstanceState);");

        final String layout = fragmentAnnotationValues.getLayout();
        if (!StringUtils.isEmpty(layout)) {
            bodyBuilder.appendFormalLine("if (" + contentViewFieldName
                    + " == null) {");

            bodyBuilder.appendFormalLine("    " + contentViewFieldName
                    + " = inflater.inflate(" + layoutType
                    .getNameIncludingTypeParameters(true, resolver) + "."
                    + layout + ", container, false);");

            bodyBuilder.appendFormalLine("}");
        }
        if (governorTypeDetails.getFieldsWithAnnotation(ROO_VIEW)
                .size() > 0) {
            bodyBuilder.appendFormalLine(INIT_VIEWS_METHOD.getSymbolName()
                    + "();");
        }
        for (MethodMetadata methodMetadata : governorTypeDetails
                .getDeclaredMethods()) {
            final AnnotationMetadata onCreate = methodMetadata
                    .getAnnotation(ROO_ON_CREATE_VIEW);
            if (onCreate == null) {
                continue;
            }
            final List<AnnotatedJavaType> paramTypes = methodMetadata
                    .getParameterTypes();
            final String call;
            if (paramTypes.size() == 0) {
                call = "();";
            } else if (paramTypes.size() == 3
                    && paramTypes.get(0).getJavaType().equals(
                            ANDROID_LAYOUT_INFLATER)
                    && paramTypes.get(1).getJavaType().equals(
                            ANDROID_VIEW_GROUP)
                    && paramTypes.get(2).getJavaType().equals(
                            ANDROID_BUNDLE)) {
                call = "(inflater, container, savedInstanceState);";
            } else {
                continue;
            }
            bodyBuilder.appendFormalLine(methodMetadata.getMethodName()
                    .getSymbolName() + call);
        }

        bodyBuilder.appendFormalLine("return " + contentViewFieldName + ";");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, ON_CREATE_VIEW_METHOD,
                contentViewField.getFieldType(), parameterTypes,
                parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getInitViewsMethod() {
        final MethodMetadata method = getGovernorMethod(INIT_VIEWS_METHOD);
        if (method != null) {
            return method;
        }
        final List<FieldMetadata> viewFields = governorTypeDetails
                .getFieldsWithAnnotation(ROO_VIEW);
        if (viewFields.isEmpty()) {
            return null;
        }

        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        for (FieldMetadata field : viewFields) {
            final String identifier = RequestFactoryUtils
                    .getStringAnnotationValue(field, ROO_VIEW, "value",
                            field.getFieldName().getSymbolName());

            final String fieldType = field.getFieldType()
                    .getNameIncludingTypeParameters(true, resolver);
            bodyBuilder.appendFormalLine(field.getFieldName()
                    + " = (" + (fieldType) + ") findViewById(" + idType
                    .getNameIncludingTypeParameters(true, resolver) + "."
                    + identifier + ");");
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, INIT_VIEWS_METHOD, VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getInitResourcesMethod() {
        final MethodMetadata method = getGovernorMethod(INIT_RESOURCES_METHOD);
        if (method != null) {
            return method;
        }
        final List<FieldMetadata> stringFields = governorTypeDetails
                .getFieldsWithAnnotation(ROO_STRING);
        if (stringFields.isEmpty()) {
            return null;
        }

        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("final " + ANDROID_RESOURCES
                .getNameIncludingTypeParameters(true, resolver)
                        + " resources = getResources();");

        for (FieldMetadata field : stringFields) {
            final String name = RequestFactoryUtils
                    .getStringAnnotationValue(field, ROO_STRING, "value",
                            field.getFieldName().getSymbolName());

            bodyBuilder.appendFormalLine(field.getFieldName()
                    + " = resources.getString(" + stringType
                    .getNameIncludingTypeParameters(true, resolver) + "."
                    + name + ");");
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, INIT_RESOURCES_METHOD,
                VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getInitServicesMethod() {
        final MethodMetadata method = getGovernorMethod(INIT_SERVICES_METHOD);
        if (method != null) {
            return method;
        }
        final List<FieldMetadata> serviceFields = governorTypeDetails
                .getFieldsWithAnnotation(ROO_SYSTEM_SERVICE);
        if (serviceFields.isEmpty()) {
            return null;
        }

        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        for (FieldMetadata field : serviceFields) {
            final SystemService systemService = SystemService.forType(field
                    .getFieldType());
            bodyBuilder.appendFormalLine(field.getFieldName() + " = (("
                    + field.getFieldType().getNameIncludingTypeParameters(
                            true, resolver) + ") this.getSystemService("
                    + ANDROID_CONTEXT.getNameIncludingTypeParameters(true,
                            resolver) + "." + systemService.getContextField()
                            + "));");
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, INIT_SERVICES_METHOD,
                VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    private MethodMetadata getFindViewById() {
        final MethodMetadata method = getGovernorMethod(
                FIND_VIEW_BY_ID_METHOD, INT_PRIMITIVE);
        if (method != null) {
            return method;
        }

        final List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(INT_PRIMITIVE);
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();
        parameterNames.add(new JavaSymbolName("id"));

        final String contentViewFieldName = contentViewField.getFieldName()
                .getSymbolName();
        
        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        bodyBuilder.appendFormalLine("if (" + contentViewFieldName
                + " == null) {");
        bodyBuilder.appendFormalLine("    return null;");
        bodyBuilder.appendFormalLine("}");
        bodyBuilder.appendFormalLine("return " + contentViewFieldName
                + ".findViewById(id);");

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PUBLIC, FIND_VIEW_BY_ID_METHOD,
                ANDROID_VIEW, parameterTypes, parameterNames, bodyBuilder);

        return methodBuilder.build();
    }

    public String toString() {
        final ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("identifier", getId());
        builder.append("valid", valid);
        builder.append("aspectName", aspectName);
        builder.append("destinationType", destination);
        builder.append("governor", governorPhysicalTypeMetadata.getId());
        builder.append("itdTypeDetails", itdTypeDetails);
        return builder.toString();
    }
}
