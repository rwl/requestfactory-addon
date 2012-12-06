package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import __TOP_LEVEL_PACKAGE__.account.AccountAuthRequestTransport;

import java.io.IOException;
import java.net.URL;

/**
 * A servlet filter that handles basic user authentication.
 */
@Component
public class AccountAuthFilter implements Filter {

    @Autowired
    LoginUrlAuthenticationEntryPoint loginUrlAuthenticationEntryPoint;

    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    public void destroy() {
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (authenticationTrustResolver.isAnonymous(auth)) {
            URL requestUrl;
            if (request.getHeader(AccountAuthRequestTransport.REQUEST_URL) != null) {
                requestUrl = new URL(request.getHeader(AccountAuthRequestTransport.REQUEST_URL));
            } else {
                requestUrl = new URL(request.getRequestURI());
            }
            URL loginUrl = new URL(requestUrl.getProtocol(), requestUrl.getHost(), requestUrl.getPort(), "/login"/*loginUrlAuthenticationEntryPoint.getLoginFormUrl()*/);
            response.setHeader("login", loginUrl.toString());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    public void init(FilterConfig config) {
    }
}
