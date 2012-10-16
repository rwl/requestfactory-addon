package org.springframework.roo.addon.gwt.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.roo.addon.gwt.request.RequestFactoryRequestMetadata;


/**
 * Unit test for {@link RequestFactoryRequestMetadata}
 * 
 * @author Andrew Swan
 * @since 1.2.0
 */
public class GwtRequestMetadataTest {

    private static final String CONTENTS = "contents";
    private static final String MID_1 = "MID:x#y";
    private static final String MID_2 = "MID:x#z";

    @Test
    public void testInstanceDoesNotEqualInstanceOfOtherClass() {
        // Set up
        final RequestFactoryRequestMetadata instance = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);

        // Invoke and check
        assertFalse(instance.equals(new Object()));
    }

    @Test
    public void testInstanceDoesNotEqualNull() {
        // Set up
        final RequestFactoryRequestMetadata instance = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);

        // Invoke and check
        assertFalse(instance.equals(null));
    }

    @Test
    public void testInstanceEqualsItself() {
        // Set up
        final RequestFactoryRequestMetadata instance = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);

        // Invoke and check
        assertEquals(instance, instance);
    }

    @Test
    public void testInstancesWithDifferentContentsAreNotEqual() {
        // Set up
        final RequestFactoryRequestMetadata instance1 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);
        final RequestFactoryRequestMetadata instance2 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS + "x");

        // Invoke and check
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void testInstancesWithDifferentContentsHaveDifferentHashCodes() {
        // Set up
        final RequestFactoryRequestMetadata instance1 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);
        final RequestFactoryRequestMetadata instance2 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS + "x");

        // Invoke and check
        assertTrue(instance1.hashCode() != instance2.hashCode());
    }

    @Test
    public void testInstancesWithSameContentsAreEqual() {
        // Set up
        final RequestFactoryRequestMetadata instance1 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);
        final RequestFactoryRequestMetadata instance2 = new RequestFactoryRequestMetadata(MID_2,
                CONTENTS);

        // Invoke and check
        assertEquals(instance1, instance2);
        assertEquals(instance2, instance1);
    }

    @Test
    public void testInstancesWithSameContentsHaveSameHashCodes() {
        // Set up
        final RequestFactoryRequestMetadata instance1 = new RequestFactoryRequestMetadata(MID_1,
                CONTENTS);
        final RequestFactoryRequestMetadata instance2 = new RequestFactoryRequestMetadata(MID_2,
                CONTENTS);

        // Invoke and check
        assertTrue(instance1.hashCode() == instance2.hashCode());
    }
}
