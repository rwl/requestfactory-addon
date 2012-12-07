package org.springframework.roo.addon.requestfactory.gwt;

import hapax.TemplateDataDictionary;

import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.w3c.dom.Document;

/**
 * Interface for {@link GwtTemplateServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtTemplateService {

    String buildUiXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);

    TemplateDataDictionary buildDictionary(RequestFactoryType type, String moduleName);

    TemplateDataDictionary buildMirrorDataDictionary(RequestFactoryType type, ClassOrInterfaceTypeDetails mirroredType, ClassOrInterfaceTypeDetails proxy, Map<RequestFactoryType, JavaType> mirrorTypeMap, Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap, String moduleName);

    TemplateDataDictionary buildStandardDataDictionary(RequestFactoryType type, String moduleName, String proxyModuleName);

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(ClassOrInterfaceTypeDetails mirroredType, Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap, String moduleName);

    String transformXml(Document document) throws TransformerException;

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(RequestFactoryType type, String moduleName);
}
