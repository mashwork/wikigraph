package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.util.List;

import org.neo4j.graphdb.Node;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

public class Example2_LUCENE
{
	public static void main(String[] args)
	{
		//String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_TEST";
		String DBDir = "/Users/Ricky/mashwork/Anarchism_D2_FULL_LUCENE_TEST";
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		
		System.out.println("DB set up. Start to search.");
		//wikiQuery.tryQuery();
		
		//List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
			//	"Anarchism is often defined as a political philosophy which holds the state to be undesirable");
		List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
				"used almost exclusively in this sense until the 1950s in the United States");
		int i = 1;
		for(Node node: luceneSuggestion)
		{
			if(HierachyManager.isPageNode(node))
    		{
    			System.out.println(i++ +": "+node.getProperty(wikiQuery.USERNAME_KEY).toString());
    		}
    		else {
    			 System.out.println(i++ +": "+node.getProperty(wikiQuery.TOC_KEY).toString());
			}
		}
	}
}
