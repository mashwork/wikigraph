package com.mashwork.wikipedia.ParseXML.query.JSON;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used to get information from json. All the  raw/preprocessed tweet information are stored in json.
 */
public class ParseJSON
{
	BufferedReader BR;
	JSONParser parser;
	String JSON = null;
	String originalTwit = null;
	String nouns = null;
	public ParseJSON(String fileDir)
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
	}
	
	public boolean hasNext()
	{
		try
		{
			JSON = BR.readLine();
			if(JSON.length()==0)
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
			JSON = JSON.substring(57,JSON.length());//System.out.println(JSON);
			parse();
			return true;
		}
		
	}
	
	//extract the tweet from json
	public void parse()
	{
		StringBuilder SB = new StringBuilder();
	    try {
	        Object obj = parser.parse(JSON);
	        JSONObject jsonObject =  (JSONObject) obj;
	        originalTwit = (String)jsonObject.get("text");
	        //System.out.println(originalTwit);
//	        JSONObject mashwork = (JSONObject)jsonObject.get("mashwork");
//	        JSONArray nameEntities = (JSONArray)mashwork.get("name_entities");
//	        JSONArray nouns = (JSONArray)mashwork.get("nouns");
//
//	        Iterator<?> it;
//	        if(nameEntities!=null)
//	        {
//		        it = nameEntities.iterator();
//		        while(it.hasNext())
//		        {
//		        	SB.append((String)it.next()+" ");
//		        }
//	        }
//		        if(nouns!=null)
//		        {
//		        it = nouns.iterator();
//		        while(it.hasNext())
//		        {
//		        	SB.append((String)it.next()+" ");
//		        }
//	        }
	        
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    nouns = SB.toString();
	}
	
	public String getNextQuery()
	{
		return nouns;
	}
	public String getNextTwit()
	{
		return originalTwit;
	}
}
