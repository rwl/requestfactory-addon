package __TOP_LEVEL_PACKAGE__.client.scaffold.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

/**
 * A place in the app that deals with lists of {@link EntityProxy}.
 */
public class ProxyListPlace extends Place {

    /**
     * Tokenizer.
     */
    @Prefix("l")
    public static class Tokenizer implements PlaceTokenizer<ProxyListPlace> {
        private static final String SEPARATOR = "!";
        private final RequestFactory requests;

        public Tokenizer(RequestFactory requests) {
            this.requests = requests;
        }

        public ProxyListPlace getPlace(String token) {
            String parts[] = token.split(SEPARATOR);
            if (parts.length > 1) {
                return new ProxyListPlace(requests.getProxyClass(parts[0]), parts[1]);
            } else {
                return new ProxyListPlace(requests.getProxyClass(parts[0]), null);
            }
        }

        public String getToken(ProxyListPlace place) {
            String token = requests.getHistoryToken(place.getProxyClass());
            if (place.getParentId() != null) {
                token += SEPARATOR + place.getParentId();
            }
            return token;
        }
    }

    private final Class<? extends EntityProxy> proxyType;
    private final String parentId;

    public ProxyListPlace(Class<? extends EntityProxy> proxyType, String parentId) {
        this.proxyType = proxyType;
        this.parentId = parentId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProxyListPlace)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        ProxyListPlace other = (ProxyListPlace) obj;
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        return proxyType.equals(other.proxyType);
    }

    public Class<? extends EntityProxy> getProxyClass() {
        return proxyType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + proxyType.hashCode();
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ProxyListPlace [proxyType=" + proxyType + ", parentId=" + parentId + "]";
    }

    public String getParentId() {
        return parentId;
    }
}
