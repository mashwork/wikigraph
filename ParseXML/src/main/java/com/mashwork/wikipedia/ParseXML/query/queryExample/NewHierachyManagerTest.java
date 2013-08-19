package com.mashwork.wikipedia.ParseXML.query.queryExample;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

//used to test how the new static tool function works.
@Deprecated
public class NewHierachyManagerTest
{
	public static void main(String[] args)
	
	{
		Pair<String,String> p1 = new Pair<String,String>("t","GOT");
		Pair<String,String> p2 = new Pair<String,String>("c1","c1a");
		Pair<String,String> p3 = new Pair<String,String>("c1","c1b");
		Pair<String,String> p4 = new Pair<String,String>("c2","c2");
		Pair<String,String> p5 = new Pair<String,String>("c3","c3");
		Pair<String,String> p6 = new Pair<String,String>("t","apple");
		
		HierachyManager.tractNamePath(p1);
		System.out.println(HierachyManager.getNamePath());
		HierachyManager.tractNamePath(p2);
		System.out.println(HierachyManager.getNamePath());
		HierachyManager.tractNamePath(p3);
		System.out.println(HierachyManager.getNamePath());
		HierachyManager.tractNamePath(p4);
		System.out.println(HierachyManager.getNamePath());
		HierachyManager.tractNamePath(p5);
		System.out.println(HierachyManager.getNamePath());
		HierachyManager.tractNamePath(p6);
		System.out.println(HierachyManager.getNamePath());
	}
}
