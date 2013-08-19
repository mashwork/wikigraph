package com.mashwork.wikipedia.ParallelXML;

import java.net.URLDecoder;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * some basic tools for filtering names and other stuff.
 */
public class Filter
{
	/**
	 * @param input title name of a page
	 * @return	the title name without "\n"
	 */
	static public String processTitle(String input)
	{
		String[] temp = input.split("\n");
		return temp[0];
	}
	
	/**
	 * @param link	the original link format in wikidump
	 * @return	converted link name
	 * For example, "happy_new_year" will become "Happy new year"
	 */
	static public String toNormalLink(String link)
	{
		if(link==null || link.length()<=0) return null;
    	link =  link.substring(0,1).toUpperCase()+link.substring(1,link.length());
    	try{
    		link = URLDecoder.decode(link,"UTF-8");
    	}catch(Exception e)
    	{
    		//System.out.print("URLDecoder Error! ");
    		//System.out.println(link);
    	}
		return link.replace('_',' ');
	}
	
	/**
	 * @param link link name
	 * @return whether this link should be filtered.
	 */
	static public boolean toBeFiltered(String link)
	{
		if(link=="") return true;
		if(link==null) return true;
		if(link.contains("File:") || link.contains("Special:") || 
				link.contains("User:") || (link.length()>0 && link.charAt(0)=='#'))
			return true;
		else
			return false;
	}
	
	static public boolean isAnchorLink(String link)
	{
		if(link.contains("#"))
			return true;
		else
			return false;
	}
	
	static public boolean isCategoryLink(String value)
	{
		if(value.contains("Category:"))
			return true;
		else
			return false;
	}
	
	/**
	 * if a title is "Game of Thrones", it will return "gameofthrones" and "GOT"
	 */
	public static String[] getAliasAbbreviation(String title)
	{
		if(title==null) return null;
		String[] subTitles = title.split("#");
		if(subTitles[0]==null) return null;
		String[] words = subTitles[0].split("\\(");
		words = words[0].split(" ");
		//words = words[0].split("((\\W|\\d).*?)");
		String[] result = {"",""};
		int count = 0;
		for(String word:words)
		{
			//System.out.println(word);
			if(word!=null && !word.equals(""))
			{
				if(Character.isLetter(word.charAt(0)))
				{
					result[0] = result[0]+word;
					result[1] = result[1]+word.charAt(0);
					count++;
				}
			}
		}
		if(result[0].length() < 20)
		{
			result[0] = result[0].toLowerCase();
		}
		else 
		{
			result[0] = "";
		}

		if(count>1 && count<=6)
		{
			result[1] = result[1].toLowerCase();
		}
		else 
		{
			result[0] = "";
		}
		return result;
	}
	
	/**
	 * if the table of content level is larger than 6, just skip
	 */
	public static boolean need2Skip(String element)
	{
		int level = Integer.parseInt(element.substring(1,element.length()));
		if(level > 6)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
