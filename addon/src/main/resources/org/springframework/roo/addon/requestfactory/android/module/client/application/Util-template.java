package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.web.bindery.event.shared.SimpleEventBus;
import com.google.web.bindery.requestfactory.shared.RequestFactory;
import com.google.web.bindery.requestfactory.vm.RequestFactorySource;

public class Util {

    /**
     * Tag for logging.
     */
    private static final String TAG = "Util";

    /**
     * Key for auth cookie name in shared preferences.
     */
    public static final String AUTH_COOKIE = "authCookie";

    /**
     * Key for shared preferences.
     */
    private static final String SHARED_PREFS = Setup.APP_NAME
            .toUpperCase(Locale.ENGLISH) + "_PREFS";

    /**
     * URL suffix for the RequestFactory servlet.
     */
    public static final String RF_METHOD = "/gwtRequest";

    /**
     * Cache containing the base URL for a given context.
     */
    private static final Map<Context, String> URL_MAP = 
            new HashMap<Context, String>();

    /**
     * Returns the (debug or production) URL associated with the
     * registration service.
     */
    public static String getBaseUrl(final Context context) {
        String url = URL_MAP.get(context);
        if (url == null) {
            // if a debug_url raw resource exists, use its contents
            // as the url
            url = getDebugUrl(context);
            // otherwise, use the production url
            if (url == null) {
                url = Setup.PROD_URL;
            }
            URL_MAP.put(context, url);
        }
        return url;
    }

    /**
     * Creates and returns an initialized {@link RequestFactory} of the
     * given type.
     */
    public static <T extends RequestFactory> T getRequestFactory(
            final Context context, final Class<T> factoryClass) {
        final T requestFactory = RequestFactorySource.create(factoryClass);

        final SharedPreferences prefs = getSharedPreferences(context);
        final String authCookie = prefs.getString(Util.AUTH_COOKIE, null);

        final String uriString = Util.getBaseUrl(context) + RF_METHOD;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            Log.w(TAG, "Bad URI: " + uriString, e);
            return null;
        }
        requestFactory.initialize(new SimpleEventBus(),
                new AndroidRequestTransport(uri, authCookie));

        return requestFactory;
    }

    /**
     * Helper method to get a SharedPreferences instance.
     */
    public static SharedPreferences getSharedPreferences(
            final Context context) {
        return context.getSharedPreferences(SHARED_PREFS, 0);
    }

    /**
     * Returns a debug url, or null. To set the url, create a file
     * {@code assets/debugging_prefs.properties} with a line of the form
     * 'url=http:/<ip address>:<port>'. A numeric IP address may be required
     * in situations where the device or emulator will not be able to
     * resolve the hostname for the dev mode server.
     */
    private static String getDebugUrl(final Context context) {
        BufferedReader reader = null;
        String url = null;
        try {
            final AssetManager assetManager = context.getAssets();
            final InputStream is = assetManager.open(
                    "debugging_prefs.properties");
            reader = new BufferedReader(new InputStreamReader(is));
            while (true) {
                String s = reader.readLine();
                if (s == null) {
                    break;
                }
                if (s.startsWith("url=")) {
                    url = s.substring(4).trim();
                    break;
                }
            }
        } catch (final FileNotFoundException e) {
            // O.K., we will use the production server
            return null;
        } catch (final Exception e) {
            Log.w(TAG, "Got exception " + e);
            Log.w(TAG, Log.getStackTraceString(e));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.w(TAG, "Got exception " + e);
                    Log.w(TAG, Log.getStackTraceString(e));
                }
            }
        }

        return url;
    }

    private Util() {
    }
}
