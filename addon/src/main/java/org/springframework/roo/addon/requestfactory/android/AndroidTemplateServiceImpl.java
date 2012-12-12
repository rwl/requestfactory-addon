package org.springframework.roo.addon.requestfactory.android;

import static org.springframework.roo.addon.requestfactory.RequestFactoryJavaType.ROO_REQUEST_FACTORY_PROXY;
import hapax.TemplateDataDictionary;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.springframework.roo.addon.plural.PluralMetadata;
import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.addon.requestfactory.RequestFactoryTypeService;
import org.springframework.roo.addon.requestfactory.RequestFactoryUtils;
import org.springframework.roo.addon.requestfactory.gwt.scaffold.GwtScaffoldMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.classpath.details.BeanInfoUtils;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.persistence.PersistenceMemberLocator;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.support.util.FileUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
@Service
public class AndroidTemplateServiceImpl implements AndroidTemplateService {

    private static final String TEMPLATE_DIR = "org/springframework/roo/addon/requestfactory/android/scaffold/templates/";
    
    @Reference RequestFactoryTemplateService requestFactoryTemplateService;
    @Reference PersistenceMemberLocator persistenceMemberLocator;
    @Reference RequestFactoryTypeService requestFactoryTypeService;
    @Reference ProjectOperations projectOperations;
    @Reference TypeLocationService typeLocationService;
    @Reference MetadataService metadataService;
    
    @Override
    public TemplateDataDictionary buildDictionary(final RequestFactoryType type,
            final String moduleName) {
        final Set<ClassOrInterfaceTypeDetails> proxies = typeLocationService
                .findClassesOrInterfaceDetailsWithAnnotation(ROO_REQUEST_FACTORY_PROXY);
        final String proxyModuleName = PhysicalTypeIdentifier.getPath(
                proxies.iterator().next().getDeclaredByMetadataId()).getModule();
        final TemplateDataDictionary dataDictionary = requestFactoryTemplateService
                .buildStandardDataDictionary(type, moduleName, proxyModuleName);
        
        if (type == AndroidType.LIST_ACTIVITY_PROCESSOR
                || type == AndroidType.PLURAL_PROCESSOR) {
            for (final ClassOrInterfaceTypeDetails proxy : proxies) {
                if (!RequestFactoryUtils.scaffoldProxy(proxy)) {
                    continue;
                }
                final ClassOrInterfaceTypeDetails entity = requestFactoryTypeService
                        .lookupEntityFromProxy(proxy);
                if (entity != null) {
                    final String entitySimpleName = entity.getName()
                            .getSimpleTypeName();
                    final String proxySimpleName = proxy.getName()
                            .getSimpleTypeName();

                    final TemplateDataDictionary section = dataDictionary
                            .addSection("entities");
                    section.setVariable("entitySimpleName", entitySimpleName);
                    section.setVariable("proxySimpleName", proxySimpleName);

                    section.setVariable("entityListActivity", entitySimpleName 
                            + AndroidType.PROXY_LIST_ACTIVITY.getSuffix());
                    
                    requestFactoryTemplateService.addImport(dataDictionary,
                            proxy.getName().getFullyQualifiedTypeName());


                    final String pluralMetadataKey = PluralMetadata
                            .createIdentifier(entity.getName(),
                                    PhysicalTypeIdentifier.getPath(entity
                                            .getDeclaredByMetadataId()));
                    final PluralMetadata pluralMetadata = (PluralMetadata)
                            metadataService.get(pluralMetadataKey);
                    final String plural = pluralMetadata.getPlural();

                    section.setVariable("entityPluralName", plural);
                }
            }
        }
            
        return dataDictionary;
    }

    @Override
    public TemplateDataDictionary buildMirrorDataDictionary(
            final AndroidType type, final ClassOrInterfaceTypeDetails mirroredType,
            final ClassOrInterfaceTypeDetails proxy,
            final Map<RequestFactoryType, JavaType> mirrorTypeMap,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {

        final TemplateDataDictionary dataDictionary = requestFactoryTemplateService
                .buildMirrorDataDictionary(type, mirroredType, proxy,
                        mirrorTypeMap, clientSideTypeMap, moduleName);

        final JavaType javaType = mirrorTypeMap.get(type);
        Validate.notNull(javaType);

        final JavaType entity = mirroredType.getName();
        final String entityName = entity.getFullyQualifiedTypeName();
        final JavaType idType = persistenceMemberLocator
                .getIdentifierType(entity);
        Validate.notNull(idType,
                "Identifier type is not available for entity '" + entityName
                + "'");

        for (int i = 0; i < type.getViewTemplates().size(); i++) {
            final String viewTemplate = type.getViewTemplates().get(0);
            dataDictionary.setVariable("view_name_" + i, viewTemplate/*AndroidUtils
                    .camelToLowerCase(viewTemplate)*/);
        }
        
        dataDictionary.setVariable("proxy_name", AndroidUtils
                .camelToLowerCase(proxy.getName().getSimpleTypeName()));
        
        return dataDictionary;
    }

    @Override
    public String buildViewXml(final String templateContents,
            final String destFile, final List<MethodMetadata> proxyMethods) {
        FileReader fileReader = null;
        try {
            final DocumentBuilder builder = XmlUtils.getDocumentBuilder();
            builder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(final String publicId,
                        final String systemId) throws SAXException, IOException {
                    if (systemId
                            .equals("http://dl.google.com/gwt/DTD/xhtml.ent")) {
                        return new InputSource(FileUtils.getInputStream(
                                GwtScaffoldMetadata.class,
                                "templates/xhtml.ent"));
                    }

                    // Use the default behaviour
                    return null;
                }
            });

            InputSource source = new InputSource();
            source.setCharacterStream(new StringReader(templateContents));

            final Document templateDocument = builder.parse(source);

            if (!new File(destFile).exists()) {
                return transformXml(templateDocument);
            }

            source = new InputSource();
            fileReader = new FileReader(destFile);
            source.setCharacterStream(fileReader);
            final Document existingDocument = builder.parse(source);

            // Look for the element holder denoted by the 'debugId' attribute
            // first
            Element existingHoldingElement = XmlUtils.findFirstElement(
                    "//*[@debugId='" + "boundElementHolder" + "']",
                    existingDocument.getDocumentElement());
            Element templateHoldingElement = XmlUtils.findFirstElement(
                    "//*[@debugId='" + "boundElementHolder" + "']",
                    templateDocument.getDocumentElement());

            // If holding element isn't found then the holding element is either
            // not widget based or using the old convention of 'id' so look for
            // the element holder with an 'id' attribute
            if (existingHoldingElement == null) {
                existingHoldingElement = XmlUtils.findFirstElement("//*[@id='"
                        + "boundElementHolder" + "']",
                        existingDocument.getDocumentElement());
            }
            if (templateHoldingElement == null) {
                templateHoldingElement = XmlUtils.findFirstElement("//*[@id='"
                        + "boundElementHolder" + "']",
                        templateDocument.getDocumentElement());
            }

            if (existingHoldingElement != null) {
                final Map<String, Element> templateElementMap = new LinkedHashMap<String, Element>();
                for (final Element element : XmlUtils.findElements("//*[@id]",
                        templateHoldingElement)) {
                    templateElementMap.put(element.getAttribute("id"), element);
                }

                final Map<String, Element> existingElementMap = new LinkedHashMap<String, Element>();
                for (final Element element : XmlUtils.findElements("//*[@id]",
                        existingHoldingElement)) {
                    existingElementMap.put(element.getAttribute("id"), element);
                }

                if (existingElementMap.keySet().containsAll(
                        templateElementMap.values())) {
                    return transformXml(existingDocument);
                }

                final List<Element> elementsToAdd = new ArrayList<Element>();
                for (final Map.Entry<String, Element> entry : templateElementMap
                        .entrySet()) {
                    if (!existingElementMap.keySet().contains(entry.getKey())) {
                        elementsToAdd.add(entry.getValue());
                    }
                }

                final List<Element> elementsToRemove = new ArrayList<Element>();
                for (final Map.Entry<String, Element> entry : existingElementMap
                        .entrySet()) {
                    if (!templateElementMap.keySet().contains(entry.getKey())) {
                        elementsToRemove.add(entry.getValue());
                    }
                }

                for (final Element element : elementsToAdd) {
                    final Node importedNode = existingDocument.importNode(
                            element, true);
                    existingHoldingElement.appendChild(importedNode);
                }

                for (final Element element : elementsToRemove) {
                    existingHoldingElement.removeChild(element);
                }

                if (elementsToAdd.size() > 0) {
                    final List<Element> sortedElements = new ArrayList<Element>();
                    for (final MethodMetadata method : proxyMethods) {
                        final String propertyName = StringUtils
                                .uncapitalize(BeanInfoUtils
                                        .getPropertyNameForJavaBeanMethod(
                                                method).getSymbolName());
                        final Element element = XmlUtils.findFirstElement(
                                "//*[@id='" + propertyName + "']",
                                existingHoldingElement);
                        if (element != null) {
                            sortedElements.add(element);
                        }
                    }
                    for (final Element el : sortedElements) {
                        if (el.getParentNode() != null
                                && el.getParentNode().equals(
                                        existingHoldingElement)) {
                            existingHoldingElement.removeChild(el);
                        }
                    }
                    for (final Element el : sortedElements) {
                        existingHoldingElement.appendChild(el);
                    }
                }

                return transformXml(existingDocument);
            }

            return transformXml(templateDocument);
        }
        catch (final Exception e) {
            throw new IllegalStateException(e);
        }
        finally {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private String transformXml(final Document document)
            throws TransformerException {
        final Transformer transformer = XmlUtils.createIndentingTransformer();
        final DOMSource source = new DOMSource(document);
        final StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(source, result);
        return result.getWriter().toString();
    }

    @Override
    public RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            final ClassOrInterfaceTypeDetails mirroredType,
            final Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            final String moduleName) {
        final ClassOrInterfaceTypeDetails proxy = requestFactoryTypeService
                .lookupProxyFromEntity(mirroredType);
        final ClassOrInterfaceTypeDetails request = requestFactoryTypeService
                .lookupUnmanagedRequestFromEntity(mirroredType);
        final JavaPackage topLevelPackage = projectOperations
                .getTopLevelPackage(moduleName);
        final Map<RequestFactoryType, JavaType> mirrorTypeMap = RequestFactoryUtils
                .getMirrorTypeMap(mirroredType.getName(), topLevelPackage);
        mirrorTypeMap.putAll(AndroidUtils.getMirrorTypeMap(
                mirroredType.getName(), topLevelPackage));
        mirrorTypeMap.put(RequestFactoryType.PROXY, proxy.getName());
        mirrorTypeMap.put(RequestFactoryType.REQUEST, request.getName());

        final Map<RequestFactoryType, ClassOrInterfaceTypeDetails> templateTypeDetailsMap = new LinkedHashMap<RequestFactoryType, ClassOrInterfaceTypeDetails>();
        final Map<RequestFactoryType, String[]> xmlTemplates = new LinkedHashMap<RequestFactoryType, String[]>();
        for (final AndroidType androidType : AndroidType.getAndroidMirrorTypes()) {
            if (androidType.getTemplate() == null) {
                continue;
            }
            TemplateDataDictionary dataDictionary = buildMirrorDataDictionary(
                    androidType, mirroredType, proxy, mirrorTypeMap,
                    clientSideTypeMap, moduleName);
            androidType.dynamicallyResolveFieldsToWatch(clientSideTypeMap);
            androidType.dynamicallyResolveMethodsToWatch(mirroredType.getName(),
                    clientSideTypeMap, topLevelPackage);
            templateTypeDetailsMap.put(androidType, requestFactoryTemplateService
                    .getTemplateDetails(dataDictionary, androidType.getTemplate(),
                            mirrorTypeMap.get(androidType), moduleName, TEMPLATE_DIR));

            if (androidType.isCreateViewXml()) {
                dataDictionary = buildMirrorDataDictionary(androidType,
                        mirroredType, proxy, mirrorTypeMap, clientSideTypeMap,
                        moduleName);
                final List<String> contents = new ArrayList<String>();
                for (String viewTemplate : androidType.getViewTemplates()) {
                    contents.add(requestFactoryTemplateService
                            .getTemplateContents(viewTemplate, dataDictionary,
                                    TEMPLATE_DIR));
                }
                xmlTemplates.put(androidType, contents.toArray(new String[0]));
            }
        }

        final Map<String, String> xmlMap = new LinkedHashMap<String, String>();
        final List<ClassOrInterfaceTypeDetails> typeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();

        return new RequestFactoryTemplateDataHolder(templateTypeDetailsMap, xmlTemplates,
                typeDetails, xmlMap);
    }

    @Override
    public List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            final RequestFactoryType type, final String moduleName) {
        final List<ClassOrInterfaceTypeDetails> templateTypeDetails = new ArrayList<ClassOrInterfaceTypeDetails>();
        final TemplateDataDictionary dataDictionary = requestFactoryTemplateService
                .buildDictionary(type, moduleName);
        templateTypeDetails.add(requestFactoryTemplateService
                .getTemplateDetails(dataDictionary, type.getTemplate(),
                        requestFactoryTemplateService.getDestinationJavaType(
                                type, moduleName), moduleName, TEMPLATE_DIR));
        return templateTypeDetails;
    }
}
