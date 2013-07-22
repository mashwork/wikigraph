package com.mashwork.wikipedia.ParseXML.query.queryExample;

import com.mashwork.wikipedia.ParseXML.query.LuceneQuery;
import com.mashwork.wikipedia.ParseXML.query.JSON.ParseJSON;

public class Example_realLucene
{
	public static void main(String[] args)
	{
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Poriton/MergedLuceneIndex_aliasAbbre";
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB_Merged";
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB-1";
		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/test/LuceneDB-5";
		LuceneQuery luceneQuery = new LuceneQuery(LuceneIndexDir);
		
		//String query = "As in anthropology, European witchcraft is seen by historians as an ideology for explaining misfortune.";
		String query = "I like how Lord Tywin refers to Loras's homosexuality as an affliction.  Game Of Thrones @HBO";
		String query2 = "screen size SUMSANG Galaxy";
		String query3 = "Obama Columbia";
		String query4 = "arya stark";
		String query5 = "List of A Song of Ice and Fire characters";
		String result = luceneQuery.search(query,10);
		System.out.println(result);
		result = luceneQuery.search(query2,10);
		System.out.println(result);
		result = luceneQuery.search(query3,10);
		System.out.println(result);
		result = luceneQuery.search(query4,10);
		System.out.println(result);
		result = luceneQuery.search(query5,10);
		System.out.println(result);
		
//		ParseJSON twits = new ParseJSON("/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21.log");
//		int i = 1;
//		while(twits.hasNext())	
//		{
//			String twit = twits.getNextTwit();
//			System.out.println("Original twit:"+twit);
//			String query = twits.getNextQuery();
//			String result = luceneQuery.search(query,10);
//			System.out.println(result);
//			if(i++>=10) break;
//		}
		
	}
}
