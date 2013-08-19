package com.mashwork.wikipedia.ParseXML.query;

import java.util.Comparator;

import com.mashwork.wikipedia.ParseXML.neo4j.Pair;


/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This comparator is used in Levenshtein distance comparison. The less distance, the better. The system rank
 * most similar results and sort them by Levecshtein distance.
 */
public class SuggestionComparator implements Comparator<Pair<String, Integer>>
{
	public int compare(Pair<String, Integer> pair1, Pair<String, Integer> pair2)
	{
		//pair1 = (Pair<String, Integer>)pair1;
		//pair2 = (Pair<String, Integer>)pair2;
		
		if(pair1.getSecond() > pair2.getSecond())
		{
			return 1;
		}
		else if(pair1.getSecond() == pair2.getSecond())
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
}
