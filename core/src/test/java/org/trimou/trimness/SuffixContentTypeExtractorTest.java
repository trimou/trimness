package org.trimou.trimness;

import org.junit.Assert;
import org.junit.Test;
import org.trimou.trimness.template.SuffixContentTypeExtractor;
import org.trimou.trimness.util.Strings;

public class SuffixContentTypeExtractorTest {

    @Test
    public void testSupportedSuffixes() {
        SuffixContentTypeExtractor extractor = new SuffixContentTypeExtractor();
        Assert.assertEquals(Strings.APP_JSON, extractor.extract("foo.json", null));
        Assert.assertEquals(Strings.TEXT_HTML, extractor.extract("foo.html", null));
        Assert.assertEquals(Strings.TEXT_HTML, extractor.extract("/some/path/bar.htm", null));
        Assert.assertEquals(Strings.APP_JAVASCRIPT, extractor.extract("alha.js", null));
        Assert.assertEquals(Strings.TEXT_CSS, extractor.extract("?basic.css", null));
        Assert.assertEquals(Strings.TEXT_PLAIN, extractor.extract("hello.txt", null));
    }

}
