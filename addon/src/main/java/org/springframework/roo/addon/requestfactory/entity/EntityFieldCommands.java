package org.springframework.roo.addon.requestfactory.entity;

import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.EXCLUDE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.READ_ONLY;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.INVISIBLE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.UNEDITABLE;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.HELP_TEXT;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.TEXT_AREA;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.PASSWORD;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.UNOWNED;
import static org.springframework.roo.addon.requestfactory.entity.EntityJavaType.UNITS;
import static org.springframework.roo.model.JdkJavaType.LIST;
import static org.springframework.roo.model.JdkJavaType.SET;
import static org.springframework.roo.model.JpaJavaType.EMBEDDABLE;
import static org.springframework.roo.model.JpaJavaType.ENTITY;
import static org.springframework.roo.model.SpringJavaType.PERSISTENT;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeDetails;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.operations.Cardinality;
import org.springframework.roo.classpath.operations.DateTime;
import org.springframework.roo.classpath.operations.EnumType;
import org.springframework.roo.classpath.operations.Fetch;
import org.springframework.roo.classpath.operations.jsr303.BooleanField;
import org.springframework.roo.classpath.operations.jsr303.CollectionField;
import org.springframework.roo.classpath.operations.jsr303.DateField;
import org.springframework.roo.classpath.operations.jsr303.DateFieldPersistenceType;
import org.springframework.roo.classpath.operations.jsr303.EmbeddedField;
import org.springframework.roo.classpath.operations.jsr303.EnumField;
import org.springframework.roo.classpath.operations.jsr303.FieldDetails;
import org.springframework.roo.classpath.operations.jsr303.NumericField;
import org.springframework.roo.classpath.operations.jsr303.ReferenceField;
import org.springframework.roo.classpath.operations.jsr303.SetField;
import org.springframework.roo.classpath.operations.jsr303.StringField;
import org.springframework.roo.classpath.operations.jsr303.UploadedFileContentType;
import org.springframework.roo.classpath.operations.jsr303.UploadedFileField;
import org.springframework.roo.classpath.scanner.MemberDetails;
import org.springframework.roo.classpath.scanner.MemberDetailsScanner;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.DataType;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.model.JdkJavaType;
import org.springframework.roo.model.ReservedWords;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * Additional shell commands for the purpose of creating fields.
 *
 * @author Ben Alex
 * @author Alan Stewart
 * @since 1.0
 */
@Component
@Service
public class EntityFieldCommands implements CommandMarker {

    private static final Logger LOGGER = HandlerUtils.getLogger(EntityFieldCommands.class);

    public static final String REQUEST_FACTORY_FIELD_COMMAND_PREFIX = "web requestfactory scaffold";
    
    private static final String REQUEST_FACTORY_FIELD_BOOLEAN_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field boolean";
    private static final String REQUEST_FACTORY_FIELD_DATE_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field date";
    private static final String REQUEST_FACTORY_FIELD_EMBEDDED_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field embedded";
    private static final String REQUEST_FACTORY_FIELD_ENUM_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field enum";
    private static final String REQUEST_FACTORY_FIELD_NUMBER_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field number";
    private static final String REQUEST_FACTORY_FIELD_REFERENCE_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field reference";
    private static final String REQUEST_FACTORY_FIELD_SET_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field set";
    private static final String REQUEST_FACTORY_FIELD_STRING_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field string";
    private static final String REQUEST_FACTORY_FIELD_FILE_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field file";
    private static final String REQUEST_FACTORY_FIELD_OTHER_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field other";
    private static final String FIELD_LIST_COMMAND = "field list";
    private static final String REQUEST_FACTORY_FIELD_LIST_COMMAND = REQUEST_FACTORY_FIELD_COMMAND_PREFIX + " field list";

    @Reference private MemberDetailsScanner memberDetailsScanner;
    @Reference private MetadataService metadataService;
    @Reference private ProjectOperations projectOperations;
    @Reference private TypeLocationService typeLocationService;
    @Reference private TypeManagementService typeManagementService;

    private final Set<String> legalNumericPrimitives = new HashSet<String>();

    protected void activate(final ComponentContext context) {
        legalNumericPrimitives.add(Short.class.getName());
        legalNumericPrimitives.add(Byte.class.getName());
        legalNumericPrimitives.add(Integer.class.getName());
        legalNumericPrimitives.add(Long.class.getName());
        legalNumericPrimitives.add(Float.class.getName());
        legalNumericPrimitives.add(Double.class.getName());
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_BOOLEAN_COMMAND, help = "Adds a private boolean field to an existing Java source file")
    public void addFieldBoolean(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "assertFalse", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must assert false") final boolean assertFalse,
            @CliOption(key = "assertTrue", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must assert true") final boolean assertTrue,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "value", mandatory = false, help = "Inserts an optional Spring @Value annotation with the given content") final String value,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "primitive", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to use a primitive type") final boolean primitive,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final BooleanField fieldDetails = new BooleanField(
                physicalTypeIdentifier, primitive ? JavaType.BOOLEAN_PRIMITIVE
                        : JavaType.BOOLEAN_OBJECT, fieldName);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        fieldDetails.setAssertFalse(assertFalse);
        fieldDetails.setAssertTrue(assertTrue);
        if (column != null) {
            fieldDetails.setColumn(column);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }
        if (value != null) {
            fieldDetails.setValue(value);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier,
                null,
                exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_DATE_COMMAND, help = "Adds a private date field to an existing Java source file")
    public void addFieldDateJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, optionContext = "java-date", help = "The Java type of the entity") final JavaType fieldType,
            @CliOption(key = "persistenceType", mandatory = false, help = "The type of persistent storage to be used") final DateFieldPersistenceType persistenceType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "future", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be in the future") final boolean future,
            @CliOption(key = "past", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be in the past") final boolean past,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "value", mandatory = false, help = "Inserts an optional Spring @Value annotation with the given content") final String value,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "dateFormat", mandatory = false, unspecifiedDefaultValue = "MEDIUM", specifiedDefaultValue = "MEDIUM", help = "Indicates the style of the date format (ignored if dateTimeFormatPattern is specified)") final DateTime dateFormat,
            @CliOption(key = "timeFormat", mandatory = false, unspecifiedDefaultValue = "NONE", specifiedDefaultValue = "NONE", help = "Indicates the style of the time format (ignored if dateTimeFormatPattern is specified)") final DateTime timeFormat,
            @CliOption(key = "dateTimeFormatPattern", mandatory = false, help = "Indicates a DateTime format pattern such as yyyy-MM-dd hh:mm:ss a") final String pattern,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final DateField fieldDetails = new DateField(physicalTypeIdentifier,
                fieldType, fieldName);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        fieldDetails.setFuture(future);
        fieldDetails.setPast(past);
        if (JdkJavaType.isDateField(fieldType)) {
            fieldDetails
                    .setPersistenceType(persistenceType != null ? persistenceType
                            : DateFieldPersistenceType.JPA_TIMESTAMP);
        }
        if (column != null) {
            fieldDetails.setColumn(column);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }
        if (dateFormat != null) {
            fieldDetails.setDateFormat(dateFormat);
        }
        if (timeFormat != null) {
            fieldDetails.setTimeFormat(timeFormat);
        }
        if (pattern != null) {
            fieldDetails.setPattern(pattern);
        }
        if (value != null) {
            fieldDetails.setValue(value);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier,
                null,
                exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_EMBEDDED_COMMAND, help = "Adds a private @Embedded field to an existing Java source file ")
    public void addFieldEmbeddedJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, optionContext = "project", help = "The Java type of the @Embeddable class") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the @Entity class to receive this field") final JavaType typeName,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        // Check if the field type is a JPA @Embeddable class
        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(fieldType);
        Validate.notNull(
                cid,
                "The specified target '--type' does not exist or can not be found. Please create this type first.");
        Validate.notNull(cid.getAnnotation(EMBEDDABLE),
                "The field embedded command is only applicable to JPA @Embeddable field types.");

        // Check if the requested entity is a JPA @Entity
        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final PhysicalTypeMetadata targetTypeMetadata = (PhysicalTypeMetadata) metadataService
                .get(physicalTypeIdentifier);
        Validate.notNull(
                targetTypeMetadata,
                "The specified target '--class' does not exist or can not be found. Please create this type first.");
        final PhysicalTypeDetails targetPtd = targetTypeMetadata
                .getMemberHoldingTypeDetails();
        Validate.isInstanceOf(MemberHoldingTypeDetails.class, targetPtd);

        final ClassOrInterfaceTypeDetails targetTypeCid = (ClassOrInterfaceTypeDetails) targetPtd;
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(this.getClass().getName(), targetTypeCid);
        Validate.isTrue(
                memberDetails.getAnnotation(ENTITY) != null
                        || memberDetails.getAnnotation(PERSISTENT) != null,
                "The field embedded command is only applicable to JPA @Entity or Spring Data @Persistent target types.");

        final EmbeddedField fieldDetails = new EmbeddedField(
                physicalTypeIdentifier, fieldType, fieldName);

        insertField(fieldDetails, permitReservedWords, false,
                null, exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_ENUM_COMMAND, help = "Adds a private enum field to an existing Java source file")
    public void addFieldEnum(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The enum type of this field") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "enumType", mandatory = false, help = "The fetch semantics at a JPA level") final EnumType enumType,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "default", mandatory = false, help = "Enum value to use in the initializer") final String defaultValue,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(cid, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();
        final EnumField fieldDetails = new EnumField(physicalTypeIdentifier,
                fieldType, fieldName);
        if (column != null) {
            fieldDetails.setColumn(column);
        }
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (enumType != null) {
            fieldDetails.setEnumType(enumType);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }

        String initializer = null;
        if (defaultValue != null) {
            initializer = fieldType.getSimpleTypeName() + "." + defaultValue;
        }

        insertField(fieldDetails, permitReservedWords, transientModifier,
                initializer,
                exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_NUMBER_COMMAND, help = "Adds a private numeric field to an existing Java source file")
    public void addFieldNumber(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, optionContext = "java-number", help = "The Java type of the entity") JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "decimalMin", mandatory = false, help = "The BigDecimal string-based representation of the minimum value") final String decimalMin,
            @CliOption(key = "decimalMax", mandatory = false, help = "The BigDecimal string based representation of the maximum value") final String decimalMax,
            @CliOption(key = "digitsInteger", mandatory = false, help = "Maximum number of integral digits accepted for this number") final Integer digitsInteger,
            @CliOption(key = "digitsFraction", mandatory = false, help = "Maximum number of fractional digits accepted for this number") final Integer digitsFraction,
            @CliOption(key = "min", mandatory = false, help = "The minimum value") final Long min,
            @CliOption(key = "max", mandatory = false, help = "The maximum value") final Long max,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "value", mandatory = false, help = "Inserts an optional Spring @Value annotation with the given content") final String value,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "primitive", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to use a primitive type if possible") final boolean primitive,
            @CliOption(key = "unique", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether to mark the field with a unique constraint") final boolean unique,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText,
            @CliOption(key = "units", mandatory = false, help = "Units of the numerical value, available in form editors") final String units) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        if (primitive
                && legalNumericPrimitives.contains(fieldType
                        .getFullyQualifiedTypeName())) {
            fieldType = new JavaType(fieldType.getFullyQualifiedTypeName(), 0,
                    DataType.PRIMITIVE, null, null);
        }
        final NumericField fieldDetails = new NumericField(
                physicalTypeIdentifier, fieldType, fieldName);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (decimalMin != null) {
            fieldDetails.setDecimalMin(decimalMin);
        }
        if (decimalMax != null) {
            fieldDetails.setDecimalMax(decimalMax);
        }
        if (digitsInteger != null) {
            fieldDetails.setDigitsInteger(digitsInteger);
        }
        if (digitsFraction != null) {
            fieldDetails.setDigitsFraction(digitsFraction);
        }
        if (min != null) {
            fieldDetails.setMin(min);
        }
        if (max != null) {
            fieldDetails.setMax(max);
        }
        if (column != null) {
            fieldDetails.setColumn(column);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }
        if (unique) {
            fieldDetails.setUnique(true);
        }
        if (value != null) {
            fieldDetails.setValue(value);
        }

        Validate.isTrue(
                fieldDetails.isDigitsSetCorrectly(),
                "Must specify both --digitsInteger and --digitsFractional for @Digits to be added");

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        if (units != null) {
            AnnotationMetadataBuilder builder = new AnnotationMetadataBuilder(
                    UNITS);
            builder.addStringAttribute("value", units);
            annotations.add(builder);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null,
                exclude, readOnly, invisible, uneditable, helpText, annotations);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_REFERENCE_COMMAND, help = "Adds a private reference field to an existing Java source file (eg the 'many' side of a many-to-one)")
    public void addFieldReferenceJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, optionContext = "project", help = "The Java type of the entity to reference") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "joinColumnName", mandatory = false, help = "The JPA @JoinColumn name") final String joinColumnName,
            @CliOption(key = "referencedColumnName", mandatory = false, help = "The JPA @JoinColumn referencedColumnName") final String referencedColumnName,
            @CliOption(key = "cardinality", mandatory = false, unspecifiedDefaultValue = "MANY_TO_ONE", specifiedDefaultValue = "MANY_TO_ONE", help = "The relationship cardinality at a JPA level") final Cardinality cardinality,
            @CliOption(key = "fetch", mandatory = false, help = "The fetch semantics at a JPA level") final Fetch fetch,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "unowned", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as an unowned") final boolean unowned,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(fieldType);
        Validate.notNull(
                cid,
                "The specified target '--type' does not exist or can not be found. Please create this type first.");

        // Check if the requested entity is a JPA @Entity
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(this.getClass().getName(), cid);
        final AnnotationMetadata entityAnnotation = memberDetails
                .getAnnotation(ENTITY);
        final AnnotationMetadata persistentAnnotation = memberDetails
                .getAnnotation(PERSISTENT);
        Validate.isTrue(
                entityAnnotation != null || persistentAnnotation != null,
                "The field reference command is only applicable to JPA @Entity or Spring Data @Persistent target types.");

        Validate.isTrue(cardinality == Cardinality.MANY_TO_ONE
                || cardinality == Cardinality.ONE_TO_ONE,
                "Cardinality must be MANY_TO_ONE or ONE_TO_ONE for the field reference command");

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final ReferenceField fieldDetails = new ReferenceField(
                physicalTypeIdentifier, fieldType, fieldName, cardinality);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (joinColumnName != null) {
            fieldDetails.setJoinColumnName(joinColumnName);
        }
        if (referencedColumnName != null) {
            Validate.notNull(joinColumnName,
                    "@JoinColumn name is required if specifying a referencedColumnName");
            fieldDetails.setReferencedColumnName(referencedColumnName);
        }
        if (fetch != null) {
            fieldDetails.setFetch(fetch);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        if (unowned) {
            annotations.add(new AnnotationMetadataBuilder(UNOWNED));
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null,
                exclude, readOnly, invisible, uneditable, helpText, annotations);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_SET_COMMAND, help = "Adds a private Set field to an existing Java source file (eg the 'one' side of a many-to-one)")
    public void addFieldSetJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The entity which will be contained within the Set") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "mappedBy", mandatory = false, help = "The field name on the referenced type which owns the relationship") final JavaSymbolName mappedBy,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "sizeMin", mandatory = false, help = "The minimum number of elements in the collection") final Integer sizeMin,
            @CliOption(key = "sizeMax", mandatory = false, help = "The maximum number of elements in the collection") final Integer sizeMax,
            @CliOption(key = "cardinality", mandatory = false, unspecifiedDefaultValue = "MANY_TO_MANY", specifiedDefaultValue = "MANY_TO_MANY", help = "The relationship cardinality at a JPA level") Cardinality cardinality,
            @CliOption(key = "fetch", mandatory = false, help = "The fetch semantics at a JPA level") final Fetch fetch,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(fieldType);
        Validate.notNull(
                cid,
                "The specified target '--type' does not exist or can not be found. Please create this type first.");

        // Check if the requested entity is a JPA @Entity
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(this.getClass().getName(), cid);
        final AnnotationMetadata entityAnnotation = memberDetails
                .getAnnotation(ENTITY);
        final AnnotationMetadata persistentAnnotation = memberDetails
                .getAnnotation(PERSISTENT);

        if (entityAnnotation != null) {
            Validate.isTrue(cardinality == Cardinality.ONE_TO_MANY
                    || cardinality == Cardinality.MANY_TO_MANY,
                    "Cardinality must be ONE_TO_MANY or MANY_TO_MANY for the field set command");
        }
        else if (cid.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) {
            cardinality = null;
        }
        else if (persistentAnnotation != null) {
            // Yes, we can deal with that
        }
        else {
            throw new IllegalStateException(
                    "The field set command is only applicable to enum, JPA @Entity or Spring Data @Persistence elements");
        }

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final SetField fieldDetails = new SetField(physicalTypeIdentifier,
                new JavaType(SET.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                        null, Arrays.asList(fieldType)), fieldName, fieldType,
                cardinality);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (sizeMin != null) {
            fieldDetails.setSizeMin(sizeMin);
        }
        if (sizeMax != null) {
            fieldDetails.setSizeMax(sizeMax);
        }
        if (mappedBy != null) {
            fieldDetails.setMappedBy(mappedBy);
        }
        if (fetch != null) {
            fieldDetails.setFetch(fetch);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null,
                exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_STRING_COMMAND, help = "Adds a private string field to an existing Java source file")
    public void addFieldString(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "decimalMin", mandatory = false, help = "The BigDecimal string-based representation of the minimum value") final String decimalMin,
            @CliOption(key = "decimalMax", mandatory = false, help = "The BigDecimal string based representation of the maximum value") final String decimalMax,
            @CliOption(key = "sizeMin", mandatory = false, help = "The minimum string length") final Integer sizeMin,
            @CliOption(key = "sizeMax", mandatory = false, help = "The maximum string length") final Integer sizeMax,
            @CliOption(key = "regexp", mandatory = false, help = "The required regular expression pattern") final String regexp,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "value", mandatory = false, help = "Inserts an optional Spring @Value annotation with the given content") final String value,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "unique", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether to mark the field with a unique constraint") final boolean unique,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText,
            @CliOption(key = "password", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that a password widget is to be used in form editors") final boolean password,
            @CliOption(key = "textArea", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that a multi-line text widget is to be used in form editors") final boolean textArea) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final StringField fieldDetails = new StringField(
                physicalTypeIdentifier, fieldName);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (decimalMin != null) {
            fieldDetails.setDecimalMin(decimalMin);
        }
        if (decimalMax != null) {
            fieldDetails.setDecimalMax(decimalMax);
        }
        if (sizeMin != null) {
            fieldDetails.setSizeMin(sizeMin);
        }
        if (sizeMax != null) {
            fieldDetails.setSizeMax(sizeMax);
        }
        if (regexp != null) {
            fieldDetails.setRegexp(regexp.replace("\\", "\\\\"));
        }
        if (column != null) {
            fieldDetails.setColumn(column);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }
        if (unique) {
            fieldDetails.setUnique(true);
        }
        if (value != null) {
            fieldDetails.setValue(value);
        }

        if (password && textArea) {
            LOGGER.severe("Multi-line password fields are not supported");
        }

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);
        if (textArea) {
            annotations.add(new AnnotationMetadataBuilder(TEXT_AREA));
        } else if (password) {
            annotations.add(new AnnotationMetadataBuilder(PASSWORD));
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null,
                exclude, readOnly, invisible, uneditable, helpText, annotations);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_FILE_COMMAND, help = "Adds a byte array field for storing uploaded file contents (JSF-scaffolded UIs only)")
    public void addFileUploadField(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the file upload field to add") final JavaSymbolName fieldName,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "contentType", mandatory = true, help = "The content type of the file") final UploadedFileContentType contentType,
            @CliOption(key = "autoUpload", mandatory = false, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false", help = "Whether the file is uploaded automatically when selected") final boolean autoUpload,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(cid, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = cid.getDeclaredByMetadataId();
        final UploadedFileField fieldDetails = new UploadedFileField(
                physicalTypeIdentifier, fieldName, contentType);
        fieldDetails.setAutoUpload(autoUpload);
        fieldDetails.setNotNull(notNull);
        if (column != null) {
            fieldDetails.setColumn(column);
        }

        insertField(fieldDetails, permitReservedWords, false, null, exclude,
                readOnly, invisible, uneditable, helpText);
    }

    private void insertField(final FieldDetails fieldDetails,
            final boolean permitReservedWords, final boolean transientModifier,
            final String initializer,
            final boolean exclude, final boolean readOnly,
            final boolean invisible, final boolean uneditable,
            final String helpText) {

        final List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
        fieldDetails.decorateAnnotationsList(annotations);

        insertField(fieldDetails, permitReservedWords, transientModifier,
                initializer,
                exclude, readOnly, invisible, uneditable, helpText,
                annotations);
    }

    private void insertField(final FieldDetails fieldDetails,
            final boolean permitReservedWords, final boolean transientModifier,
            String initializer,
            final boolean exclude, final boolean readOnly,
            final boolean invisible, final boolean uneditable,
            final String helpText,
            final List<AnnotationMetadataBuilder> annotations) {
        if (!permitReservedWords) {
            ReservedWords.verifyReservedWordsNotPresent(fieldDetails
                    .getFieldName());
            if (fieldDetails.getColumn() != null) {
                ReservedWords.verifyReservedWordsNotPresent(fieldDetails
                        .getColumn());
            }
        }

        if (exclude) {
            annotations.add(new AnnotationMetadataBuilder(EXCLUDE));
        }
        if (readOnly) {
            annotations.add(new AnnotationMetadataBuilder(READ_ONLY));
        }
        if (invisible) {
            annotations.add(new AnnotationMetadataBuilder(INVISIBLE));
        }
        if (uneditable) {
            annotations.add(new AnnotationMetadataBuilder(UNEDITABLE));
        }
        if (helpText != null) {
            AnnotationMetadataBuilder builder = new AnnotationMetadataBuilder(
                    HELP_TEXT);
            builder.addStringAttribute("value", helpText);
            annotations.add(builder);
        }

        if (fieldDetails instanceof CollectionField) {
            final CollectionField collectionField = (CollectionField) fieldDetails;
            initializer = "new " + collectionField.getInitializer() + "()";
        }
        else if (fieldDetails instanceof DateField
                && fieldDetails.getFieldName().getSymbolName()
                        .equals("created")) {
            initializer = "new Date()";
        }
        int modifier = Modifier.PRIVATE;
        if (transientModifier) {
            modifier += Modifier.TRANSIENT;
        }

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                fieldDetails.getPhysicalTypeIdentifier(), modifier,
                annotations, fieldDetails.getFieldName(),
                fieldDetails.getFieldType());
        fieldBuilder.setFieldInitializer(initializer);
        typeManagementService.addField(fieldBuilder.build());
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_OTHER_COMMAND, help = "Inserts a private field into the specified file")
    public void insertField(
            @CliOption(key = "fieldName", mandatory = true, help = "The name of the field") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The Java type of this field") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "column", mandatory = false, help = "The JPA @Column name") final String column,
            @CliOption(key = "value", mandatory = false, help = "Inserts an optional Spring @Value annotation with the given content") final String value,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final FieldDetails fieldDetails = new FieldDetails(
                physicalTypeIdentifier, fieldType, fieldName);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (comment != null) {
            fieldDetails.setComment(comment);
        }
        if (column != null) {
            fieldDetails.setColumn(column);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null,
                exclude, readOnly, invisible, uneditable, helpText);
    }

    @CliAvailabilityIndicator({ REQUEST_FACTORY_FIELD_OTHER_COMMAND, REQUEST_FACTORY_FIELD_NUMBER_COMMAND, REQUEST_FACTORY_FIELD_STRING_COMMAND,
            REQUEST_FACTORY_FIELD_DATE_COMMAND, REQUEST_FACTORY_FIELD_BOOLEAN_COMMAND, REQUEST_FACTORY_FIELD_ENUM_COMMAND, REQUEST_FACTORY_FIELD_EMBEDDED_COMMAND,
            REQUEST_FACTORY_FIELD_FILE_COMMAND })
    public boolean isJdkFieldManagementAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @CliAvailabilityIndicator({ REQUEST_FACTORY_FIELD_REFERENCE_COMMAND, REQUEST_FACTORY_FIELD_SET_COMMAND, FIELD_LIST_COMMAND, REQUEST_FACTORY_FIELD_LIST_COMMAND })
    public boolean isJpaFieldManagementAvailable() {
        // In a separate method in case we decide to check for JPA registration
        // in the future
        return projectOperations.isFocusedProjectAvailable();
    }

    @CliCommand(value = FIELD_LIST_COMMAND, help = "Adds a private List field to an existing Java source file (eg the 'one' side of a many-to-one)")
    public void addFieldListJpa(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The entity which will be contained within the Set") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "mappedBy", mandatory = false, help = "The field name on the referenced type which owns the relationship") final JavaSymbolName mappedBy,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "sizeMin", mandatory = false, help = "The minimum number of elements in the collection") final Integer sizeMin,
            @CliOption(key = "sizeMax", mandatory = false, help = "The maximum number of elements in the collection") final Integer sizeMax,
            @CliOption(key = "cardinality", mandatory = false, unspecifiedDefaultValue = "MANY_TO_MANY", specifiedDefaultValue = "MANY_TO_MANY", help = "The relationship cardinality at a JPA level") Cardinality cardinality,
            @CliOption(key = "fetch", mandatory = false, help = "The fetch semantics at a JPA level") final Fetch fetch,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords) {
        addFieldList(fieldName, fieldType, typeName, mappedBy, notNull, nullRequired, sizeMin, sizeMax, cardinality, fetch, comment, transientModifier, permitReservedWords, false, false, false, false, null);
    }

    @CliCommand(value = REQUEST_FACTORY_FIELD_LIST_COMMAND, help = "Adds a private List field to an existing Java source file (eg the 'one' side of a many-to-one)")
    public void addFieldListJpaGwt(
            @CliOption(key = { "", "fieldName" }, mandatory = true, help = "The name of the field to add") final JavaSymbolName fieldName,
            @CliOption(key = "type", mandatory = true, help = "The entity which will be contained within the Set") final JavaType fieldType,
            @CliOption(key = "class", mandatory = false, unspecifiedDefaultValue = "*", optionContext = "update,project", help = "The name of the class to receive this field") final JavaType typeName,
            @CliOption(key = "mappedBy", mandatory = false, help = "The field name on the referenced type which owns the relationship") final JavaSymbolName mappedBy,
            @CliOption(key = "notNull", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value cannot be null") final boolean notNull,
            @CliOption(key = "nullRequired", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Whether this value must be null") final boolean nullRequired,
            @CliOption(key = "sizeMin", mandatory = false, help = "The minimum number of elements in the collection") final Integer sizeMin,
            @CliOption(key = "sizeMax", mandatory = false, help = "The maximum number of elements in the collection") final Integer sizeMax,
            @CliOption(key = "cardinality", mandatory = false, unspecifiedDefaultValue = "MANY_TO_MANY", specifiedDefaultValue = "MANY_TO_MANY", help = "The relationship cardinality at a JPA level") Cardinality cardinality,
            @CliOption(key = "fetch", mandatory = false, help = "The fetch semantics at a JPA level") final Fetch fetch,
            @CliOption(key = "comment", mandatory = false, help = "An optional comment for JavaDocs") final String comment,
            @CliOption(key = "transient", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates to mark the field as transient") final boolean transientModifier,
            @CliOption(key = "permitReservedWords", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates whether reserved words are ignored by Roo") final boolean permitReservedWords,
            @CliOption(key = "exclude", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be excluded from the RequestFactory proxy") final boolean exclude,
            @CliOption(key = "readOnly", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is to be read only in the RequestFactory proxy") final boolean readOnly,
            @CliOption(key = "invisible", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicates that the field is not to be included in views of the proxy") final boolean invisible,
            @CliOption(key = "uneditable", mandatory = false, unspecifiedDefaultValue = "false", specifiedDefaultValue = "true", help = "Indicated that the field is not to be included when editing the proxy") final boolean uneditable,
            @CliOption(key = "helpText", mandatory = false, help = "Help text available in form editors") final String helpText) {
        addFieldList(fieldName, fieldType, typeName, mappedBy, notNull, nullRequired, sizeMin, sizeMax, cardinality, fetch, comment, transientModifier, permitReservedWords, exclude, readOnly, invisible, uneditable, helpText);
    }

    private void addFieldList(JavaSymbolName fieldName, JavaType fieldType, JavaType typeName, JavaSymbolName mappedBy,
            boolean notNull, boolean nullRequired, Integer sizeMin, Integer sizeMax, Cardinality cardinality, Fetch fetch,
            String comment, boolean transientModifier, boolean permitReservedWords, final boolean exclude, final boolean readOnly,
            final boolean invisible, final boolean uneditable, final String helpText) {

        final ClassOrInterfaceTypeDetails cid = typeLocationService
                .getTypeDetails(fieldType);
        Validate.notNull(
                cid,
                "The specified target '--type' does not exist or can not be found. Please create this type first.");

        // Check if the requested entity is a JPA @Entity
        final MemberDetails memberDetails = memberDetailsScanner
                .getMemberDetails(this.getClass().getName(), cid);
        final AnnotationMetadata entityAnnotation = memberDetails
                .getAnnotation(ENTITY);
        final AnnotationMetadata persistentAnnotation = memberDetails
                .getAnnotation(PERSISTENT);

        if (entityAnnotation != null) {
            Validate.isTrue(cardinality == Cardinality.ONE_TO_MANY
                    || cardinality == Cardinality.MANY_TO_MANY,
                    "Cardinality must be ONE_TO_MANY or MANY_TO_MANY for the field set command");
        }
        else if (cid.getPhysicalTypeCategory() == PhysicalTypeCategory.ENUMERATION) {
            cardinality = null;
        }
        else if (persistentAnnotation != null) {
            // Yes, we can deal with that
        }
        else {
            throw new IllegalStateException(
                    "The field list command is only applicable to enum, JPA @Entity or Spring Data @Persistence elements");
        }

        final ClassOrInterfaceTypeDetails javaTypeDetails = typeLocationService
                .getTypeDetails(typeName);
        Validate.notNull(javaTypeDetails, "The type specified, '" + typeName
                + "'doesn't exist");

        final String physicalTypeIdentifier = javaTypeDetails
                .getDeclaredByMetadataId();
        final ListField fieldDetails = new ListField(physicalTypeIdentifier,
                new JavaType(LIST.getFullyQualifiedTypeName(), 0, DataType.TYPE,
                        null, Arrays.asList(fieldType)), fieldName, fieldType,
                cardinality);
        fieldDetails.setNotNull(notNull);
        fieldDetails.setNullRequired(nullRequired);
        if (sizeMin != null) {
            fieldDetails.setSizeMin(sizeMin);
        }
        if (sizeMax != null) {
            fieldDetails.setSizeMax(sizeMax);
        }
        if (mappedBy != null) {
            fieldDetails.setMappedBy(mappedBy);
        }
        if (fetch != null) {
            fieldDetails.setFetch(fetch);
        }
        if (comment != null) {
            fieldDetails.setComment(comment);
        }

        insertField(fieldDetails, permitReservedWords, transientModifier, null, exclude, readOnly, invisible, uneditable, helpText);
    }
}