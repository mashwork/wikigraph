package com.mashwork.wikipedia.ParseXML.SmallPortion;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used to extract related pages from a certain category page. For example, starting from 
 * "Category:2010s American television series", almost 1500 TV series pages will be found within 1 step.
 * Later we can use this pages to create a small database to analyze tweets.
 */
public class CategoryPoritonExtraction
{
	public static void Write2File(String outputDir, List<List<String>> result) 
	{
		try
		{
			FileWriter FW = new FileWriter(outputDir);
			Iterator<List<String>> ListIt = result.iterator();
			while(ListIt.hasNext())
			{
				List<String> list = ListIt.next();
				String pageName = list.get(list.size()-1);
				FW.write(getTitle(pageName)+"\n");
			}
			FW.flush();
			FW.close();
		} catch (IOException e)
		{
			System.out.println("Can not open file Error!!");
			e.printStackTrace();
		}
	}
	
	public static String getTitle(String input)
	{
		if(input==null || input.equals(""))
		{
			System.out.println("getTitle input error!");
			return null;
		}
		else
		{
			return input.split("#")[0];
		}
	}
	
	public static void main(String[] args)
	{
		if (args.length < 3) {
		      System.out.println("USAGE: ExtractLinks <DBDIR> <output-file> <initialPage>");
		      System.exit(255);
		      }
		
//		String DBDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
//		String outputDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/CategoryTitle.txt";
//		String initialPage = "Category:2010s American television series";
		
		String DBDir = args[0];
		String outputDir = args[1];
		String initialPage = args[2];
		
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		
		System.out.println("Your initial query: "+ initialPage);
		//List<List<String>> paths = wikiQuery.findShortestPaths("Sean Bean","Kit Harington",20);
		List<List<String>> categoryPaths = wikiQuery.findCategories(initialPage,1);
		Write2File(outputDir,categoryPaths);
	}
}
