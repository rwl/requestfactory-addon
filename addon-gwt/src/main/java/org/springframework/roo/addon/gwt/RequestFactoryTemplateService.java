package org.springframework.roo.addon.gwt;

import java.util.List;
import java.util.Map;

import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.model.JavaSymbolName;

/**
 * Interface for {@link RequestFactoryTemplateServiceImpl}.
 * 
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface RequestFactoryTemplateService {

    String buildUiXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);

    RequestFactoryTemplateDataHolder getMirrorTemplateTypeDetails(
            ClassOrInterfaceTypeDetails governorTypeDetails,
            Map<JavaSymbolName, RequestFactoryProxyProperty> clientSideTypeMap,
            String moduleName);

    List<ClassOrInterfaceTypeDetails> getStaticTemplateTypeDetails(
            RequestFactoryType type, String moduleName);
}
