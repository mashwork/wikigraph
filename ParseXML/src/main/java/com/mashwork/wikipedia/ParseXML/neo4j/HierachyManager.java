package com.mashwork.wikipedia.ParseXML.neo4j;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.neo4j.graphdb.Node;

public class HierachyManager {
	static HashMap<String,Integer> Order;
	static Stack<Pair<String,Node>> stack;
	static Stack<Pair<String,String>> pathStack;
	final static String USERNAME_KEY = "pageName";
	final static String TOC_KEY = "TocName";
	
	public static void tractPath(Pair<String,String> pair)
	{
		if(pathStack.isEmpty())
		{
			pathStack.push(pair);
		}
		else 
		{
			String topElementOrder = pathStack.peek().getFirst();
			String newElementOrder = pair.getFirst();
			if(Order.get(newElementOrder) - Order.get(topElementOrder) <=0)
			{
				pathStack.pop();
				tractPath(pair);
			}
			else
			{
				pathStack.push(pair);
			}
		}
	}
	
	public static String getPath()
	{
		Iterator<Pair<String,String>> it = pathStack.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext())
		{
			sb.append(it.next().getSecond()+"#");
		}
		String result = sb.toString();
		if(result==null || result.equals("")) return null;
		return result.substring(0, result.length()-1);
	}
	
	private static int compare(String level)
	{
		if(stack.isEmpty())
		{
			return 1;
		}
		//System.out.println(stack.peek().getFirst());
		//System.out.println("Order.get(nodeName) "+ Order.get(nodeName) + "Node name is "+nodeName);
		//System.out.println("Order.get(stack.peek().getFirst()) " + Order.get(stack.peek().getFirst()));
		//System.out.println(Order.get(nodeName) - Order.get(stack.peek().getFirst()));
		//System.out.println("level "+level +" stack " + stack.peek().getFirst());
		if(Order.get(level)==null)
		{
			System.out.println(level);
		}
		return (Order.get(level) - Order.get(stack.peek().getFirst()));
	}
	
	public static Pair<String, Node> MyPeek()
	{
		return stack.peek();
	}
	
	public static void MyPush(Pair<String, Node> pair)
	{
		if(Order.get(pair.getFirst())!=null)
		stack.push(pair);
	}
	
	public static void MyPop(Pair<String, Node> pair)
	{
		if(compare(pair.getFirst()) > 0)
		{
			return;
		}
		else
		{
			stack.pop();
			MyPop(pair);
		}
	}
	
	public static Node findParentNode(String level)
	{
		while(compare(level)<=0)
		{
			stack.pop();
		}
		return stack.peek().getSecond();
	}
	
	public static String getPrePath()
	{
		if(isPageNode(stack.peek().getSecond()))
		{
			return stack.peek().getSecond().getProperty(USERNAME_KEY).toString();
		}
		else
		{
			return stack.peek().getSecond().getProperty(TOC_KEY).toString();
		}
	}
	
	public static boolean isPageNode(Node node)
    {
    	List<String> nameList = (List<String>)node.getPropertyKeys();
//    	for(String key:nameList)
//    	{
//    		System.out.println(key);
//    	}
    	if(nameList.contains(TOC_KEY))
    		return false;
    	else {
			return true;
		}
    }
	
	//public static void Init()
	static{
		Order = new HashMap<String, Integer>();
		Order.put("t",1);
		Order.put("c1",2);
		Order.put("c2",3);
		Order.put("c3",4);
		Order.put("c4",5);
		Order.put("c5",6);
		Order.put("c6",7);
		Order.put("c7",7);
		Order.put("c8",7);
		Order.put("c9",7);
		Order.put("c10",7);
		Order.put("c11",7);
		Order.put("c12",7);
		Order.put("c13",7);
		Order.put("c14",7);
		Order.put("c15",7);
		Order.put("c16",7);
		Order.put("c17",7);
		Order.put("c18",7);
		Order.put("c19",7);
		Order.put("c20",7);
		Order.put("l",8);
		
		stack  = new Stack<Pair<String,Node>>();
		pathStack = new Stack<Pair<String,String>>();
	}
	
}
