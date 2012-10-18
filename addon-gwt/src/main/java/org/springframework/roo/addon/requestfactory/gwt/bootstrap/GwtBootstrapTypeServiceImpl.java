package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.requestfactory.RequestFactoryOperations;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeServiceImpl;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.scaffold.GwtBootstrapScaffoldMetadata;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MemberHoldingTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.project.LogicalPath;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
@Service
public class GwtBootstrapTypeServiceImpl extends RequestFactoryTypeServiceImpl implements GwtBootstrapTypeService {

    private static final String PATH = "path";

    @Reference private FileManager fileManager;

    @Override
    public void buildType(final RequestFactoryType type,
            final List<ClassOrInterfaceTypeDetails> templateTypeDetails,
            final String moduleName) {
        if (GwtBootstrapType.LIST_PLACE_RENDERER.equals(type)) {
            final Map<JavaSymbolName, List<JavaType>> watchedMethods = new HashMap<JavaSymbolName, List<JavaType>>();
            watchedMethods.put(new JavaSymbolName("render"), Collections
                    .singletonList(new JavaType(projectOperations
                            .getTopLevelPackage(moduleName)
                            .getFullyQualifiedPackageName()
                            + ".client.scaffold.place.ProxyListPlace")));
            type.setWatchedMethods(watchedMethods);
        }
        else {
            type.resolveMethodsToWatch(type);
        }

        type.resolveWatchedFieldNames(type);
        final List<ClassOrInterfaceTypeDetails> typesToBeWritten = new ArrayList<ClassOrInterfaceTypeDetails>();
        for (final ClassOrInterfaceTypeDetails templateTypeDetail : templateTypeDetails) {
            typesToBeWritten.addAll(buildType(type, templateTypeDetail,
                    getExtendsTypes(templateTypeDetail), moduleName));
        }
        requestFactoryFileManager.write(typesToBeWritten, type.isOverwriteConcrete());
    }

    public void addSourcePath(final String sourcePath, final String moduleName) {
        final String gwtXmlPath = getGwtModuleXml(moduleName);
        Validate.notBlank(gwtXmlPath, "gwt.xml could not be found for module '"
                + moduleName + "'");
        final Document gwtXmlDoc = getGwtXmlDocument(gwtXmlPath);
        final Element gwtXmlRoot = gwtXmlDoc.getDocumentElement();
        final List<Element> sourceElements = XmlUtils.findElements(
                "/module/source", gwtXmlRoot);
        if (!anyExistingSourcePathsIncludePath(sourcePath, sourceElements)) {
            final Element firstSourceElement = sourceElements.get(0);
            final Element newSourceElement = gwtXmlDoc.createElement("source");
            newSourceElement.setAttribute(PATH, sourcePath);
            gwtXmlRoot.insertBefore(newSourceElement, firstSourceElement);
            fileManager.createOrUpdateTextFileIfRequired(gwtXmlPath,
                    XmlUtils.nodeToString(gwtXmlDoc),
                    "Added source paths to gwt.xml file", true);
        }
    }

    private boolean anyExistingSourcePathsIncludePath(final String sourcePath,
            final Iterable<Element> sourceElements) {
        for (final Element sourceElement : sourceElements) {
            if (sourcePath.startsWith(sourceElement.getAttribute(PATH))) {
                return true;
            }
        }
        return false;
    }

    public String getGwtModuleXml(final String moduleName) {
        final LogicalPath logicalPath = LogicalPath.getInstance(
                Path.SRC_MAIN_JAVA, moduleName);
        final String gwtModuleXml = projectOperations.getPathResolver()
                .getRoot(logicalPath)
                + File.separatorChar
                + projectOperations.getTopLevelPackage(moduleName)
                        .getFullyQualifiedPackageName()
                        .replace('.', File.separatorChar)
                + File.separator
                + "*.gwt.xml";
        final Set<String> paths = new LinkedHashSet<String>();
        for (final FileDetails fileDetails : fileManager
                .findMatchingAntPath(gwtModuleXml)) {
            paths.add(fileDetails.getCanonicalPath());
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException(
                    "Each module must have a gwt.xml file");
        }
        if (paths.size() > 1) {
            throw new IllegalStateException(
                    "Each module can only have only gwt.xml file: "
                            + paths.size());
        }
        return paths.iterator().next();
    }

    public Document getGwtXmlDocument(final String gwtModuleCanonicalPath) {
        final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
        builder.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(final String publicId,
                    final String systemId) throws SAXException, IOException {
                if (systemId.endsWith("gwt-module.dtd")) {
                    return new InputSource(FileUtils.getInputStream(
                            GwtBootstrapScaffoldMetadata.class,
                            "templates/gwt-module.dtd"));
                }
                // Use the default behaviour
                return null;
            }
        });

        InputStream inputStream = null;
        try {
            inputStream = fileManager.getInputStream(gwtModuleCanonicalPath);
            return builder.parse(inputStream);
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public Collection<JavaPackage> getSourcePackages(final String moduleName) {
        final Document gwtXmlDoc = getGwtXmlDocument(getGwtModuleXml(moduleName));
        final Element gwtXmlRoot = gwtXmlDoc.getDocumentElement();
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final Collection<JavaPackage> sourcePackages = new HashSet<JavaPackage>();
        for (final Element sourcePathElement : XmlUtils.findElements(
                "/module/source", gwtXmlRoot)) {
            final String relativePackage = sourcePathElement.getAttribute(PATH)
                    .replace(RequestFactoryOperations.PATH_DELIMITER, ".");
            sourcePackages.add(new JavaPackage(topLevelPackage + "."
                    + relativePackage));
        }
        return sourcePackages;
    }

    public boolean isMethodReturnTypeInSourcePath(final MethodMetadata method,
            final MemberHoldingTypeDetails memberHoldingTypeDetail,
            final Iterable<JavaPackage> sourcePackages) {
        final JavaType returnType = method.getReturnType();
        final boolean inSourcePath = isTypeInAnySourcePackage(returnType,
                sourcePackages);
        if (!inSourcePath
                && !isCommonType(returnType)
                && !JavaType.VOID_PRIMITIVE.getFullyQualifiedTypeName().equals(
                        returnType.getFullyQualifiedTypeName())) {
            displayWarning("The path to type "
                    + returnType.getFullyQualifiedTypeName()
                    + " which is used in type "
                    + memberHoldingTypeDetail.getName()
                    + " by the field '"
                    + method.getMethodName().getSymbolName()
                    + "' needs to be added to the module's gwt.xml file in order to be used in a Proxy.");
            return false;
        }
        return true;
    }

    private boolean isTypeInAnySourcePackage(final JavaType type,
            final Iterable<JavaPackage> sourcePackages) {
        for (final JavaPackage sourcePackage : sourcePackages) {
            if (type.getPackage().isWithin(sourcePackage)) {
                return true; // It's a project type
            }
            if (isCollectionType(type)
                    && type.getParameters().size() == 1
                    && type.getParameters().get(0).getPackage()
                            .isWithin(sourcePackage)) {
                return true; // It's a collection of a project type
            }
        }
        return false;
    }

}
