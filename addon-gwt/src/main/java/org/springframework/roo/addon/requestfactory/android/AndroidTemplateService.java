package org.springframework.roo.addon.requestfactory.android;

import java.util.List;
import java.util.Map;

import org.springframework.roo.addon.requestfactory.RequestFactoryProxyProperty;
import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateDataHolder;
import org.springframework.roo.addon.requestfactory.RequestFactoryType;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaSymbolName;

/**
 * Interface for {@link AndroidTemplateServiceImpl}.
 */
public interface AndroidTemplateService {

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            String moduleName);

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            RequestFactoryType type, String moduleName);



    String buildViewXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);
}
