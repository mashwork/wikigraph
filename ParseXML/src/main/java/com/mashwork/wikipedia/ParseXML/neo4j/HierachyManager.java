package com.mashwork.wikipedia.ParseXML.neo4j;

import java.util.HashMap;
import java.util.Stack;
import org.neo4j.graphdb.Node;

public class HierachyManager {
	static HashMap<String,Integer> Order;
	static Stack<Pair<String,Node>> stack;
	
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
		return (Order.get(level) - Order.get(stack.peek().getFirst()));
	}
	
	public static Pair<String, Node> MyPeek()
	{
		return stack.peek();
	}
	
	public static void MyPush(Pair<String, Node> pair)
	{
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
		Order.put("c7",8);
		Order.put("c8",9);
		Order.put("l",10);
		
		stack  = new Stack<Pair<String,Node>>();
	}
	
}
