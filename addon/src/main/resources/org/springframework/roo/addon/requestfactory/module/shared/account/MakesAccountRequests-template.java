package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

/**
 * Implemented by {@link com.google.web.bindery.requestfactory.shared.RequestFactory}s
 * that vend account requests.
 */
public interface MakesAccountRequests {

    /**
     * Return a request selector.
     */
    OpenIdAccountServiceRequest accountServiceRequest();

}
