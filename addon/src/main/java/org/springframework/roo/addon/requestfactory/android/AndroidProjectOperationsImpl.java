package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ANDROID_ACTIVITY;
import static org.springframework.roo.addon.requestfactory.android.AndroidJavaType.ROO_ACTIVITY;
import static org.springframework.roo.addon.requestfactory.android.AndroidPaths.LAYOUT_PATH;
import static org.springframework.roo.addon.requestfactory.android.AndroidPaths.SEP;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.jpa.JpaOperations;
import org.springframework.roo.addon.requestfactory.android.types.Dimension;
import org.springframework.roo.addon.requestfactory.android.types.Orientation;
import org.springframework.roo.classpath.TypeLocationService;
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

    public static DocumentBuilder newDocumentBuilder() {
        try {
            return FACTORY.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Reference TypeLocationService typeLocationService;
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
    public void layoutLinear(final String name, final Dimension height,
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
            final boolean main) {
        if (!StringUtils.isEmpty(layout)) {
            final String layoutPath = pathResolver.getFocusedIdentifier(
                    Path.ROOT, LAYOUT_PATH + SEP + layout + XML_EXTENSION);
            if (!fileManager.exists(layoutPath)) {
                LOGGER.info("Layout '" + layout + "' does not exist");
                layoutLinear(layout, Dimension.FILL_PARENT, Dimension
                        .FILL_PARENT, Orientation.VERTICAL);
            }
        }

        final List<AnnotationMetadataBuilder> annotationBuilders = new ArrayList<AnnotationMetadataBuilder>();
        final AnnotationMetadataBuilder activityAnnotationBuilder = new AnnotationMetadataBuilder(ROO_ACTIVITY);
        activityAnnotationBuilder.addStringAttribute("value", layout);
        annotationBuilders.add(activityAnnotationBuilder);

        jpaOperations.newEntity(name, false, ANDROID_ACTIVITY,
                annotationBuilders);

        androidTypeService.addActvity(
                projectOperations.getFocusedModuleName(),
                name.getFullyQualifiedTypeName(), main);
    }

    @Override
    public void view(final JavaType type, final JavaType view,
            final String identifier, final JavaSymbolName fieldName,
            final Dimension height, final Dimension width) {

    }

    @Override
    public void resourceString(final JavaType type, final String name,
            final JavaSymbolName fieldName, final String value,
            final Dimension height, final Dimension width) {

    }
}
