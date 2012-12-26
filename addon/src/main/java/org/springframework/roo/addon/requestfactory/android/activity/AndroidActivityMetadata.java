package org.springframework.roo.addon.requestfactory.android.activity;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_BUNDLE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_RESOURCES;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_CONTEXT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ON_CREATE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_STRING;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_SYSTEM_SERVICE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_VIEW;
import static org.springframework.roo.model.JavaType.VOID_PRIMITIVE;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.android.types.SystemService;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
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


public class AndroidActivityMetadata extends AbstractItdTypeDetailsProvidingMetadataItem {

    private static final String PROVIDES_TYPE_STRING = AndroidActivityMetadata.class.getName();

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private static final JavaSymbolName ON_CREATE_METHOD = new JavaSymbolName("onCreate");
    private static final JavaSymbolName INIT_VIEWS_METHOD = new JavaSymbolName("initViews");
    private static final JavaSymbolName INIT_RESOURCES_METHOD = new JavaSymbolName("initResources");
    private static final JavaSymbolName INIT_SERVICES_METHOD = new JavaSymbolName("initServices");

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

    private final ActivityAnnotationValues activityAnnotationValues;

    private final JavaType layoutType;
    private final JavaType idType;
    private final JavaType stringType;

    public AndroidActivityMetadata(String identifier, JavaType aspectName,
            PhysicalTypeMetadata governorPhysicalTypeMetadata,
            final ActivityAnnotationValues activityAnnotationValues,
            final String topLevelPackage) {
        super(identifier, aspectName, governorPhysicalTypeMetadata);
        Validate.isTrue(isValid(identifier), "Metadata identification string '"
                + identifier + "' does not appear to be a valid");

        this.activityAnnotationValues = activityAnnotationValues;

        this.layoutType = new JavaType(topLevelPackage + ".R.layout");
        this.idType = new JavaType(topLevelPackage + ".R.id");
        this.stringType = new JavaType(topLevelPackage + ".R.string");

        if (!isValid()) {
            return;
        }

        builder.addMethod(getOnCreateMethod());

        builder.addMethod(getInitViewsMethod());
        builder.addMethod(getInitResourcesMethod());
        builder.addMethod(getInitServicesMethod());

        itdTypeDetails = builder.build();
    }

    private MethodMetadata getOnCreateMethod() {
        final MethodMetadata method = getGovernorMethod(ON_CREATE_METHOD,
                ANDROID_BUNDLE);
        if (method != null) {
            return method;
        }

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        final List<JavaType> throwsTypes = new ArrayList<JavaType>();
        final List<AnnotatedJavaType> parameterTypes = AnnotatedJavaType
                .convertFromJavaTypes(ANDROID_BUNDLE);
        final List<JavaSymbolName> parameterNames = Arrays
                .asList(new JavaSymbolName("savedInstanceState"));

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        if (governorTypeDetails.getFieldsWithAnnotation(ROO_STRING).size() > 0) {
            bodyBuilder.appendFormalLine(INIT_RESOURCES_METHOD.getSymbolName()
                    + "();");
        }
        if (governorTypeDetails.getFieldsWithAnnotation(ROO_SYSTEM_SERVICE).size() > 0) {
            bodyBuilder.appendFormalLine(INIT_SERVICES_METHOD.getSymbolName()
                    + "();");
        }

        bodyBuilder.appendFormalLine("super.onCreate(savedInstanceState);");

        final String layout = activityAnnotationValues.getLayout();
        if (!StringUtils.isEmpty(layout)) {
            bodyBuilder.appendFormalLine("setContentView(" + layoutType
                    .getNameIncludingTypeParameters(true, builder
                            .getImportRegistrationResolver()) + "."
                    + layout + ");");
        }
        if (governorTypeDetails.getFieldsWithAnnotation(ROO_VIEW).size() > 0) {
            bodyBuilder.appendFormalLine(INIT_VIEWS_METHOD.getSymbolName()
                    + "();");
        }
        for (MethodMetadata methodMetadata : governorTypeDetails.getDeclaredMethods()) {
            final AnnotationMetadata onCreate = methodMetadata.getAnnotation(ROO_ON_CREATE);
            if (onCreate == null) {
                continue;
            }
            final List<AnnotatedJavaType> paramTypes = methodMetadata.getParameterTypes();
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
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

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

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        final List<JavaType> throwsTypes = new ArrayList<JavaType>();
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        for (FieldMetadata field : viewFields) {
            final String identifier = RequestFactoryUtils
                    .getStringAnnotationValue(field, ROO_VIEW, "value",
                            field.getFieldName().getSymbolName());

            final String fieldType = field.getFieldType()
                    .getNameIncludingTypeParameters(true,
                            builder.getImportRegistrationResolver());
            bodyBuilder.appendFormalLine(field.getFieldName()
                    + " = (" + (fieldType) + ") findViewById(" + idType
                    .getNameIncludingTypeParameters(true, builder
                            .getImportRegistrationResolver()) + "."
                    + identifier + ");");
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, INIT_VIEWS_METHOD, VOID_PRIMITIVE,
                parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

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

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        final List<JavaType> throwsTypes = new ArrayList<JavaType>();
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();

        bodyBuilder.appendFormalLine("final " + ANDROID_RESOURCES
                .getNameIncludingTypeParameters(true,
                        builder.getImportRegistrationResolver())
                        + " resources = getResources();");

        for (FieldMetadata field : stringFields) {
            final String name = RequestFactoryUtils
                    .getStringAnnotationValue(field, ROO_STRING, "value",
                            field.getFieldName().getSymbolName());

            bodyBuilder.appendFormalLine(field.getFieldName()
                    + " = resources.getString(" + stringType
                    .getNameIncludingTypeParameters(true, builder
                            .getImportRegistrationResolver()) + "."
                    + name + ");");
        }

        final MethodMetadataBuilder methodBuilder = new MethodMetadataBuilder(
                getId(), Modifier.PRIVATE, INIT_RESOURCES_METHOD,
                VOID_PRIMITIVE, parameterTypes, parameterNames, bodyBuilder);
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

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

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        final List<JavaType> throwsTypes = new ArrayList<JavaType>();
        final List<AnnotatedJavaType> parameterTypes = new ArrayList<AnnotatedJavaType>();
        final List<JavaSymbolName> parameterNames = new ArrayList<JavaSymbolName>();

        final InvocableMemberBodyBuilder bodyBuilder = new InvocableMemberBodyBuilder();
        
        final ImportRegistrationResolver resolver = builder.getImportRegistrationResolver();

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
        methodBuilder.setAnnotations(annotations);
        methodBuilder.setThrowsTypes(throwsTypes);

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
