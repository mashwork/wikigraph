package com.mashwork.wikipedia.ParseXML.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.mashwork.wikipedia.ParallelXML.ParallelLucene.WikiAnalyzer;

public class LuceneQuery
{
	IndexSearcher indexSearcher;
	QueryParser titleParser;
	QueryParser textParser;
	MultiFieldQueryParser queryParser;
	
	public LuceneQuery(String luceneIndexDir)
	{
		try
		{
			indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory
					.open(new File(luceneIndexDir))));
		}catch(IOException e)
		{
			System.out.println("Can not load lucene index!");
			e.printStackTrace();
		}
		
		//Analyzer wikiAnalyzer = new WikiAnalyzer();
//		this.titleParser = new QueryParser(Version.LUCENE_36, "title",
//				new WikiAnalyzer());		
//		this.textParser = new QueryParser(Version.LUCENE_36, "text",
//				new WikiAnalyzer());
		InputStream stopWordDir = LuceneQuery.class.getClassLoader().getResourceAsStream("stopwords");
		Set<String> stopWords = null;
		try
		{
			stopWords = getStopWords(stopWordDir);
		}catch(IOException e)
		{
			System.out.println("Read Stop Words Error!");
			e.printStackTrace();
		}
		
		String[] fields = new String[] { "title", "text" ,"alias", "abbreviation"};
		HashMap<String,Float> boosts = new HashMap<String,Float>();
		boosts.put("title", (float) 10.0);
		boosts.put("text", (float) 0.0);
		boosts.put("alias", (float) 0.0);
		boosts.put("abbreviation", (float) 0.0);
		this.queryParser = new MultiFieldQueryParser(
			Version.LUCENE_36,
		    fields, 
		    new WikiAnalyzer(Version.LUCENE_36,stopWords),
		    boosts
		);
	}
	
	public Set<String> getStopWords(InputStream stopWordStream) throws IOException
	{
		InputStreamReader Sr = new InputStreamReader(stopWordStream);
		BufferedReader Br = new BufferedReader(Sr);
		Set<String> stopWords = new HashSet<String>();
		String word = null;
		while((word = Br.readLine())!=null)
		{	
			stopWords.add(word);
		}
		return stopWords;
	}
	
	public String search(String queryString, int numToShow)
	{
		long startTime = System.currentTimeMillis();
		StringBuilder SB = new StringBuilder();
		System.out.println("Now search for query: "+queryString);
		
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
			//e.printStackTrace();
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, numToShow);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}
		
		SB.append("Totally "+results.totalHits+" matches found based on text.\n");
		SB.append("Showing the top "+numToShow+" results:\n");
		ScoreDoc[] hits = results.scoreDocs;
		
		SB.append("No.	Score		Title\n");
		int i = 1;
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
			SB.append(i++ +".	"+hit.score+ "	"+doc.get("title")+"		"+ doc.get("alias")+"\n");
		}
		long endTime = System.currentTimeMillis();
		SB.append("Totally "+(endTime-startTime) +" ms used to search.\n");
		return SB.toString();
	}
	
	public String[] getTop3(String queryString)
	{
		//long startTime = System.currentTimeMillis();
		String[] result = new String[3];
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, 3);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}

		ScoreDoc[] hits = results.scoreDocs;

		int i = 1;
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
			result[i++-1] = doc.get("title");
			//System.out.println(result[i-1]);
		}
		//long endTime = System.currentTimeMillis();
		return result;
	}
}
