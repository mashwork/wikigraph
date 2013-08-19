package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.io.FileWriter;
import java.io.IOException;

import com.mashwork.wikipedia.ParseXML.query.LuceneQuery;
import com.mashwork.wikipedia.ParseXML.query.JSON.ParseJSON;
//code used for simple analysis. Try to find the best parameter. Deprecated
@Deprecated
public class Example_realLucene
{
	public static void main(String[] args) throws IOException
	{
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Poriton/MergedLuceneIndex_aliasAbbre";
//		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB_New_betterAlias/LuceneDB_merged";
//		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB_Merged64";
		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/2010sTV/Lucene";
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/TSB/TSB_D1/TSB_Lucene_Merged";
		//String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/TSB/TSB_D1/TSB_Lucene_Whole_Merged";

		String graphDbDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
		
		LuceneQuery luceneQuery = new LuceneQuery(LuceneIndexDir,graphDbDir);
		
		//String query = "mother of dragons";
		//String result = luceneQuery.search(query,10);
		//String result = luceneQeury.LuceneEastimate(query);
		//System.out.println(result);
		
		
		//String query = "As in anthropology, European witchcraft is seen by historians as an ideology for explaining misfortune.";
//		String query = "I like how Lord Tywin refers to Loras's homosexuality as an affliction.";		//  Game Of Thrones @HBO
//		String query2 = "screen size SUMSANG Galaxy";
//		String query3 = "breaking bad";
//		String query4 = "Jon Snow and Ygritte";
//		String query5 = "sam merlotte needs to beat up alcide . ";	//#trueblood
//		String query6 = "James was a great talent &amp; i owe him. Quite simply without tony soprano" +
//				" there's no Walter white. Bryan cranston @ipeksiyahmanto";
//		String query7 = "The seagate harddisk is making too much noise. I'm going to return it";
//		String query8 = "Love sunshine. Miami, I'm coming.";
//		String query9 = "Fuck! Get stuck in traffic again. NY traffic sucks";
//		String result = luceneQuery.search(query,10);
//		System.out.println(result);
//		result = luceneQuery.search(query2,10);
//		System.out.println(result);
//		result = luceneQuery.search(query3,10);
//		System.out.println(result);
//		result = luceneQuery.search(query4,10);
//		System.out.println(result);
//		result = luceneQuery.search(query5,10);
//		System.out.println(result);
//		result = luceneQuery.search(query6,10);
//		System.out.println(result);
//		result = luceneQuery.search(query7,10);
//		System.out.println(result);
//		result = luceneQuery.search(query8,10);
//		System.out.println(result);
//		result = luceneQuery.search(query9,10);
//		System.out.println(result);
		
		//ParseJSON twits = new ParseJSON("/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21.log");
		//String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21_TSB_D1_wholeTweet_estimate";
		ParseJSON twits = new ParseJSON("/Users/Ricky/mashwork/wikiXmlParser/crawledXML/testFile/twitter_20130621-00.log");
		String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/testFile/" +
				"twitter_20130621-00_2010Tv_all_top30_tagged_estimated_sum0.8_p0.5_dynamic0.8_NoAbbrv.txt";
		
		FileWriter FW = null; 
		try
		{
			FW = new FileWriter(outputDir);
		}catch(IOException e)
		{
			System.out.println("Can not open file!");
			e.printStackTrace();
		}
		
		int i = 1;
		int start = 1;
		int end = 5761;				//272956
		
		while(twits.hasNext())	
		{
//			if(i++% (end/100) != 0) continue;
			String twit = twits.getNextTwit();
			System.out.println("Original twit:"+twit);
			String query = twits.getNextQuery();
			FW.write("Original twit:"+twit+"\n");
			FW.write("Query: "+twit+"\n");
			String result = null;
			
			result = luceneQuery.labelEstimate(twit,30);
			//result = luceneQuery.search(twit,3);
			//result = luceneQuery.LuceneEstimate(twit);
			
			if(result !=null)
			FW.write(result);
			
			FW.write("\n\n");
			System.out.println(result);
			if(i>=end) break;
		}
		FW.close();
	}
}
