package com.mashwork.wikipedia.ParseXML.serverQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ServerHandler
{

	static final String DBDir = "/Users/Ricky/mashwork/GOT_D3_DB";
    static WikiServerQuery wikiServerQuery = new WikiServerQuery(DBDir);
    
    static String method;
    static String startNode;
    static String endNode;
    static int maxDepth;
    static boolean isPageOnly;
    static int listLength;
    static List<String> nodeList;
    
	
	public static String query(Map<String, String> args)
	{
		parseCommand(args);
		String errorInfo = null;
		if(method==null) return null;
		if(method.equals("findPath()"))
		{
			 errorInfo = isErrorCommand(123);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindPath(startNode,endNode,maxDepth);
			}
		}
		else if(method.equals("findShortestPaths()"))
		{
			errorInfo = isErrorCommand(123);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindShortestPaths(startNode,endNode,maxDepth);
			}
		}
		else if(method.equals("findCategories()"))
		{
			errorInfo = isErrorCommand(13);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindCategories(startNode,maxDepth);
			}
		}
		else if(method.equals("findFathers()"))
		{
			errorInfo = isErrorCommand(134);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindFathers(startNode,maxDepth,isPageOnly);
			}
		}
		else if(method.equals("findCommonAncestors()"))
		{
			errorInfo = isErrorCommand(356);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindCommonAncestor(nodeList,maxDepth);
			}
		}
		else if(method.equals("printQuerySuggestion()"))
		{
			errorInfo = isErrorCommand(1);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverQuerySuggestion(startNode);
			}
		}
		else if(method.equals("getComponentNumbers()"))
		{
			errorInfo = isErrorCommand(1);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverGetComponentNumbers(startNode);
			}
		}
		else if(method.equals("findRoot()"))
		{
			errorInfo = isErrorCommand(1);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindRoot(startNode);
			}
		}
		else if(method.equals("findPage()"))
		{
			errorInfo = isErrorCommand(1);
			if(errorInfo==null)
			{
				return wikiServerQuery.serverFindPage(startNode);
			}
		}
		else
		{
			return errorCommand(0);
		}
		
		
		return errorInfo;
	}
	
	
	private static void parseCommand(Map<String,String> args)
	{
		clearCommand();
		if(args.get("method")!=null)
		{
			method = args.get("method");
		}
		
		if(args.get("startNode")!=null)
		{
			startNode = args.get("startNode");
		}
		
		if(args.get("endNode")!=null)
		{
			endNode = args.get("endNode");
		}
		
		if(args.get("maxDepth")!=null)
		{
			maxDepth = Integer.parseInt(args.get("maxDepth"));
		}
		
		if(args.get("isPageOnly")!=null && args.get("isPageOnly").equals("true"))
		{
			isPageOnly = true;
		}
		if(args.get("isPageOnly")!=null && args.get("isPageOnly").equals("false"))
		{
			isPageOnly = false;
		}
		

		if(args.get("listLength")!=null)
		{
			listLength = Integer.parseInt(args.get("listLength"));
			int length = Integer.parseInt(args.get("listLength"));
			nodeList = new ArrayList<String>();
			for(int i = 1;i <= length;i++)
			{
				nodeList.add(args.get("node"+i));
			}
		}
	}
	
	private static void clearCommand()
	{
		method = null;
		startNode = null;
		endNode = null;
		maxDepth = -1;
		isPageOnly = true;
		listLength = -1;
		nodeList = null;
	}
	
	private static String isErrorCommand(int code)
	{
		if(code == 123)
		{
			if(startNode==null)
			{
				return errorCommand(1);
			}
			if(endNode==null)
			{
				return errorCommand(2);
			}
			if(maxDepth==-1)
			{
				return errorCommand(3);
			}
		}
		else if(code == 13)
		{
			if(startNode==null)
			{
				return errorCommand(1);
			}
			if(maxDepth==-1)
			{
				return errorCommand(3);
			}
		}
		else if(code == 134)
		{
			if(startNode==null)
			{
				return errorCommand(1);
			}
			if(maxDepth==-1)
			{
				return errorCommand(3);
			}
		}
		else if(code == 356)
		{
			if(maxDepth==-1)
			{
				return errorCommand(3);
			}
			if(listLength==-1)
			{
				return errorCommand(4);
			}
			if(listLength != nodeList.size())
			{
				return errorCommand(5);
			}
		}
		else if(code == 1)
		{
			if(startNode==null)
			{
				return errorCommand(1);
			}
		}
		
		return null;
		
	}
	
	private static String errorCommand(int type)
	{
		if(type==0)
		{
			return "Can not find such method!\n";
		}
		else if(type==1)
		{
			return "Missing startNode!\n";
		}
		else if(type==2)
		{
			return "Missing endNode!\n";
		}
		else if(type==3)
		{
			return "maxDepth error!";
		}
		else if(type==4)
		{
			return "List legnth error!";
		}
		else if(type==5)
		{
			return "List legnth does not match param number!";
		}
		else
		{
			return "No such error code!";
		}
	}
	
}
