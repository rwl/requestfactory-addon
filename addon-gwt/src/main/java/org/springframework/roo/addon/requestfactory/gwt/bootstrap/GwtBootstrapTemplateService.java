package org.springframework.roo.addon.requestfactory.gwt.bootstrap;

import java.util.List;

import org.springframework.roo.addon.requestfactory.RequestFactoryTemplateService;
import org.springframework.roo.classpath.details.MethodMetadata;

/**
 * Interface for {@link GwtBootstrapTemplateServiceImpl}.
 *
 * @author James Tyrrell
 * @since 1.1.2
 */
public interface GwtBootstrapTemplateService extends RequestFactoryTemplateService {

    String buildUiXml(String templateContents, String destFile,
            List<MethodMetadata> proxyMethods);
}
