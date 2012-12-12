package org.springframework.roo.addon.requestfactory.android;

import hapax.TemplateDataDictionary;

import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;

/**
 * Interface for {@link AndroidTemplateServiceImpl}.
 */
public interface AndroidTemplateService {

    String buildViewXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);

    TemplateDataDictionary buildMirrorDataDictionary(AndroidType type,
            ClassOrInterfaceTypeDetails mirroredType,
            ClassOrInterfaceTypeDetails proxy, Map<RequestFactoryType,
            JavaType> mirrorTypeMap, Map<JavaSymbolName,
            RequestFactoryProxyProperty> clientSideTypeMap,
            String moduleName);

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            ClassOrInterfaceTypeDetails mirroredType, Map<JavaSymbolName,
            RequestFactoryProxyProperty> clientSideTypeMap, String moduleName);

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            RequestFactoryType type, String moduleName);

    TemplateDataDictionary buildDictionary(RequestFactoryType type,
            String moduleName);
}
