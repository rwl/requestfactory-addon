package org.springframework.roo.addon.requestfactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;
import org.springframework.roo.addon.requestfactory.RequestFactoryPath;
import org.springframework.roo.addon.requestfactory.gwt.bootstrap.GwtBootstrapPaths;


/**
 * Unit test of the {@link RequestFactoryPath} enum.
 *
 * @author Andrew Swan
 * @since 1.2.0
 */
public class GwtPathTest {

    @Test
    public void testPackageNameForWebPath() {
        assertEquals("", GwtBootstrapPaths.WEB.packageName(null));
    }

    @Test
    public void testSegmentNamesAreNonNull() {
        for (final RequestFactoryPath requestFactoryPath : GwtBootstrapPaths.ALL_PATHS) {
            assertNotNull("Null segment name for " + requestFactoryPath,
                    requestFactoryPath.getSegmentName());
        }
    }

    @Test
    public void testSegmentNamesAreUnique() {
        final Collection<String> segmentNames = new HashSet<String>();
        for (final RequestFactoryPath requestFactoryPath : GwtBootstrapPaths.ALL_PATHS) {
            final String segmentName = requestFactoryPath.getSegmentName();
            assertTrue("Duplicate segment name '" + segmentName + "'",
                    segmentNames.add(segmentName));
        }
    }

    @Test
    public void testSegmentPackageForWebPath() {
        assertEquals("", GwtBootstrapPaths.WEB.segmentPackage());
    }
}
