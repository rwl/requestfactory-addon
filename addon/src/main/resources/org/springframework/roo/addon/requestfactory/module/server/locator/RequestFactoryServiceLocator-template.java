package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import javax.servlet.http.HttpServletRequest;

import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;
import com.google.web.bindery.requestfactory.shared.ServiceLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class RequestFactoryServiceLocator implements ServiceLocator {

    HttpServletRequest request = RequestFactoryServlet.getThreadLocalRequest();
    ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(request.getSession().getServletContext());

    @Override
    public Object getInstance(Class<?> clazz) {
        return context.getBean(clazz);
    }
}
