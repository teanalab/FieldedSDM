package ru.ksu.niimm.cll.belegaer.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * @author Nikita Zhiltsov
 */
public class RetainOnlyEnglishTokenFilter extends FilteringTokenFilter {

    private static final Pattern ASCII_TOKEN_PATTERN = Pattern.compile("^[\\x20-\\x7E]+$");

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

    public RetainOnlyEnglishTokenFilter(Version version, TokenStream in) {
        super(version, in);
    }

    @Override
    protected boolean accept() throws IOException {
        return ASCII_TOKEN_PATTERN.matcher(termAtt.toString()).matches();
    }
}
