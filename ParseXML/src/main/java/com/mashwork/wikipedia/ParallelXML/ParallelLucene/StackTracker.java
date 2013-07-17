package com.mashwork.wikipedia.ParallelXML.ParallelLucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

public class StackTracker
{
	HashMap<String,Integer> Order;
	Stack<Pair<String,String>> pathStack;
	
	{
		Order = new HashMap<String, Integer>();
		Order.put("t",1);
		Order.put("c1",2);
		Order.put("c2",3);
		Order.put("c3",4);
		Order.put("c4",5);
		Order.put("c5",6);
		Order.put("c6",7);
		Order.put("c7",8);
		Order.put("c8",9);
		Order.put("l",10);
		
		pathStack = new Stack<Pair<String,String>>();
	}
	
	public void tractPath(Pair<String,String> pair)
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
	
	public String getPath()
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
	
}
