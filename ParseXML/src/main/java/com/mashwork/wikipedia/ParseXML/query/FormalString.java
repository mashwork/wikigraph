package com.mashwork.wikipedia.ParseXML.query;

import java.util.HashSet;

public class FormalString
{
	//static String[] preposotions = {"and", "the", "in", "an", "or", "at", "of",
			//			"a", "on", "to", "by", "for", "with", "from", "as"};
	static HashSet<String> prepositions = new HashSet<String>();
	
	static
	{
		prepositions.add("and");
		prepositions.add("the");
		prepositions.add("in");
		prepositions.add("an");
		prepositions.add("or");
		prepositions.add("at");
		prepositions.add("of");
		prepositions.add("a");
		prepositions.add("on");
		prepositions.add("to");
		prepositions.add("by");
		prepositions.add("for");
		prepositions.add("with");
		prepositions.add("from");
		prepositions.add("as");
	}
	
	public static String formalize(String input)
	{
		if(input==null || input=="") return input;
		String[] temp = input.split(" ");
		int length = temp.length;
		temp[0] = capitalize(temp[0]);
		for(int i = 1;i < length;i++)
		{
			if(!prepositions.contains(temp[i]))
			{
				temp[i] = capitalize(temp[i]);
			}
		}
		
		String result = temp[0];
		for(int j = 1; j < length;j++)
		{
			result = result + " " + temp[j];
		}
		
		return result;
	}
	
	public static String capitalize(String word)
	{
		if(word==null || word=="") return null;
    	return word.substring(0,1).toUpperCase()+word.substring(1,word.length());
	}

	
}
