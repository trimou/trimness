package org.trimou.trimness.util;

import static org.junit.Assert.assertTrue;
import static org.trimou.trimness.util.Strings.matchesLinkPattern;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class StringsTest {

    @Test
    public void testLinkPattern() {
        assertTrue(matchesLinkPattern("foo-bar"));
        assertTrue(matchesLinkPattern("foo-Bar_1"));
        assertFalse(matchesLinkPattern(" 1"));
        assertFalse(matchesLinkPattern("čedar"));
        assertFalse(matchesLinkPattern(""));
        assertFalse(matchesLinkPattern("too-long-link-name-must-have-at-least-60-chars-to-fail-but-its-enough-for-real-scenarios"));
    }

}
