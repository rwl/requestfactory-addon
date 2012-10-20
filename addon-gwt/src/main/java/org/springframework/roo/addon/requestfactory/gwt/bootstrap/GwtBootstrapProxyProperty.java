package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import static org.springframework.roo.model.JavaType.LONG_OBJECT;
import static org.springframework.roo.model.JavaType.OBJECT;
import static org.springframework.roo.model.JdkJavaType.BIG_DECIMAL;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaType;

public class GwtBootstrapProxyProperty extends RequestFactoryProxyProperty {

    private static final String ALTERNATE_SIZE = "LARGE";

    public static String getProxyRendererType(
            final JavaPackage topLevelPackage, final JavaType javaType) {
        return GwtBootstrapType.EDIT_RENDERER.getPath().packageName(topLevelPackage)
                + "." + javaType.getSimpleTypeName() + "Renderer";
    }

    public GwtBootstrapProxyProperty(JavaPackage topLevelPackage,
            ClassOrInterfaceTypeDetails ptmd, JavaType type) {
        super(topLevelPackage, ptmd, type);
    }

    public GwtBootstrapProxyProperty(final JavaPackage topLevelPackage,
            final ClassOrInterfaceTypeDetails ptmd, final JavaType type,
            final String name, final List<AnnotationMetadata> annotations,
            final String getter) {
        super(topLevelPackage, ptmd, type, name, annotations, getter);
    }

    public String getBinder() {
        if (type.equals(JavaType.DOUBLE_OBJECT)) {
            return "b:DoubleBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(LONG_OBJECT)) {
            return "b:LongBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(JavaType.INT_OBJECT)) {
            return "b:IntegerBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(JavaType.FLOAT_OBJECT)) {
            return "r:FloatBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(JavaType.BYTE_OBJECT)) {
            return "r:ByteBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(JavaType.SHORT_OBJECT)) {
            return "r:ShortBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(JavaType.CHAR_OBJECT)) {
            return "r:CharBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        if (type.equals(BIG_DECIMAL)) {
            return "r:BigDecimalBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'";
        }
        return isCollection() ? "e:" + getSetEditor()
                : isDate() ? "d:DateBox"
                : isBoolean() ? "b:CheckBox"
                : isString() ? "b:TextBox b:id='" + name + "' alternateSize='" + ALTERNATE_SIZE + "'"
                : "b:ValueListBox";
    }

    public String forEditView() {
        String initializer = "";

        if (isBoolean()) {
            initializer = " = " + getCheckboxSubtype();
        }

        if (isEnum() && !isCollection()) {
            initializer = String.format(" = new ValueListBox<%s>(%s)",
                    type.getFullyQualifiedTypeName(), getRenderer());
        }

        if (isProxy()) {
            initializer = String
                    .format(" = new ValueListBox<%1$s>(%2$s.instance(), new com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider<%1$s>())",
                            type.getFullyQualifiedTypeName(),
                            getProxyRendererType());
        }

        return String.format("@UiField %s %s %s", getEditor(), getName(),
                initializer);
    }

    public String forMobileListView(final String rendererName) {
        return new StringBuilder("if (value.").append(getGetter())
                .append("() != null) {\n\t\t\t\tsb.appendEscaped(")
                .append(rendererName).append(".render(value.")
                .append(getGetter()).append("()));\n\t\t\t}").toString();
    }

    public String getCheckboxSubtype() {
        // TODO: Ugly hack, fix in M4
        return "new CheckBox() { public void setValue(Boolean value) { super.setValue(value == null ? Boolean.FALSE : value); } }";
    }

    public String getCollectionRenderer() {
        JavaType arg = OBJECT;
        if (type.getParameters().size() > 0) {
            arg = type.getParameters().get(0);
        }
        return GwtBootstrapPaths.SCAFFOLD_PLACE.packageName(topLevelPackage)
                + ".CollectionRenderer.of("
                + new GwtBootstrapProxyProperty(topLevelPackage, ptmd, arg)
                        .getRenderer() + ")";
    }

    @Override
    public String getProxyRendererType() {
        return getProxyRendererType(topLevelPackage,
                isCollectionOfProxy() ? type.getParameters().get(0) : type);
    }

    private String getEditor() {
        if (type.equals(JavaType.DOUBLE_OBJECT)) {
            return "DoubleBox";
        }
        if (type.equals(LONG_OBJECT)) {
            return "LongBox";
        }
        if (type.equals(JavaType.INT_OBJECT)) {
            return "IntegerBox";
        }
        if (type.equals(JavaType.FLOAT_OBJECT)) {
            return "FloatBox";
        }
        if (type.equals(JavaType.BYTE_OBJECT)) {
            return "ByteBox";
        }
        if (type.equals(JavaType.SHORT_OBJECT)) {
            return "ShortBox";
        }
        if (type.equals(JavaType.CHAR_OBJECT)) {
            return "CharBox";
        }
        if (type.equals(BIG_DECIMAL)) {
            return "BigDecimalBox";
        }
        if (isBoolean()) {
            return "(provided = true) CheckBox";
        }
        return isCollection() ? getSetEditor() : isDate() ? "DateBox"
                : isString() ? "TextBox" : "(provided = true) ValueListBox<"
                        + type.getFullyQualifiedTypeName() + ">";
    }

    public String getRenderer() {
        return isCollection() ? getCollectionRenderer()
                : isDate() ? "new DateTimeFormatRenderer("
                        + getDateTimeFormat() + ")"
                        : isPrimitive() || isEnum() || isEmbeddable()
                                || type.equals(OBJECT) ? "new AbstractRenderer<"
                                + getType()
                                + ">() {\n        public String render("
                                + getType()
                                + " obj) {\n          return obj == null ? \"\" : String.valueOf(obj);\n        }\n      }"
                                : getProxyRendererType() + ".instance()";
    }

    private String getSetEditor() {
        String typeName = OBJECT.getFullyQualifiedTypeName();
        if (type.getParameters().size() > 0) {
            typeName = type.getParameters().get(0).getSimpleTypeName();
        }
        if (typeName.endsWith(RequestFactoryType.PROXY.getSuffix())) {
            typeName = typeName.substring(0, typeName.length()
                    - RequestFactoryType.PROXY.getSuffix().length());
        }
        return typeName
                + (type.getSimpleTypeName().equals("Set") ? GwtBootstrapType.SET_EDITOR
                        .getSuffix() : GwtBootstrapType.LIST_EDITOR.getSuffix());
    }

    public JavaType getSetEditorType() {
        return new JavaType(GwtBootstrapType.SET_EDITOR.getPath().packageName(
                topLevelPackage)
                + "." + getSetEditor());
    }

    public String getSetValuePickerMethod() {
        return "\tpublic void "
                + getSetValuePickerMethodName()
                + "(Collection<"
                + (isCollection() ? type.getParameters().get(0)
                        .getSimpleTypeName() : type.getSimpleTypeName())
                + "> values) {\n" + "\t\t" + getName()
                + ".setAcceptableValues(values);\n" + "\t}\n";
    }

    public String getSetEmptyValuePickerMethod() {
        return "\tpublic void "
                + getSetValuePickerMethodName()
                + "(Collection<"
                + (isCollection() ? type.getParameters().get(0)
                        .getSimpleTypeName() : type.getSimpleTypeName())
                + "> values) { }";
    }

    public String getSetValuePickerMethodName() {
        return "set" + StringUtils.capitalize(getName()) + "PickerValues";
    }

}
