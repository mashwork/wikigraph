package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.util.List;

import org.neo4j.graphdb.Node;

import com.mashwork.wikipedia.ParseXML.neo4j.HierachyManager;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

@Deprecated
public class Example2
{
	public static void main(String[] args)
	{
		//String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB_LUCENE_Structure";
		//String DBDir = "/Users/Ricky/mashwork/ListCharac";
		//String DBDir = "/Users/Ricky/mashwork/Anarchism_D2_FULL_LUCENE_TEST";
		String DBDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/GraphDB";
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		
//		System.out.println("DB set up. Start to search.");
//		List<String> names = wikiQuery.findPath("Tyrion Lannister","Jon Snow",20);
//		wikiQuery.printPath(names);
		
		System.out.println("All the paths: ");
		List<List<String>> paths = wikiQuery.findShortestPaths("True Blood","Breaking Bad",5);//Austrian School William Godwin
		//List<List<String>> paths = wikiQuery.findShortestPaths("Game of Thrones","Category:Fantasy television series",20);
		wikiQuery.printAllPaths(paths);
	
		System.out.println("All the categories paths: ");
		//List<List<String>> categoryPaths = wikiQuery.findCommonCategories("Brad Pitt","Britney Spears",4);
		List<List<String>> categoryPaths = wikiQuery.findCommonCategories("True Blood","Breaking Bad",5);
		wikiQuery.printAllPaths(categoryPaths);
		
//		System.out.println("All the Father node paths: (Degree 1)");
//		//List<List<String>> paths = wikiQuery.findShortestPaths("Sean Bean","Kit Harington",20);
//		List<List<String>> fatherPaths = wikiQuery.findFathers("Anarchism",2,false);
//		wikiQuery.printAllPaths(fatherPaths);
		
		
//		System.out.println("The component number for ");
//		Pair<Integer,Integer> pair = wikiQuery.getComponentNumbers("Political philosophy");
//		System.out.println("For node Political philosophy: The number of Toc is "+pair.getFirst()+"." +
//		" The number of links is "+pair.getSecond() +".");
		
		//List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
			//	"Anarchism is often defined as a political philosophy which holds the state to be undesirable");
		//List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
				//"used almost exclusively in this sense until the 1950s in the United States");
//		List<Node> luceneSuggestion = wikiQuery.luceneQuerySuggestion(
//			"I love Castle Black. Ygritte is so beautiful");
//				//"My GF bought me a pair of Nike shoes.");
//
//		int i = 1;
//		for(Node node: luceneSuggestion)
//		{
//			if(HierachyManager.isPageNode(node))
//    		{
//    			System.out.println(i++ +": "+node.getProperty(wikiQuery.USERNAME_KEY).toString());
//    		}
//    		else {
//    			 System.out.println(i++ +": "+node.getProperty(wikiQuery.TOC_KEY).toString());
//			}
//		}
	}
}
