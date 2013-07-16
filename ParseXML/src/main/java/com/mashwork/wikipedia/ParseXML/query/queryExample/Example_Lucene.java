package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.util.List;

import org.neo4j.graphdb.Node;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

public class Example_Lucene
{
	public static void main(String[] args)
	{
		String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_FullText";
		//String DBDir = "/Users/Ricky/mashwork/ListCharac";
		//String DBDir = "/Users/Ricky/mashwork/Anarchism_D2_FULL_LUCENE_TEST";
		//String DBDir = "/Users/Ricky/mashwork/AnarchismD2DB_TEST";
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
				"I love Jon Snow. Ygritte is so beautiful");
				//"So glad Daenerys killed Xaro.");
				//"Qarth is a beautiful city.");
				//"My GF bought me a pair of Nike shoes.");
				

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
