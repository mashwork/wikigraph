package com.mashwork.wikipedia.ParseXML.neo4j;

import java.text.DecimalFormat;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
@Deprecated
/*
 * All the classes under this package are deprecated. These classes used a different schema to put node and links into
 * neo4j. It is efficient when the data size is small. But will have performance issue if it is big. Most of the time
 * is spent on retrieving node(memory-IO swapping).
 */
public class NodeElementParser extends ElementParser{
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";

	static String fatherName;
	final static int pageCount = 25579;
	static int links = 0;
	static int counter = 0;
	static Transaction tx;
	
	public NodeElementParser(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx)
	{
		super(graphDb,nodeIndex,TocIndex, fullTextIndex);
		NodeElementParser.tx = tx;
	}
	
	@Override
	public void handleElement(String element, String value)
	{
		if(element.equals("l") || element.equals("d"))
		{
			links++;
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
			
			if ( counter > 0 && counter % 10000 == 0 ) {
	            tx.success();
	            tx.finish();
	            tx = graphDb.beginTx();
	        }
			
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
			//Node node = createTocNode(value,fatherName);
			Node node = createTocNode(value);
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
	private Node createTocNode(String pageName)
	{
		String prePath = HierachyManager.getPrePath();
		Node exist = TocIndex.get(TOC_KEY,prePath+"#"+pageName).getSingle();
		if(exist!=null)
		{
			return exist;
		}
		else
		{
			Node node = graphDb.createNode();
	        node.setProperty( USERNAME_KEY, pageName );
	        String TocName = prePath + "#"+pageName;
	        //System.out.println(TocName);
	        node.setProperty( TOC_KEY, TocName);
	        //if(fatherName.equals("Amsterdam")) System.out.println("The stored name: "+TocName);
	        TocIndex.add(node,TOC_KEY,TocName);
	        
	        fullTextIndex.add(node,TOC_KEY,TocName.replace('#',' '));
	        return node;
		}
	}
	
	//This is for page nodes.
	private Node createAndIndexNode(String pageName)
    {
        Node node = graphDb.createNode();
        node.setProperty( USERNAME_KEY, pageName );
        nodeIndex.add( node, USERNAME_KEY, pageName );
        
        fullTextIndex.add(node,USERNAME_KEY,pageName);
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
