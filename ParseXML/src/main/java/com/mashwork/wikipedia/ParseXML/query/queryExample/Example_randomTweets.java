package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.io.FileWriter;
import java.io.IOException;

import com.mashwork.wikipedia.ParseXML.query.LuceneQuery;
import com.mashwork.wikipedia.ParseXML.query.JSON.ParseJSON;

//code used for simple analysis. Try to find the best parameter. Deprecated
@Deprecated
public class Example_randomTweets
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

		//ParseJSON twits = new ParseJSON("/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21.log");
		//String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21_TSB_D1_wholeTweet_estimate";
		ParseJSON twits = new ParseJSON("/Users/Ricky/mashwork/wikiXmlParser/crawledXML/randomTweets/twitter_20130801-11.log");
		String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/" +
				"randomTweets/twitter_20130801-11_top30_tagged_sum_0.8_p0.5_dynamic0.8.txt";
		
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
		int end = 99125;				//272956
		
		while(twits.hasNext())	
		{
			//if(i++% (end/100) != 0) continue;
			String twit = twits.getNextTwit();
			
			//System.out.println("Original twit:"+twit);
			String query = twits.getNextQuery();
			
			String result = null;
			
			try
			{
				result = luceneQuery.labelEstimate(twit,30);
			}catch(Exception e)
			{
				
			}
			//result = luceneQuery.search(twit,3);
			//result = luceneQuery.LuceneEstimate(twit);
			
			if(result !=null)
			{
				FW.write("Original twit:"+twit+"\n");
				FW.write("Query: "+twit+"\n");
				FW.write(result);
				FW.write("\n\n");
			}
			
			if(i>=end) break;
		}
		FW.close();
	}
}
