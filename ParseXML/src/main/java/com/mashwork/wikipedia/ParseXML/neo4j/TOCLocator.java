package com.mashwork.wikipedia.ParseXML.neo4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

public class TOCLocator {
	final static String USERNAME_KEY = "pageName";
	static int linkNotFound = 0;
	private static Traverser getNode(final Node node , int depth)
	{
	    TraversalDescription td = Traversal.description()
	            .breadthFirst()
	            .relationships( RelTypes.TOC, Direction.OUTGOING )
	            .evaluator( Evaluators.excludeStartPosition() )
	            .evaluator(Evaluators.toDepth(depth));
	    return td.traverse( node );
	}
	
//	private static Traverser getNode(final Node node)
//	{
//	    TraversalDescription td = Traversal.description()
//	            .breadthFirst()
//	            .relationships( RelTypes.TOC, Direction.OUTGOING )
//	            .evaluator( Evaluators.excludeStartPosition() );
//	    return td.traverse( node );
//	}
	
	//This method will find TOC in the next level(depth 1)
	public static List<Node> findAllTOC(Node currentNode)
	{
		//String name = currentNode.getProperty(USERNAME_KEY).toString();
		
		List<Node> result = new ArrayList<Node>();
		Traverser nodeTraverser = getNode(currentNode,5);
		Iterator<Node> it =  nodeTraverser.nodes().iterator();
		while(it.hasNext())
		{
			Node next = it.next();
			//if(name.equals("Nereid"))
			{
				System.out.println(next.getProperty(USERNAME_KEY));
			}
			result.add(next);
		}
		//if(name.equals("Nereid"))
		System.out.println("Node "+currentNode.getProperty(USERNAME_KEY)+"  "+result.size());
		return result;
	}
	
	public static Node findNextToc(Node currentNode, String queryName)
	{
		//System.out.println("111");
		Traverser nodeTraverser = getNode(currentNode,1);
		//System.out.println("222");
		Iterator<Node> it = nodeTraverser.nodes().iterator();
		System.out.println("The father is"+currentNode.getProperty(USERNAME_KEY));
		//int i = 1;
		while(it.hasNext())
		{
			Node node = it.next();
			//System.out.println("The TOC is"+node.getProperty(USERNAME_KEY) + "  "+i++);
			if(node.getProperty(USERNAME_KEY).equals(queryName))
			{
				return node;
			}
		}
		//System.out.println("333");
		//System.out.println("Can not find such node! Error in findNextToc()!");
		return null;
	}
	
	//This method will find TOC in the whole tree structure(depth 10)
	public static Node findToc(Node currentNode, String queryName)
	{
		Traverser nodeTraverser = getNode(currentNode,5);			//at most depth 5 will be searched.
		for(Path nodePath: nodeTraverser)
		{
			if(nodePath.endNode().getProperty(USERNAME_KEY).equals(queryName))
			{
				//System.out.println("Found TOC");
				return nodePath.endNode();
			}
		}
		linkNotFound++;
		//System.out.println("Warning: Case II. Page has been found. But TOC of this page is not stored!");
		return null;
	}
	public static Node findToc2(Node currentNode, String queryName)
	{
		//System.out.println("444");
		Traverser nodeTraverser = getNode(currentNode,1);			//at most depth 5 will be searched.
		//System.out.println("555");
		Iterator<Node> it = nodeTraverser.nodes().iterator();
		while(it.hasNext())
		{
			Node node = it.next();
			if(node.getProperty(USERNAME_KEY).equals(queryName))
			{
				return node;
			}
		}
		//System.out.println("666");
		linkNotFound++;
		//System.out.println("Warning: Case II. Page has been found. But TOC of this page is not stored!");
		return null;
	}
}
