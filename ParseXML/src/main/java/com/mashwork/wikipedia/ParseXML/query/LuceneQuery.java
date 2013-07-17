package com.mashwork.wikipedia.ParseXML.query;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
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
		titleParser = new QueryParser(Version.LUCENE_36, "title",
				new WikiAnalyzer());		
		textParser = new QueryParser(Version.LUCENE_36, "text",
				new WikiAnalyzer());
	}
	
	public String search(String queryString, int numToShow)
	{
		StringBuilder SB = new StringBuilder();
		System.out.println("Now search for query: "+queryString);
		
		Query query = null;
		try
		{
			query = textParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			e.printStackTrace();
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
			SB.append(i++ +".	"+hit.score+ "	"+doc.get("title")+"\n");
		}
		return SB.toString();
	}
}
