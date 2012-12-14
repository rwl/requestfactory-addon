package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.web.bindery.requestfactory.shared.RequestTransport;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;


public class AndroidRequestTransport implements RequestTransport {

    private final URI uri;

    private final String cookie;

    /**
     * Constructs an AndroidRequestTransport instance.
     *
     * @param uri the URI for the RequestFactory service
     * @param cookie the cookie used for authentication
     */
    public AndroidRequestTransport(final URI uri, final String cookie) {
        this.uri = uri;
        this.cookie = cookie;
    }

    public void send(final String payload,
            final TransportReceiver receiver) {
        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost();
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        post.setHeader("Cookie", cookie);

        post.setURI(uri);
        Throwable ex;
        try {
            post.setEntity(new StringEntity(payload, "UTF-8"));
            final HttpResponse response = client.execute(post);
            if (200 == response.getStatusLine().getStatusCode()) {
                String contents = readStreamAsString(response.getEntity()
                        .getContent());
                receiver.onTransportSuccess(contents);
            } else {
                receiver.onTransportFailure(new ServerFailure(response
                        .getStatusLine().getReasonPhrase()));
            }
            return;
        } catch (final UnsupportedEncodingException e) {
            ex = e;
        } catch (final ClientProtocolException e) {
            ex = e;
        } catch (final IOException e) {
            ex = e;
        }
        receiver.onTransportFailure(new ServerFailure(ex.getMessage()));
    }

    private String readStreamAsString(final InputStream in) {
        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];
            int count;
            do {
                count = in.read(buffer);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            } while (count >= 0);
            return out.toString("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException("Unsupported encoding.", e);
        } catch (final IOException e) {
            return null;
        } finally {
            try {
                in.close();
            } catch (final IOException ignored) {
            }
        }
    }
}
