package __TOP_LEVEL_PACKAGE__.place;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.web.bindery.requestfactory.shared.EntityProxy;
import com.google.web.bindery.requestfactory.shared.EntityProxyId;
import com.google.web.bindery.requestfactory.shared.RequestFactory;

/**
 * A place in the app that deals with a specific {@link RequestFactory} proxy.
 */
public class ProxyPlace extends Place {

    /**
     * The things you do with a record, each of which is a different bookmarkable
     * location in the scaffold app.
     */
    public enum Operation {
        CREATE, EDIT, DETAILS, VISUALIZE
    }

    /**
     * Tokenizer.
     */
    @Prefix("r")
    public static class Tokenizer implements PlaceTokenizer<ProxyPlace> {
        private static final String SEPARATOR = "!";
        private final RequestFactory requests;

        public Tokenizer(RequestFactory requests) {
            this.requests = requests;
        }

        public ProxyPlace getPlace(String token) {
            String bits[] = token.split(SEPARATOR);
            Operation operation = Operation.valueOf(bits[1]);
            String parentId = (bits.length == 3) ? bits[2] : null;
            if (Operation.CREATE == operation) {
                return new ProxyPlace(requests.getProxyClass(bits[0]), parentId);
            }
            return new ProxyPlace(requests.getProxyId(bits[0]), operation, parentId);
        }

        public String getToken(ProxyPlace place) {
            String token;
            if (Operation.CREATE == place.getOperation()) {
                token = requests.getHistoryToken(place.getProxyClass());
            } else {
                token = requests.getHistoryToken(place.getProxyId());
            }
            token += SEPARATOR + place.getOperation();
            if (place.getParentId() != null) {
                token += SEPARATOR + place.getParentId();
            }
            return token;
        }
    }

    private final EntityProxyId<?> proxyId;
    private final Class<? extends EntityProxy> proxyClass;
    private final Operation operation;
    private final String parentId;

    public ProxyPlace(Class<? extends EntityProxy> proxyClass, String parentId) {
        this.operation = Operation.CREATE;
        this.proxyId = null;
        this.proxyClass = proxyClass;
        this.parentId = parentId;
    }

    public ProxyPlace(EntityProxyId<?> record, String parentId) {
        this(record, Operation.DETAILS, parentId);
    }

    public ProxyPlace(EntityProxyId<?> proxyId, Operation operation, String parentId) {
        this.operation = operation;
        this.proxyId = proxyId;
        this.proxyClass = null;
        this.parentId = parentId;
        assert Operation.CREATE != operation;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProxyPlace)) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        ProxyPlace other = (ProxyPlace) obj;
        if (operation != other.operation) {
            return false;
        }
        if (proxyClass == null) {
            if (other.proxyClass != null) {
                return false;
            }
        } else if (!proxyClass.equals(other.proxyClass)) {
            return false;
        }
        if (proxyId == null) {
            if (other.proxyId != null) {
                return false;
            }
        } else if (!proxyId.equals(other.proxyId)) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        return true;
    }

    public Operation getOperation() {
        return operation;
    }

    public String getParentId() {
        return this.parentId;
    }

    public Class<? extends EntityProxy> getProxyClass() {
        return proxyId != null ? proxyId.getProxyClass() : proxyClass;
    }

    /**
     * @return the proxyId, or null if the operation is {@link Operation#CREATE}
     */
    public EntityProxyId<?> getProxyId() {
        return proxyId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((operation == null) ? 0 : operation.hashCode());
        result = prime * result + ((proxyClass == null) ? 0 : proxyClass.hashCode());
        result = prime * result + ((proxyId == null) ? 0 : proxyId.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ProxyPlace [operation=" + operation + ", proxy=" + proxyId + ", proxyClass=" + proxyClass + ", parentId=" + parentId + "]";
    }
}
