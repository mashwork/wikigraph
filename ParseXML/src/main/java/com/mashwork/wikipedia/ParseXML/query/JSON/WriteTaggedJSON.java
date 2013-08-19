package com.mashwork.wikipedia.ParseXML.query.JSON;

import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.mashwork.wikipedia.ParseXML.query.LuceneQuery;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used to tag the tweets and save the query results in json.
 */
public class WriteTaggedJSON
{
	FileWriter Fw;
	LuceneQuery luceneQuery;
	ParseJSON twits;
	
	public WriteTaggedJSON(String LuceneIndexDir,String outputDir,String JSONDir,String graphDbDir)
	{
		try
		{
			this.Fw = new FileWriter(outputDir);
		}catch(IOException e)
		{
			System.out.println("Can not open write file!");
			e.printStackTrace();
		}
		
		this.luceneQuery = new LuceneQuery(LuceneIndexDir,graphDbDir);
		this.twits = new ParseJSON(JSONDir);
	}
	
	public void parse()
	{
		while(twits.hasNext())	
		{
			String twit = twits.getNextTwit();
			String query = twits.getNextQuery();
			if(query!=null && !query.equals(""))
			{
				String[] result = luceneQuery.getTop3(query);
				if(result != null)
				{
					write2File(twit,query,result);
				}
			}
		}
		
		try
		{
			Fw.flush();
			Fw.close();
		}catch(Exception e)
		{
			System.out.println("Close file Error!");
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void write2File(String twit, String query, String[] results)
	{
		JSONObject JSON = new JSONObject();

		JSONArray tags = new JSONArray();
		for(int i = 0; i < results.length;i++)
		{
			tags.add(results[i]);
		}
		JSON.put("twit",twit);
		JSON.put("query",query);
		JSON.put("tags",tags);
		
		try
		{
			Fw.write(JSON.toString()+"\n");
		}catch(Exception e)
		{
			System.out.println("Write Json error!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		if (args.length < 4) {
		      System.out.println("USAGE: ExtractLinks <JSONDIR> <GraphDBDir> <LuceneIndeDir> <outputDir>");
		      System.exit(255);
		      }
		
//		String JSONDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/jiali_tweets";
//		//String JSONDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21.log";
//		String graphDbDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
//		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/" +
//				"GOT_D3_Poriton/MergedLuceneIndex_aliasAbbre";
//		String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/jiali_tweets_tagged";
		
		String JSONDir = args[0];
		String graphDbDir = args[1];
		String LuceneIndexDir = args[2];
		String outputDir = args[3];
		
		
		WriteTaggedJSON writeTaggedJSON = new WriteTaggedJSON(LuceneIndexDir,outputDir,JSONDir,graphDbDir);
		writeTaggedJSON.parse();
		
	}
}
