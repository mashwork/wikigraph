package com.mashwork.wikipedia.ParallelXML.ParallelLucene;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * modified from lu liu's analyzer. Do some preprocessing before add lucene index
 */
public final class WikiAnalyzer extends StopwordAnalyzerBase {

	  /** Default maximum allowed token length */
	  public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

	  private int maxTokenLength = DEFAULT_MAX_TOKEN_LENGTH;

	  public static final Set<?> STOP_WORDS_SET = StopAnalyzer.ENGLISH_STOP_WORDS_SET; 

	  public WikiAnalyzer(Version matchVersion, Set<?> stopWords) {
	    super(matchVersion, stopWords);
	  }

	    public WikiAnalyzer() {
	        this(Version.LUCENE_36, STOP_WORDS_SET);
	    }
	    
	  public WikiAnalyzer(Version matchVersion) {
	    this(matchVersion, STOP_WORDS_SET);
	  }

	  public WikiAnalyzer(Version matchVersion, File stopwords) throws IOException {
	    this(matchVersion, WordlistLoader.getWordSet(IOUtils.getDecodingReader(stopwords,
	        IOUtils.CHARSET_UTF_8), matchVersion));
	  }

	  public WikiAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
	    this(matchVersion, WordlistLoader.getWordSet(stopwords, matchVersion));
	  }

	  public void setMaxTokenLength(int length) {
	    maxTokenLength = length;
	  }
	    
	  public int getMaxTokenLength() {
	    return maxTokenLength;
	  }

	  @Override
	  protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
	    final StandardTokenizer src = new StandardTokenizer(matchVersion, reader);
	    src.setMaxTokenLength(maxTokenLength);
	    TokenStream tok = new StandardFilter(matchVersion, src);
	    tok = new LowerCaseFilter(matchVersion, tok);
	    tok = new StopFilter(matchVersion, tok, stopwords);
	    //tok = new PorterStemFilter(tok);
	    return new TokenStreamComponents(src, tok) {
	      @Override
	      protected boolean reset(final Reader reader) throws IOException {
	        src.setMaxTokenLength(WikiAnalyzer.this.maxTokenLength);
	        return super.reset(reader);
	      }
	    };
	  }
	}
