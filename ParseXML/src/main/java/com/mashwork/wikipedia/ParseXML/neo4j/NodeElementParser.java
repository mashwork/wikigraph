package com.mashwork.wikipedia.ParseXML.neo4j;

import java.text.DecimalFormat;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

public class NodeElementParser extends ElementParser{
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";
	static String fatherName;
	final static int pageCount = 20855;
	static int counter = 0;
	public NodeElementParser(GraphDatabaseService graphDb, Index<Node> nodeIndex, Index<Node> TocIndex)
	{
		super(graphDb,nodeIndex,TocIndex);
	}
	
	@Override
	public void handleElement(String element, String value)
	{
		if(element.equals("l") || element.equals("d"))
		{
			return;
		}
		if(element.equals("t"))
		{
			Node node = retrievePageNode(value);
			if(node == null)
			{
				node = createAndIndexNode(value);
			}
			Pair<String,Node> pair = new Pair<String,Node>(element,node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);
			fatherName = node.getProperty(USERNAME_KEY).toString();
			//System.out.println("Create node: " +value);
			DecimalFormat df = new DecimalFormat("0.00");
			double percentage = ((double)counter++/pageCount*100);
			if(counter%(pageCount/100) == 0 )
			{
				System.out.println("Processing tree: "+ counter
					+"  "+df.format(percentage)+"% " +"  "+ value);
			}
		}
		else
		{
			Node fatherNode = HierachyManager.findParentNode(element);
			//String fatherName = fatherNode.getProperty(USERNAME_KEY).toString();
			Node node = createTocNode(value,fatherName);
			Pair<String,Node> pair = new Pair<String,Node>(element,node);
			//HierachyManager.MyPop(pair);
			
			//Pair<String,Node> fatherPair = HierachyManager.MyPeek();
			//Node fatherNode = fatherPair.getSecond();
			
			fatherNode.createRelationshipTo(node, RelTypes.TOC);
			
			
			
			HierachyManager.MyPush(pair);
			
//			System.out.println("Create node: " +value + " Create link: " + 
//			fatherNode.getProperty(USERNAME_KEY)+ " to " + value);
		}
	}
	
	//This is for Table of Content nodes.
	private Node createTocNode(String pageName, String fatherName)
	{
		Node node = graphDb.createNode();
        node.setProperty( USERNAME_KEY, pageName );
        String TocName = fatherName + pageName;
        node.setProperty( TOC_KEY, TocName);
        if(fatherName.equals("Amsterdam")) System.out.println("The stored name: "+TocName);
        TocIndex.add(node,TOC_KEY,TocName);
        return node;
	}
	
	//This is for page nodes.
	private Node createAndIndexNode(String pageName)
    {
        Node node = graphDb.createNode();
        node.setProperty( USERNAME_KEY, pageName );
        nodeIndex.add( node, USERNAME_KEY, pageName );
        return node;
    }
	
	private Node retrievePageNode(String nodeName)
	{
		Node result = nodeIndex.get(USERNAME_KEY,nodeName).getSingle();
		return result;
	}
	
//	private boolean existNode(String nodeName)
//	{
//		if(nodeIndex.get(USERNAME_KEY,nodeName).getSingle() != null)
//		{
//			return true;
//		}
//		else
//		{
//			return false;
//		}
//	}
}
