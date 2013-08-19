package com.mashwork.wikipedia.ParallelXML.ParallelLucene;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * this class is used for tracking where we are in a tree structure. All the ancestor nodes will be stored in stack.
 */
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
		Order.put("c7",7);
		Order.put("c8",7);
		Order.put("c9",7);
//		Order.put("c10",7);
//		Order.put("c11",7);
//		Order.put("c12",7);
//		Order.put("c13",7);
//		Order.put("c14",7);
//		Order.put("c15",7);
		Order.put("l",8);
		
		pathStack = new Stack<Pair<String,String>>();
	}
	
	
	//update the path. Will pop() first, then push().
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
			if((Order.get(newElementOrder)!=null && Order.get(topElementOrder)!=null) &&
					Order.get(newElementOrder) - Order.get(topElementOrder) <=0)
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
