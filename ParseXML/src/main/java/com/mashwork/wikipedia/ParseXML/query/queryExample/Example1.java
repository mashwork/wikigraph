package com.mashwork.wikipedia.ParseXML.query.queryExample;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Node;

import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.query.*;

public class Example1
{
	public static void main(String[] args)
	{
		String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB";
		WikiQuery wikiQuery = new WikiQuery(DBDir);
		
		System.out.println("DB set up. Start to search.");
		
		//List<String> names = wikiQuery.findPath("Game of Thrones","Jon Snow",20);
		//List<String> names = wikiQuery.findPath("Game of Thrones","John Bradley-West",20);
		List<String> names = wikiQuery.findPath("Game of Thrones","War of the Roses",20);
		wikiQuery.printPath(names);
		
		System.out.println("All the paths: ");
		List<List<String>> paths = wikiQuery.findShortestPaths("Tyrion Lannister","List of A Song of Ice and Fire characters",20);
		//List<List<String>> paths = wikiQuery.findShortestPaths("Game of Thrones","Category:Fantasy television series",20);
		wikiQuery.printAllPaths(paths);
		
		System.out.println("All the categories paths: ");
		//List<List<String>> paths = wikiQuery.findShortestPaths("Sean Bean","Kit Harington",20);
		List<List<String>> categoryPaths = wikiQuery.findCategories("Ned Stark",5);
		wikiQuery.printAllPaths(categoryPaths);
		
		System.out.println("All the Father node paths: (Degree 1)");
		//List<List<String>> paths = wikiQuery.findShortestPaths("Sean Bean","Kit Harington",20);
		List<List<String>> fatherPaths = wikiQuery.findFathers("Catelyn Stark",1,false);
		wikiQuery.printAllPaths(fatherPaths);
		
		System.out.println("All the Father node paths: (Degree 2)");
		List<List<String>> fatherPaths2 = wikiQuery.findFathers("Catelyn Stark",2,false);
		wikiQuery.printAllPaths(fatherPaths2);
		
		//wikiQuery.printQuerySuggestion("game of thrones");
		wikiQuery.printQuerySuggestion("king's landing");
		//wikiQuery.printQuerySuggestion("season 3");
		
		System.out.println("\nCommon Ancestor: ");
		List<String> twoNodes = new ArrayList<String>();
		twoNodes.add("Tyrion Lannister");
		twoNodes.add("Catelyn Stark");
		twoNodes.add("Jeyne Westerling");
		//twoNodes.add("Jon Snow");
		//twoNodes.add("Emilia Clarke");
		List<Pair<Node,List<Integer>>> ancestors = wikiQuery.findCommonAncestor(twoNodes,4);
		int ii = 1;
		Pair<Integer,Integer> pair;
		String name;
		for(Pair<Node,List<Integer>> ancestor:ancestors)
		{
			if(wikiQuery.isPageNode(ancestor.getFirst()))
			{
				System.out.println(ii++ +". "+ancestor.getFirst().getProperty(wikiQuery.USERNAME_KEY).toString()
						+"  ID:"+ancestor.getFirst().getId());
				name = ancestor.getFirst().getProperty(wikiQuery.USERNAME_KEY).toString();
				pair = wikiQuery.getComponentNumbers(name);
				System.out.println("For node "+name+": The number of Toc is "+pair.getFirst()+"." +
						" The number of links is "+pair.getSecond() +".");
				
				System.out.print("Distances to this ancestor:");
				for(int distance:ancestor.getSecond())
				{
					System.out.print(distance+" ");
				}
				System.out.println();
			}
			else
			{
				System.out.println(ii++ +". "+ancestor.getFirst().getProperty(wikiQuery.TOC_KEY).toString()
						+"  ID:"+ancestor.getFirst().getId());
				name = ancestor.getFirst().getProperty(wikiQuery.TOC_KEY).toString();
				pair = wikiQuery.getComponentNumbers(name);
				System.out.println("For node "+name+": The number of sibling Toc is "+pair.getFirst()+"." +
						" The number of sibling links is "+pair.getSecond() +".");
				
				System.out.print("Distances to this ancestor:");
				for(int distance:ancestor.getSecond())
				{
					System.out.print(distance+" ");
				}
				System.out.println();
			}
		}
		
//		System.out.println();
//		String name = "Game of Thrones";
//		Pair<Integer,Integer> numbers = wikiQuery.getComponentNumbers(name);
//		System.out.println("For node "+name+": The number of Toc is "+numbers.getFirst()+"." +
//				" The number of links is "+numbers.getSecond() +".");
		
		
		//wikiQuery.printQuerySuggestion("Jon Snow");
	}
	
}
