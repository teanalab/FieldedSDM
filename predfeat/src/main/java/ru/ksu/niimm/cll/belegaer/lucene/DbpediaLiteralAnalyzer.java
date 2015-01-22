package ru.ksu.niimm.cll.belegaer.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.miscellaneous.LengthFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Nikita Zhiltsov
 */
public class DbpediaLiteralAnalyzer extends Analyzer {
    private final int MIN_TOKEN_LENGTH;
    private static final int MAX_TOKEN_LENGTH = 50;
    private final boolean DO_STEMMING;
    /**
     * An unmodifiable set containing some common English words that are usually not
     * useful for searching.
     */
    private static final CharArraySet STOP_WORDS_SET = CharArraySet.copy(Version.LUCENE_48, TrecStopWordSet.stopWords);

    public DbpediaLiteralAnalyzer(int MIN_TOKEN_LENGTH) {
        this.MIN_TOKEN_LENGTH = MIN_TOKEN_LENGTH;
        this.DO_STEMMING = true;
    }

    public DbpediaLiteralAnalyzer(int MIN_TOKEN_LENGTH, boolean doStemming) {
        this.MIN_TOKEN_LENGTH = MIN_TOKEN_LENGTH;
        this.DO_STEMMING = doStemming;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
        final StandardTokenizer src = new StandardTokenizer(Version.LUCENE_48, reader);
        src.setMaxTokenLength(MAX_TOKEN_LENGTH);
        TokenStream tok = new LowerCaseFilter(Version.LUCENE_48, src);
        tok = new StopFilter(Version.LUCENE_48, tok, STOP_WORDS_SET);
        tok = new LengthFilter(Version.LUCENE_48, tok, MIN_TOKEN_LENGTH, MAX_TOKEN_LENGTH);
        tok = new EnglishPossessiveFilter(Version.LUCENE_48, tok);
        tok = new RetainOnlyEnglishTokenFilter(Version.LUCENE_48, tok);
        if (DO_STEMMING) {
            tok = new KStemFilter(tok);
        }
        return new TokenStreamComponents(src, tok) {
            @Override
            protected void setReader(final Reader reader) throws IOException {
                super.setReader(reader);
            }
        };
    }
}
