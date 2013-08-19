package com.mashwork.wikipedia.ParseXML.query.JSON;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//this class is used for a simple statistics analyze. Do not use this class
@Deprecated
public class Statistics
{
	BufferedReader BR;
	JSONParser parser;
	String JSON = null;
	String originalTwit = null;
	String nouns = null;
	int total = 0;
	int hit = 0;
	String query;
	public Statistics(String fileDir, String query)
	{
		try
		{
			this.BR = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir)));
		}catch(IOException e)
		{
			System.out.println("Can not open Json file!");
			e.printStackTrace();
		}
		this.parser = new JSONParser();
		this.query = query;
	}
	
	public void run()
	{
		while(hasNext())
		{
		}
		System.out.println("Total hit: "+hit);
		System.out.println("Total twits: "+total);
		System.out.println("Percentage: "+(double)hit/total*100);
	}
	
	public boolean hasNext()
	{
		try
		{
			JSON = BR.readLine();
		}catch(Exception e)
		{
			System.out.println("Read Json file error!");
			e.printStackTrace();
		}
		
		if(JSON == null)
		{
			try
			{
				BR.close();
			}catch(Exception e)
			{
				System.out.println("Can not close buffered reader!");
				e.printStackTrace();
			}
			return false;
		}
		else
		{
			check();
			return true;
		}
		
	}
	
	public void check()
	{
		total++;
	    try {
	        Object obj = parser.parse(JSON);
	        JSONObject jsonObject =  (JSONObject) obj;
	        JSONArray tags = (JSONArray)jsonObject.get("tags");
	        //String queryWords = (String)jsonObject.get("query");
	        Iterator<?> it;
	        if(tags!=null)
	        {
//	        	if(queryWords.contains("gameofthrones") || queryWords.contains("got") || queryWords.contains("GOT"))
//	        	{
//	        		hit++;
//	        		return;
//	        	}
		        it = tags.iterator();
		        while(it.hasNext())
		        {
		        	String tag = (String) it.next();
		        	if(tag!=null && tag.contains(query))
		        	{
		        		hit++;
		        		break;
		        	}
		        	//System.out.println(tag);
		        }
	        }
	        
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args)
	{
		String fileDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/twitter_20130609-21_tagged_alias.log";
		String query = "Game of Thrones";
		Statistics count = new Statistics(fileDir,query);
		count.run();
	}
	
}
