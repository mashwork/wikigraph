package com.mashwork.wikipedia.ParseXML.query.queryExample;

import com.mashwork.wikipedia.ParseXML.query.WikiQuery;
import org.neo4j.graphdb.Node;

public class Example_Retrieve_Speed
{
	public static void main(String[] args)
	{
		String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_Structure";
		//String DBDir = "/Users/Ricky/mashwork/ListCharac";
		//String DBDir = "/Users/Ricky/mashwork/Anarchism_D2_FULL_LUCENE_TEST";
		//String DBDir = "/Users/Ricky/mashwork/AnarchismD2DB_TEST";
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		String name1 = "HBO#Programming#Sports programming";
		String name2 = "Game of Thrones#Production#Filming";
		int i = 50000;
		int count = 0;
		long startTime = System.currentTimeMillis();
		while(i-- >0)
		{
			Node x = wikiQuery.findPage(name1);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Node found: "+count+". Time used: "+(endTime-startTime)+" ms.");
	}
}
