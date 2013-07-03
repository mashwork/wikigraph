package com.mashwork.wikipedia.ParseXML.serverQuery;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Traverser;

import com.mashwork.wikipedia.ParseXML.neo4j.Pair;
import com.mashwork.wikipedia.ParseXML.query.WikiQuery;

public class WikiServerQuery extends WikiQuery
{
	public boolean startNodeNotFound;
	public boolean endNodeNotFound;
	public WikiServerQuery(String DBDir)
	{
		super(DBDir);
	}
	
	public String serverFindPath(String startNode,String endNode,int maxDepth)
	{
		List<String> result = findPath(startNode,endNode,maxDepth);
		if(result==null)
		{
			StringBuffer sb = new StringBuffer();
			if(startNodeNotFound==true)
			{
				sb.append("StartNode not found. Here is the query suggestion for "+startNode+"\n");
				sb.append(serverQuerySuggestion(startNode));
			}
			if(endNodeNotFound==true)
			{
				sb.append("EndNode not found. Here is the query suggestion for "+endNode+"\n");
				sb.append(serverQuerySuggestion(endNode));
			}
			return sb.toString();
		}
		else
		{
			return resultToString(result);
		}
	}
	public String serverFindShortestPaths(String startNode,String endNode,int maxDepth)
	{
		List<List<String>> result = findShortestPaths(startNode,endNode,maxDepth);
		if(result==null)
		{
			StringBuffer sb = new StringBuffer();
			if(startNodeNotFound==true)
			{
				sb.append("StartNode not found. Here is the query suggestion for "+startNode+"\n");
				sb.append(serverQuerySuggestion(startNode));
			}
			if(endNodeNotFound==true)
			{
				sb.append("EndNode not found. Here is the query suggestion for "+endNode+"\n");
				sb.append(serverQuerySuggestion(endNode));
			}
			return sb.toString();
		}
		else
		{
			return allResultToString(result);
		}
	}
	
	public String serverFindCategories(String startNode,int maxDepth)
	{
		List<List<String>> result = findCategories(startNode,maxDepth);
		if(result==null)
		{
			StringBuffer sb = new StringBuffer();
			if(startNodeNotFound==true)
			{
				sb.append("StartNode not found. Here is the query suggestion for "+startNode+"\n");
				sb.append(serverQuerySuggestion(startNode));
			}
			return sb.toString();
		}
		else
		{
			return allResultToString(result);
		}
	}
	public String serverFindFathers(String startNode,int maxDepth,boolean isPageOnly)
	{
		List<List<String>> result = findFathers(startNode,maxDepth,isPageOnly);
		if(result==null)
		{
			StringBuffer sb = new StringBuffer();
			if(startNodeNotFound==true)
			{
				sb.append("StartNode not found. Here is the query suggestion for "+startNode+"\n");
				sb.append(serverQuerySuggestion(startNode));
			}
			return sb.toString();
		}
		else
		{
			return allResultToString(result);
		}
	}
	public String serverFindCommonAncestor(List<String> nodeList,int maxDepth)
	{
		List<Pair<Node,List<Integer>>> result = findCommonAncestor(nodeList,maxDepth);
		if(result==null)
		{
			return "Input number less than 2 OR no common ancestors for these names!\n";
		}
		else
		{
			return pairResultToString(result);
		}
	}
	public String serverQuerySuggestion(String startNode)
	{
		List<Pair<String,Integer>> result = querySuggestion(startNode);
		return suggestionToString(result,startNode);
	}
	public String serverGetComponentNumbers(String startNode)
	{
		Pair<Integer,Integer> pair = getComponentNumbers(startNode);
		return "For node "+startNode+": The number of Toc is "+pair.getFirst()+"." +
				" The number of links is "+pair.getSecond() +".";
	}
	public String serverFindRoot(String startNode)
	{
		return findRoot(startNode).getProperty(USERNAME_KEY).toString();
	}
	
	public String serverFindPage(String startNode)
	{
		Node result = findPage(startNode);
		if(result==null)
		{
			return "Node \""+startNode+"\" does not exist in the graph database. Please try other names.";
		}
		else
		{
			return "Node \""+startNode+"\" exists.";
		}
	}
	
	public String resultToString(List<String> path)
    {
    	StringBuffer sb = new StringBuffer();
    	if(path!=null)
    	{
    		for(int i = 0;i < path.size()-1;i++)
    		{
    			sb.append(path.get(i)+"->");
    		}
    		sb.append(path.get(path.size()-1));
    	}
    	return sb.toString();
    }
    
	public String allResultToString(List<List<String>> paths)
    {
    	StringBuffer sb = new StringBuffer();
    	int i = 1;
    	if(paths!=null)
    	{
	    	for(List<String> path : paths)
	    	{
	    		sb.append(i++ +". ");
	    		sb.append(resultToString(path)+"\n");
	    	}
	    	sb.append("\n");
    	}
    	return sb.toString();
    }
    
	public String pairResultToString(List<Pair<Node,List<Integer>>> ancestors)
    {
    	StringBuffer sb = new StringBuffer();
    	int ii = 1;
		Pair<Integer,Integer> pair;
		String name;
		for(Pair<Node,List<Integer>> ancestor:ancestors)
		{
			if(isPageNode(ancestor.getFirst()))
			{
				sb.append(ii++ +". "+ancestor.getFirst().getProperty(USERNAME_KEY).toString()
						+"  ID:"+ancestor.getFirst().getId()+"\n");
				name = ancestor.getFirst().getProperty(USERNAME_KEY).toString();
				pair = getComponentNumbers(name);
				sb.append("For node "+name+": The number of Toc is "+pair.getFirst()+"." +
						" The number of links is "+pair.getSecond() +".\n");
				
				sb.append("Distances to this ancestor:");
				for(int distance:ancestor.getSecond())
				{
					sb.append(distance+" ");
				}
				sb.append("\n");
			}
			else
			{
				sb.append(ii++ +". "+ancestor.getFirst().getProperty(TOC_KEY).toString()
						+"  ID:"+ancestor.getFirst().getId()+"\n");
				name = ancestor.getFirst().getProperty(TOC_KEY).toString();
				pair = getComponentNumbers(name);
				sb.append("For node "+name+": The number of sibling Toc is "+pair.getFirst()+"." +
						" The number of sibling links is "+pair.getSecond() +".\n");
				
				sb.append("Distances to this ancestor:");
				for(int distance:ancestor.getSecond())
				{
					sb.append(distance+" ");
				}
				sb.append("\n");
			}
		}
		
		return sb.toString();
    }
    
	public String suggestionToString(List<Pair<String,Integer>> suggestions, String nodeName)
    {
    	StringBuffer sb = new StringBuffer();
    	if(suggestions.isEmpty())
    	{
    		sb.append("Can not find query suggestion for \""+nodeName+"\"!\n");
    	}
    	else 
    	{
    		sb.append("Here is the query suggestion for \""+nodeName+"\": " +
	    			"(Top "+DefaultQuerySuggestionNumber+" suggestions will be returned)\n");
    		for(int i = 0; i < DefaultQuerySuggestionNumber && i < suggestions.size();i++)
	    	{
    			sb.append(i+1 +". "+suggestions.get(i).getFirst()+"\n");
	    	}
    	}
    	return sb.toString();
    }
	
	public void clearNotFound()
	{
		startNodeNotFound = false;
		endNodeNotFound = false;
	}
	
	
	@Override
	public List<List<String>> findCategories(String nodeName, int maxDepth)
	{
		Node startNode = findPage(nodeName);
		List<List<String>> result = new ArrayList<List<String>>();
		if(startNode==null)
    	{
			clearNotFound();
			startNodeNotFound = true;
        	return null;
    	}
		Traverser categoryPaths = getCate(startNode, maxDepth);
		for(Path path : categoryPaths)
		{
			if(isCategoryLink(path.endNode().getProperty(USERNAME_KEY).toString()))
			{
				result.add(extractTitles(path));
			}
		}
		return result;
	}
	
	public List<Path> findFatherPaths(String nodeName, int maxDepth)
	{
		Node startNode = findPage(nodeName);
		List<Path> result = new ArrayList<Path>();
		if(startNode==null)
    	{
			clearNotFound();
			startNodeNotFound = true;
        	return null;
    	}
		Traverser fatherPaths = getFathers(startNode, maxDepth);
		
		for(Path path : fatherPaths)
		{
			result.add(path);
		}
		return result;
	}
	
	public List<String> findPath(String startPage, String endPage, int maxDepth) {
		Node startNode = findPage(startPage);
        Node endNode = findPage(endPage);
        if(startNode == null || endNode==null)
        {
        	clearNotFound();
        	if(startNode == null)
        	{
        		startNodeNotFound = true;
        	}
        	if(endNode == null)
        	{
        		endNodeNotFound = true;
        	}
        	return null;
        }
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(setAllExpander(), maxDepth);
        Path path = finder.findSinglePath(startNode, endNode);
        return extractTitles(path);
    }
	
	public List<List<String>> findShortestPaths(String startPage, String endPage, int maxDepth) {
		Node startNode = findPage(startPage);
        Node endNode = findPage(endPage);
        if(startNode == null || endNode==null)
        {
        	clearNotFound();
        	if(startNode == null)
        	{
        		startNodeNotFound = true;
        	}
        	if(endNode == null)
        	{
        		endNodeNotFound = true;
        	}
        	return null;
        }
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(setAllExpander(), maxDepth);
        Iterable<Path> paths = finder.findAllPaths(startNode, endNode);
        List<List<String>> pagePaths = new ArrayList<List<String>>();
        for (Path path : paths) {
            pagePaths.add(extractTitles(path));
        }
        return pagePaths;
    }
	
	public List<Pair<Node,List<Integer>>> findCommonAncestor(List<String> nodeSet, int maxDepth)
	{
		if(nodeSet.size()<2)
		{
			return null;
		}
		else
		{
			List<Pair<Node, List<Integer>>> commonAncestors;
			
			List<Pair<Node, List<Integer>>> nodeCandidates;
			
			String startNode = nodeSet.get(0);
			commonAncestors = getEndNodes(findFatherPaths(startNode,maxDepth));
			for(int i = 1; i < nodeSet.size();i++)
			{
				String currentNode = nodeSet.get(i);
				nodeCandidates = getEndNodes(findFatherPaths(currentNode,maxDepth));
				commonAncestors = findCommonNodes(commonAncestors,nodeCandidates);
			}
			return commonAncestors;
		}
	}
}
