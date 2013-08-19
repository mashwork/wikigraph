package com.mashwork.wikipedia.ParseXML.neo4j;

import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.List;

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
public class LinkElementParser extends ElementParser{
	final String USERNAME_KEY = "pageName";
	final String TOC_KEY = "TocName";
	final static int linkCount = 3038323;
	static int counter = 0;
	static List<Node> ListNodes = null;
	static String fatherName = null;
	
	static Transaction tx;
	
	public LinkElementParser(GraphDatabaseService graphDb, Index<Node> nodeIndex,
			Index<Node> TocIndex, Index<Node> fullTextIndex, Transaction tx)
	{
		super(graphDb,nodeIndex,TocIndex,fullTextIndex);
		LinkElementParser.tx = tx;
	}
	
	@Override
	public void handleElement(String element, String value)
	{
		if(element.equals("d"))
		{
			return;
		}
		if(element.equals("t"))
		{
			Node node = retrievePageNode(value);
			if(node == null)
			{
				System.out.println("Error! Page node not found!");
			}
			//ListNodes = TOCLocator.findAllTOC(node);
			fatherName = value;
			Pair<String,Node> pair = new Pair<String,Node>(element,node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);
			//System.out.println("Create node: " +value);
		}
		else if(!element.equals("l"))
		{
			HierachyManager.findParentNode(element);
			//Node node = TOCLocator.findNextToc(fatherNode,value);
			Node node = findNextTOC(value);
			if(node == null) System.out.println("findNextTOC is null" +" Value:"+value);
			Pair<String,Node> pair = new Pair<String,Node>(element,node);
			HierachyManager.MyPop(pair);
			HierachyManager.MyPush(pair);	
		}
		else			//This is the case for links. Need to analyze several cases.
		{
			if ( counter > 0 && counter % 10000 == 0 ) {
	            tx.success();
	            tx.finish();
	            tx = graphDb.beginTx();
	        }
			
			DecimalFormat df = new DecimalFormat("0.00");
			double percentage = ((double)counter++/linkCount*100);
			if(counter%(linkCount/100) == 0 )
			{
				System.out.println("Processing link: "+ counter
					+"  "+df.format(percentage)+"% " +"  "+ value);
			}
			
			value = toNormalLink(value);
			
			if(isAnchorLink(value))
			{
				Node TOC = locateToc(value);
				if(TOC != null)			//TOC == null means anchor link goes to page that is not crawled in xml.
				{
					Pair<String,Node> fatherPair = HierachyManager.MyPeek();
					Node fatherNode = fatherPair.getSecond();
					
					fatherNode.createRelationshipTo(TOC, RelTypes.ANCHOR);
//					System.out.println("Link Created(Anchor): "+fatherNode.getProperty(USERNAME_KEY)+
//							" -> "+TOC.getProperty(USERNAME_KEY));
				}
				//System.out.println(value);
			}
			else
			{
				//System.out.println(value);
				
				Node node = retrievePageNode(value);
				if(node == null)
				{
					node = createAndIndexNode(value);
				}
				Pair<String,Node> fatherPair = HierachyManager.MyPeek();
				Node fatherNode = fatherPair.getSecond();
				
				if(isCategoryLink(value))
				{
					fatherNode.createRelationshipTo(node, RelTypes.CATEGORY);
//					System.out.println("Link Created(Cate): "+fatherNode.getProperty(USERNAME_KEY)+
//							" -> "+node.getProperty(USERNAME_KEY));
				}
				else
				{
					if(fatherNode==null)
					{
						System.out.println("Father node is null");
						return;
					}
					if(node==null)
					{
						System.out.println("Son node is null");
						return;
					}
					//System.out.println("Father: " + fatherNode.getProperty(USERNAME_KEY) +"  Node "+ node.getProperty(USERNAME_KEY));
					fatherNode.createRelationshipTo(node, RelTypes.INTERNAL);
//					System.out.println("Link Created(Inter): "+fatherNode.getProperty(USERNAME_KEY)+
//							" -> "+node.getProperty(USERNAME_KEY) + "  value:" + value);
				}
				
			}
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
	
	private boolean isAnchorLink(String value)
	{
		if(value.contains("#"))
			return true;
		else
			return false;
	}
	
	private boolean isCategoryLink(String value)
	{
		if(value.contains("Category:"))
			return true;
		else
			return false;
	}
	
	//Make sure to exclude inter-page anchor links like "#abc". Exclude it in xmlParser first.
	private Node locateToc(String value)
	{
		//System.out.println("Is anchor link." + value);
		String[] pageName = value.split("#");
		Node page = retrievePageNode(pageName[0]);
		if(pageName.length == 1 || page==null)
		{
			TOCLocator.linkNotFound++;
			//System.out.println("Warning: Case I. Page " + pageName[0] + " is not stored in the current xml. Out of range.");
			return null;
		}
		Node TOC = TOCLocator.findToc2(page,pageName[1]);
		return TOC;
	}
	private Node findNextTOC(String queryName)
	{
		String TocName = HierachyManager.getPrePath() +"#"+ queryName;
		//System.out.println(TocName);
		//System.out.println("TocName is :" + TocName);
		//Iterator<Node> it = TocIndex.get(TOC_KEY,TocName);
		return TocIndex.get(TOC_KEY,TocName).getSingle();
		//return null;
	}
	
	private String toNormalLink(String link)
	{
		if(link==null) return null;
    	link =  link.substring(0,1).toUpperCase()+link.substring(1,link.length());
    	try{
    		link = URLDecoder.decode(link,"UTF-8");
    	}catch(Exception e)
    	{
    		System.out.print("URLDecoder Error! ");
    		System.out.println(link);
    	}
		return link.replace('_',' ');
	}
//	private Node findNextTOC(String queryName)
//	{
//		Iterator<Node> it = ListNodes.iterator();
//		while(it.hasNext())
//		{
//			Node node = it.next();
//			if(node.getProperty(USERNAME_KEY).equals(queryName))
//				return node;
//		}
//		return null;
//	}
}
