package __ACCOUNT_PACKAGE__;

import org.openid4java.util.HttpFetcher;

import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.inject.Provider;

/**
 * This will inject an Openid4javaFetcher instance that is using a GAE-friendly URLFetchService.
 */
public class CustomHttpCacheProvider implements Provider<HttpFetcher> {

    @Override
    public HttpFetcher get() {
        return new Openid4javaFetcher(URLFetchServiceFactory.getURLFetchService());
    }
}
