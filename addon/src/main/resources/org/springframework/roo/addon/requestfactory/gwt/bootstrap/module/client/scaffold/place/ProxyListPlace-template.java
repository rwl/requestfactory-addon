package __TOP_LEVEL_PACKAGE__.__SEGMENT_PACKAGE__;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

/**
 * A place in the app that deals with lists of {@link EntityProxy}.
 */
public class ProxyListPlace extends Place {

    public enum ListOperation {
        SELECT, VISUALIZE
    }

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
            final String parts[] = token.split(SEPARATOR);
            final ListOperation operation = ListOperation.valueOf(parts[1]);
            final String parentId = (parts.length == 3) ? parts[2] : null;
            return new ProxyListPlace(requests.getProxyClass(parts[0]),
                    operation, parentId);
        }

        public String getToken(ProxyListPlace place) {
            String token = requests.getHistoryToken(place.getProxyClass());
            token += SEPARATOR + place.getOperation();
            if (place.getParentId() != null) {
                token += SEPARATOR + place.getParentId();
            }
            return token;
        }
    }

    private final Class<? extends EntityProxy> proxyType;
    private final String parentId;
    private final ListOperation operation;

    public ProxyListPlace(final Class<? extends EntityProxy> proxyType,
            final String parentId) {
        this(proxyType, ListOperation.SELECT, parentId);
    }

    public ProxyListPlace(final Class<? extends EntityProxy> proxyType,
            final ListOperation operation, final String parentId) {
        this.proxyType = proxyType;
        this.parentId = parentId;
        this.operation = operation;
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
        if (operation != other.operation) {
            return false;
        }
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

    public ListOperation getOperation() {
        return operation;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + proxyType.hashCode();
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ProxyListPlace [operation=" + operation + ", proxyType=" + proxyType + ", parentId=" + parentId + "]";
    }

    public String getParentId() {
        return parentId;
    }
}
