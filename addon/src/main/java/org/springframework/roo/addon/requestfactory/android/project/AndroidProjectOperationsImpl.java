package org.springframework.roo.addon.requestfactory.android.project;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_ACTIVITY;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_FRAGMENT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_SUPPORT_FRAGMENT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_FRAGMENT;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_STRING;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_SYSTEM_SERVICE;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_VIEW;
import static org.springframework.roo.addon.requestfactory.android.AndroidPaths.LAYOUT_PATH;
import static org.springframework.roo.addon.requestfactory.android.AndroidPaths.SEP;
import static org.springframework.roo.addon.requestfactory.android.AndroidPaths.VALUES_PATH;
import static org.springframework.roo.model.JavaType.STRING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.jpa.JpaOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.android.AndroidTypeService;
import org.springframework.roo.addon.requestfactory.android.types.Dimension;
import org.springframework.roo.addon.requestfactory.android.types.Orientation;
import org.springframework.roo.addon.requestfactory.android.types.Permission;
import org.springframework.roo.addon.requestfactory.android.types.SystemService;
import org.springframework.roo.addon.requestfactory.annotations.android.RooActivity;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.TypeManagementService;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.logging.HandlerUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component
@Service
public class AndroidProjectOperationsImpl implements AndroidProjectOperations {

    private static final Logger LOGGER = HandlerUtils.getLogger(AndroidProjectOperationsImpl.class);

    private static final DocumentBuilderFactory FACTORY;

    static {
        FACTORY = DocumentBuilderFactory.newInstance();
        FACTORY.setNamespaceAware(true);
    }

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String XML_EXTENSION = ".xml";
    private static final String WIDGET_PACKAGE = "android.widget";
    private static final String ID_PREFIX = "@+id/";
    private static final String STRINGS = "strings";

    public static DocumentBuilder newDocumentBuilder() {
        try {
            return FACTORY.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Reference TypeLocationService typeLocationService;
    @Reference TypeManagementService typeManagementService;
    @Reference ProjectOperations projectOperations;
    @Reference JpaOperations jpaOperations;
    @Reference FileManager fileManager;
    @Reference PathResolver pathResolver;
    @Reference AndroidTypeService androidTypeService;

    @Override
    public boolean isActivityAvailable() {
        return projectOperations.isFocusedProjectAvailable();
    }

    @Override
    public boolean isViewAvailable() {
        return projectOperations.isFocusedProjectAvailable()
                && typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_ACTIVITY)
                .size() > 0;
    }

    @Override
    public void layout(final String name, final Dimension height,
            final Dimension width, final Orientation orientation) {
        final Document document = newDocumentBuilder().newDocument();

        final Element layoutElem = document.createElement("LinearLayout");
        layoutElem.setAttribute("xmlns:android", ANDROID_NS);
        layoutElem.setAttribute("android:layout_height", height.value());
        layoutElem.setAttribute("android:layout_width", width.value());
        layoutElem.setAttribute("android:orientation", orientation.value());
        document.appendChild(layoutElem);

        final String layoutPath = pathResolver.getFocusedIdentifier(
                Path.ROOT, LAYOUT_PATH + SEP + name + XML_EXTENSION);
        if (fileManager.exists(layoutPath)) {
            LOGGER.severe("Layout '" + name + "' already exists");
            return;
        }
        final MutableFile file = fileManager.createFile(layoutPath);
        final OutputStream output = file.getOutputStream();
        XmlUtils.writeFormattedXml(output, document);
        try {
            output.close();
        } catch (IOException e) {
            LOGGER.severe("Error closing stream: " + e.getMessage());
            return;
        }
//        fileManager.createOrUpdateTextFileIfRequired(layoutPath,
//                XmlUtils.nodeToString(document), true);
    }

    @Override
    public void activity(final JavaType name, final String layout,
            final boolean main, final boolean noTitle,
            final boolean fullscreen) {
        if (noTitle) {
            Validate.isTrue(fullscreen == false,
                    "Options 'noTitle' and 'fullscreen' are mutex");
        }
        if (fullscreen) {
            Validate.isTrue(noTitle == false,
                    "Options 'noTitle' and 'fullscreen' are mutex");
        }
        
        if (!StringUtils.isEmpty(layout)) {
            final String layoutPath = pathResolver.getFocusedIdentifier(
                    Path.ROOT, LAYOUT_PATH + SEP + layout + XML_EXTENSION);
            if (!fileManager.exists(layoutPath)) {
                LOGGER.info("Layout '" + layout + "' does not exist");
                layout(layout, Dimension.FILL_PARENT, Dimension
                        .FILL_PARENT, Orientation.VERTICAL);
            }
        }

        final List<AnnotationMetadataBuilder> annotations =
                new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder activityAnnotationBuilder =
                new AnnotationMetadataBuilder(ROO_ACTIVITY);
        if (!StringUtils.isEmpty(layout)) {
            activityAnnotationBuilder.addStringAttribute("value", layout);
        }
        if (noTitle) {
            activityAnnotationBuilder.addBooleanAttribute(RooActivity
                    .NO_TITLE_ATTRIBUTE, noTitle);
        }
        if (fullscreen) {
            activityAnnotationBuilder.addBooleanAttribute(RooActivity
                    .FULLSCREEN_ATTRIBUTE, fullscreen);
        }
        annotations.add(activityAnnotationBuilder);

        jpaOperations.newEntity(name, false, ANDROID_ACTIVITY,
                annotations);

        androidTypeService.addActvity(
                projectOperations.getFocusedModuleName(),
                name.getFullyQualifiedTypeName(), main);
    }

    @Override
    public void view(final JavaType type, final String viewName,
            final String identifier, final JavaSymbolName fieldName,
            final Dimension height, final Dimension width) {

        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(type);
        Validate.notNull(typeDetails, "The type specified, '" + type
                + "'doesn't exist");

        final JavaType viewType = new JavaType(viewName.contains(".")
                ? viewName : WIDGET_PACKAGE + "." + viewName);

        final String layout = RequestFactoryUtils.getStringAnnotationValue(
                typeDetails, ROO_ACTIVITY, "value", "");
        if (!StringUtils.isEmpty(layout)) {
            final DocumentBuilder builder = newDocumentBuilder();
            final String layoutPath = pathResolver.getFocusedIdentifier(
                    Path.ROOT, LAYOUT_PATH + SEP + layout + XML_EXTENSION);

            InputStream inputStream = null;
            Document document = null;
            try {
                inputStream = fileManager.getInputStream(layoutPath);
                document = builder.parse(inputStream);
            }
            catch (final Exception e) {
                LOGGER.severe("Error reading layout XML: " + e.getMessage());
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }

            if (document != null) {
                final Element root = document.getDocumentElement();

                final Element viewElem = document.createElement(viewType
                        .getSimpleTypeName());
                final String id = StringUtils.isEmpty(identifier)
                        ? fieldName.getSymbolName() : identifier;
                viewElem.setAttribute("android:id", ID_PREFIX + id);
                viewElem.setAttribute("android:layout_height", height.value());
                viewElem.setAttribute("android:layout_width", width.value());
                root.appendChild(viewElem);

                fileManager.createOrUpdateTextFileIfRequired(layoutPath ,
                        XmlUtils.nodeToString(document), true);
            }
        }

        final String physicalTypeIdentifier = typeDetails
                .getDeclaredByMetadataId();

        final List<AnnotationMetadataBuilder> annotations =
                new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder annotationBuilder =
                new AnnotationMetadataBuilder(ROO_VIEW);
        if (!StringUtils.isEmpty(identifier)) {
            annotationBuilder.addStringAttribute("value", identifier);
        }
        annotations.add(annotationBuilder);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                physicalTypeIdentifier, 0, annotations, fieldName, viewType);
        typeManagementService.addField(fieldBuilder.build());
    }

    @Override
    public void resourceString(final JavaType type, final String name,
            final JavaSymbolName fieldName, final String value) {

        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(type);
        Validate.notNull(typeDetails, "The type specified, '" + type
                + "' doesn't exist");

        final DocumentBuilder builder = newDocumentBuilder();
        final String valuesPath = pathResolver.getFocusedIdentifier(
                Path.ROOT, VALUES_PATH + SEP + STRINGS + XML_EXTENSION);

        InputStream inputStream = null;
        Document document = null;
        try {
            inputStream = fileManager.getInputStream(valuesPath);
            document = builder.parse(inputStream);
        }
        catch (final Exception e) {
            LOGGER.severe("Error reading resource XML: " + e.getMessage());
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }

        if (document != null) {
            final Element root = document.getDocumentElement();


            final Element stringElem = XmlUtils.createTextElement(document,
                    "string", value);
            final String id = StringUtils.isEmpty(name)
                    ? fieldName.getSymbolName() : name;
            stringElem.setAttribute("name", id);
            root.appendChild(stringElem);

            fileManager.createOrUpdateTextFileIfRequired(valuesPath ,
                    XmlUtils.nodeToString(document), true);
        }

        final String physicalTypeIdentifier = typeDetails
                .getDeclaredByMetadataId();

        final List<AnnotationMetadataBuilder> annotations =
                new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder annotationBuilder =
                new AnnotationMetadataBuilder(ROO_STRING);
        if (!StringUtils.isEmpty(name)) {
            annotationBuilder.addStringAttribute("value", name);
        }
        annotations.add(annotationBuilder);

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                physicalTypeIdentifier, 0, annotations, fieldName, STRING);
        typeManagementService.addField(fieldBuilder.build());
    }

    @Override
    public void systemService(final JavaType type,
            JavaSymbolName fieldName, final SystemService service,
            final boolean addPermissions) {

        final ClassOrInterfaceTypeDetails typeDetails = typeLocationService
                .getTypeDetails(type);
        Validate.notNull(typeDetails, "The type specified, '" + type
                + "' doesn't exist");

        if (fieldName == null) {
            fieldName = new JavaSymbolName(StringUtils.uncapitalize(service
                    .getServiceType().getSimpleTypeName()));
        }

        final String physicalTypeIdentifier = typeDetails
                .getDeclaredByMetadataId();

        final List<AnnotationMetadataBuilder> annotations = Arrays
                .asList(new AnnotationMetadataBuilder(ROO_SYSTEM_SERVICE));

        final FieldMetadataBuilder fieldBuilder = new FieldMetadataBuilder(
                physicalTypeIdentifier, 0, annotations, fieldName,
                service.getServiceType());
        typeManagementService.addField(fieldBuilder.build());

        if (addPermissions) {
            final String moduleName = projectOperations
                    .getFocusedModuleName();
            for (Permission permission : service.getPermissions()) {
                androidTypeService.addPermission(moduleName, permission
                        .permissionName());
            }
        }
    }

    @Override
    public void permission(final Permission permission) {
        Validate.notNull(permission, "Permission type may not be null");
        final String moduleName = projectOperations.getFocusedModuleName();
        androidTypeService.addPermission(moduleName, permission
                .permissionName());
    }

    @Override
    public void fragment(final JavaType name, final String layout,
            final boolean support) {

        if (!StringUtils.isEmpty(layout)) {
            final String layoutPath = pathResolver.getFocusedIdentifier(
                    Path.ROOT, LAYOUT_PATH + SEP + layout + XML_EXTENSION);
            if (!fileManager.exists(layoutPath)) {
                LOGGER.info("Layout '" + layout + "' does not exist");
                layout(layout, Dimension.FILL_PARENT, Dimension
                        .FILL_PARENT, Orientation.VERTICAL);
            }
        }

        final List<AnnotationMetadataBuilder> annotations =
                new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder activityAnnotationBuilder =
                new AnnotationMetadataBuilder(ROO_FRAGMENT);
        if (!StringUtils.isEmpty(layout)) {
            activityAnnotationBuilder.addStringAttribute("value", layout);
        }
        annotations.add(activityAnnotationBuilder);

        jpaOperations.newEntity(name, false,
                support ? ANDROID_SUPPORT_FRAGMENT : ANDROID_FRAGMENT,
                annotations);
    }
}
