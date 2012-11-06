package org.springframework.roo.addon.requestfactory;

import static org.springframework.roo.model.JavaType.LONG_OBJECT;
import static org.springframework.roo.model.JavaType.OBJECT;
import static org.springframework.roo.model.JdkJavaType.BIG_DECIMAL;
import static org.springframework.roo.model.JdkJavaType.BIG_INTEGER;
import static org.springframework.roo.model.JdkJavaType.DATE;
import static org.springframework.roo.model.JpaJavaType.EMBEDDABLE;
import static org.springframework.roo.model.SpringJavaType.DATE_TIME_FORMAT;
import static org.springframework.roo.model.SpringJavaType.NUMBER_FORMAT;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

public class RequestFactoryProxyProperty {

    public static String getProxyRendererType(final JavaPackage topLevelPackage,
            final JavaType javaType) {
        return "Object";
    }

    protected List<AnnotationMetadata> annotations;
    protected String getter;
    protected String name;
    protected final ClassOrInterfaceTypeDetails ptmd;
    protected final JavaPackage topLevelPackage;

    protected final JavaType type;

    public RequestFactoryProxyProperty(final JavaPackage topLevelPackage,
            final ClassOrInterfaceTypeDetails ptmd, final JavaType type) {
        Validate.notNull(type, "Type required");
        this.topLevelPackage = topLevelPackage;
        this.ptmd = ptmd;
        this.type = type;
    }

    public RequestFactoryProxyProperty(final JavaPackage topLevelPackage,
            final ClassOrInterfaceTypeDetails ptmd, final JavaType type,
            final String name, final List<AnnotationMetadata> annotations,
            final String getter) {
        this(topLevelPackage, ptmd, type);
        this.name = name;
        this.annotations = annotations;
        this.getter = getter;
    }

    public String getBinder() {
        throw new UnsupportedOperationException();
    }

    public String getMobileBinder() {
        throw new UnsupportedOperationException();
    }

    public String forEditView() {
        throw new UnsupportedOperationException();
    }

    public String forMobileEditView() {
        throw new UnsupportedOperationException();
    }

    public String forMobileListView(final String rendererName) {
        throw new UnsupportedOperationException();
    }

    public String getCheckboxSubtype() {
        throw new UnsupportedOperationException();
    }

    public String getCollectionRenderer() {
        throw new UnsupportedOperationException();
    }

    protected String getDateTimeFormat() {
        String format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)";
        if (annotations == null || annotations.isEmpty()) {
            return format;
        }

        String style = "";
        final AnnotationMetadata annotation = MemberFindingUtils
                .getAnnotationOfType(annotations, DATE_TIME_FORMAT);
        if (annotation != null) {
            final AnnotationAttributeValue<?> attr = annotation
                    .getAttribute(new JavaSymbolName("style"));
            if (attr != null) {
                style = (String) attr.getValue();
            }
        }
        if (StringUtils.isNotBlank(style)) {
            if (style.equals("S")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT)";
            }
            else if (style.equals("M")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM)";
            }
            else if (style.equals("F")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_FULL)";
            }
            else if (style.equals("S-")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)";
            }
            else if (style.equals("M-")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM)";
            }
            else if (style.equals("F-")) {
                format = "DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_FULL)";
            }
        }
        return format;
    }

    public String getFormatter() {
        if (isCollectionOfProxy()) {
            return getCollectionRenderer() + ".render";
        }
        else if (isDate()) {
            return getDateTimeFormat() + ".format";
        }
        else if (type.equals(JavaType.INT_OBJECT)
                || type.equals(JavaType.FLOAT_OBJECT)
                || type.equals(JavaType.DOUBLE_OBJECT)
                || type.equals(BIG_INTEGER) || type.equals(BIG_DECIMAL)) {
            String formatter = "String.valueOf";
            if (annotations == null || annotations.isEmpty()) {
                return formatter;
            }

            final AnnotationMetadata annotation = MemberFindingUtils
                    .getAnnotationOfType(annotations, NUMBER_FORMAT);
            if (annotation != null) {
                final AnnotationAttributeValue<?> attr = annotation
                        .getAttribute(new JavaSymbolName("style"));
                if (attr != null) {
                    final String style = attr.getValue().toString();
                    if ("org.springframework.format.annotation.NumberFormat.Style.CURRENCY"
                            .equals(style)) {
                        formatter = "NumberFormat.getCurrencyFormat().format";
                    }
                    else if ("org.springframework.format.annotation.NumberFormat.Style.PERCENT"
                            .equals(style)) {
                        formatter = "NumberFormat.getPercentFormat().format";
                    }
                    else {
                        formatter = "NumberFormat.getDecimalFormat().format";
                    }
                }
                else {
                    formatter = "NumberFormat.getDecimalFormat().format";
                }
            }
            return formatter;
        }
        else if (isProxy()) {
            return getProxyRendererType() + ".instance().render";
        }
        else {
            return "String.valueOf";
        }
    }

    public String getGetter() {
        return getter;
    }

    public JavaSymbolName getGetterSymbolName() {
        return new JavaSymbolName(getter);
    }

    public String getName() {
        return name;
    }

    public JavaSymbolName getSymbolName() {
        return new JavaSymbolName(name);
    }

    public JavaType getPropertyType() {
        return type;
    }

    public String getProxyRendererType() {
        return getProxyRendererType(topLevelPackage,
                isCollectionOfProxy() ? type.getParameters().get(0) : type);
    }

    public String getReadableName() {
        return new JavaSymbolName(name).getReadableSymbolName();
    }

    public String getRenderer() {
        throw new UnsupportedOperationException();
    }

    public JavaType getSetEditorType() {
        throw new UnsupportedOperationException();
    }

    public String getSetValuePickerMethod() {
        throw new UnsupportedOperationException();
    }

    public String getSetEmptyValuePickerMethod() {
        throw new UnsupportedOperationException();
    }

    public String getSetValuePickerMethodName() {
        return "set" + StringUtils.capitalize(getName()) + "PickerValues";
    }

    public String getType() {
        return type.getFullyQualifiedTypeName();
    }

    public JavaType getValueType() {
        if (isCollection()) {
            return type.getParameters().get(0);
        }
        return type;
    }

    public boolean isBoolean() {
        return type.equals(JavaType.BOOLEAN_OBJECT);
    }

    public boolean isCollection() {
        return type.isCommonCollectionType();
    }

    public boolean isCollectionOfProxy() {
        return type.getParameters().size() != 0
                && isCollection()
                && new RequestFactoryProxyProperty(topLevelPackage, ptmd, type
                        .getParameters().get(0)).isProxy();
    }

    public boolean isDate() {
        return type.equals(DATE);
    }

    public boolean isEmbeddable() {
        if (ptmd != null) {
            final List<AnnotationMetadata> annotations = ptmd.getAnnotations();
            for (final AnnotationMetadata annotation : annotations) {
                if (annotation.getAnnotationType().equals(EMBEDDABLE)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEnum() {
        return ptmd != null
                && ptmd.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION;
    }

    public boolean isPrimitive() {
        return type.isPrimitive() || isDate() || isString() || isBoolean()
                || type.equals(JavaType.DOUBLE_OBJECT)
                || type.equals(LONG_OBJECT) || type.equals(JavaType.INT_OBJECT)
                || type.equals(JavaType.FLOAT_OBJECT)
                || type.equals(JavaType.BYTE_OBJECT)
                || type.equals(JavaType.SHORT_OBJECT)
                || type.equals(JavaType.CHAR_OBJECT)
                || type.equals(BIG_DECIMAL);
    }

    public boolean isProxy() {
        return ptmd != null && !isDate() && !isString() && !isPrimitive()
                && !isEnum() && !isCollection() && !isEmbeddable()
                && !type.equals(OBJECT);
    }

    public boolean isString() {
        return type.equals(JavaType.STRING);
    }
}