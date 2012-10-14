package org.springframework.roo.addon.requestfactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;

/**
 * Unit test of the {@link RequestFactoryPath} enum.
 * 
 * @author Andrew Swan
 * @since 1.2.0
 */
public class GwtPathTest {

    @Test
    public void testPackageNameForWebPath() {
        assertEquals("", RequestFactoryPath.WEB.packageName(null));
    }

    @Test
    public void testSegmentNamesAreNonNull() {
        for (final RequestFactoryPath requestFactoryPath : RequestFactoryPath.values()) {
            assertNotNull("Null segment name for " + requestFactoryPath,
                    requestFactoryPath.getSegmentName());
        }
    }

    @Test
    public void testSegmentNamesAreUnique() {
        final Collection<String> segmentNames = new HashSet<String>();
        for (final RequestFactoryPath requestFactoryPath : RequestFactoryPath.values()) {
            final String segmentName = requestFactoryPath.getSegmentName();
            assertTrue("Duplicate segment name '" + segmentName + "'",
                    segmentNames.add(segmentName));
        }
    }

    @Test
    public void testSegmentPackageForWebPath() {
        assertEquals("", RequestFactoryPath.WEB.segmentPackage());
    }
}
