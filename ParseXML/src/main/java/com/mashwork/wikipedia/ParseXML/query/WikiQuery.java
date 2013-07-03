package com.mashwork.wikipedia.ParseXML.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Map;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.util.Version;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Expander;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.kernel.Traversal;

import com.mashwork.wikipedia.ParseXML.neo4j.RelTypes;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

import de.linuxusers.levenshtein.algos.LevenshteinDistance;
import de.linuxusers.levenshtein.util.Conversion;

public class WikiQuery {
	private final GraphDatabaseService graphDb;
    private final Index<Node> nodeIndex;
    private final Index<Node> TocIndex;
    private final Index<Node> fullTextIndex;
    public final int DefaultQuerySuggestionNumber = 60;
    public final String USERNAME_KEY = "pageName";
	public final String TOC_KEY = "TocName";
	public final String TEXT_KEY = "fullText";
	
	public WikiQuery(String DBDir)
	{
		Map<String, String> config = new HashMap<String, String>();
		config.put( "read_only", "true" );
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
		        DBDir )
		        .setConfig( config )
		        .newGraphDatabase();
		
		
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DBDir );
        nodeIndex = graphDb.index().forNodes( "nodes" );
        TocIndex = graphDb.index().forNodes( "Toc" );
        fullTextIndex= graphDb.index().forNodes("full");
        registerShutdownHook();
	}
	
	
	protected Traverser getCate(final Node node , int depth)
	{
	    TraversalDescription td = Traversal.description()
	            .breadthFirst()
	            .relationships( RelTypes.CATEGORY, Direction.OUTGOING )
	            .relationships( RelTypes.TOC, Direction.OUTGOING )
	            .evaluator( Evaluators.excludeStartPosition() )
	            .evaluator(Evaluators.toDepth(depth));
	    return td.traverse( node );
	}
	
	protected Traverser getFathers(final Node node, int depth)
	{
		TraversalDescription td = Traversal.description()
	            .depthFirst()
	            .relationships( RelTypes.INTERNAL, Direction.INCOMING )
	            .relationships( RelTypes.TOC, Direction.INCOMING )
	            .relationships( RelTypes.ANCHOR, Direction.INCOMING )
	            .evaluator( Evaluators.excludeStartPosition() )
	            .evaluator(Evaluators.toDepth(depth));
	    return td.traverse( node );
	}
	
	public List<List<String>> findCategories(String nodeName, int maxDepth)
	{
		Node startNode = findPage(nodeName);
		if(startNode==null)
    	{
    		System.out.println("Start Node is Null! Can not find \""+nodeName+"\"!");
        	printQuerySuggestion(nodeName);
        	return null;
    	}
		Traverser categoryPaths = getCate(startNode, maxDepth);
		List<List<String>> result = new ArrayList<List<String>>();
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
		if(startNode==null)
    	{
    		System.out.println("Start Node is Null! Can not find \""+nodeName+"\"!");
        	printQuerySuggestion(nodeName);
        	return null;
    	}
		Traverser fatherPaths = getFathers(startNode, maxDepth);
		List<Path> result = new ArrayList<Path>();
		for(Path path : fatherPaths)
		{
			result.add(path);
		}
		return result;
	}
	
	public List<List<String>> findFathers(String nodeName, int maxDepth, boolean pageNodeOnly)
	{
		List<Path> fatherPaths = findFatherPaths(nodeName, maxDepth);
		if(fatherPaths==null) return null;
		List<List<String>> result = new ArrayList<List<String>>();
		if(pageNodeOnly)
		{
			for(Path path : fatherPaths)
			{
				if(isPageNode(path.endNode()))
				{
					result.add(extractTitles(path));
				}
			}
		}
		else				//TOC node will be included
		{
			for(Path path : fatherPaths)
			{
				result.add(extractTitles(path));
			}
		}
		return result;
	}
	
	protected Expander setAllExpander()
	{
		Expander expander = Traversal.expanderForAllTypes();
		return expander;
	}
	
	public List<String> findPath(String startPage, String endPage, int maxDepth) {
		Node startNode = findPage(startPage);
        Node endNode = findPage(endPage);
        if(startNode == null || endNode==null)
        {
        	if(startNode==null)
        	{
	        	System.out.println("Start Node is Null! Can not find \""+startPage+"\"!");
	        	printQuerySuggestion(startPage);
        	}
        	if(endNode==null)
        	{
        		System.out.println("End Node is Null! Can not find \""+endPage+"\"!");
	        	printQuerySuggestion(endPage);
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
        	if(startNode==null)
        	{
	        	System.out.println("Start Node is Null! Can not find \""+startPage+"\"!");
	        	printQuerySuggestion(startPage);
        	}
        	if(endNode==null)
        	{
        		System.out.println("End Node is Null! Can not find \""+endPage+"\"!");
	        	printQuerySuggestion(endPage);
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
	
//	public List<Node> findCommonAncestor(List<String> nodeSet, int maxDepth)
//	{
//		if(nodeSet.size()<2)
//		{
//			System.out.println("Input size less than 2. No common ancestor!");
//			return null;
//		}
//		else
//		{
//			List<Node> commonAncestors;
//			
//			List<Node> nodeCandidates;
//			
//			String startNode = nodeSet.get(0);
//			commonAncestors = getEndNodes(findFatherPaths(startNode,maxDepth));
//			for(int i = 1; i < nodeSet.size();i++)
//			{
//				String currentNode = nodeSet.get(i);
//				nodeCandidates = getEndNodes(findFatherPaths(currentNode,maxDepth));
//				commonAncestors = findCommonNodes(commonAncestors,nodeCandidates);
//			}
//			return commonAncestors;
//		}
//	}
	
	public List<Pair<Node,List<Integer>>> findCommonAncestor(List<String> nodeSet, int maxDepth)
	{
		if(nodeSet.size()<2)
		{
			System.out.println("Input size less than 2. No common ancestor!");
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
	
	public Node findPage(String title) {
        Node node = nodeIndex.get(USERNAME_KEY, title).getSingle();
        if (node == null) {
        	//node = TocIndex.get(TOC_KEY,title).getSingle();
        	Iterator<Node> it = TocIndex.get(TOC_KEY,title);
        	if(it.hasNext())
        	{
        		node = it.next();
        	}
        	else 
        	{
				node = null;
			}
        	//node = TocIndex.get(TOC_KEY,title).next();
//        	if(node != null)
//        	{
//        		System.out.println("UserName: "+node.getProperty(USERNAME_KEY).toString());
//        		System.out.println("TOC: "+node.getProperty(TOC_KEY).toString());
//        	}
//        	if(node == null)
//        	{
//                System.out.println("Can not find such node!");
//                System.out.println("For query " + "\""+title+"\""+" node name suggestion is: ");
//                printQuerySuggestion(title);
//        		//throw new IllegalArgumentException("no such page: " + title);
//        	}
        }
        return node;
    }

//	public List<Node> findCommonNodes(List<Node> nodeSet1, List<Node> nodeSet2)
//	{
//		Iterator<Node> it1 = nodeSet1.iterator();
//		Iterator<Node> it2 = nodeSet2.iterator();
//		List<Node> result = new ArrayList<Node>();
//		while(it1.hasNext())
//		{
//			Node one = it1.next();
//			it2 = nodeSet2.iterator();
//			
//			while(it2.hasNext())
//			{
//				Node two = it2.next();
//				if(one.getId()==two.getId())
//				{
//					result.add(one);
//				}
//			}
//		}
//		return result;
//	}
	
	public List<Pair<Node,List<Integer>>> findCommonNodes(List<Pair<Node,List<Integer>>> nodeSet1, List<Pair<Node,List<Integer>>> nodeSet2)
	{
		Iterator<Pair<Node,List<Integer>>> it1 = nodeSet1.iterator();
		Iterator<Pair<Node,List<Integer>>> it2 = nodeSet2.iterator();
		List<Pair<Node,List<Integer>>> result = new ArrayList<Pair<Node,List<Integer>>>();
		while(it1.hasNext())
		{
			Pair<Node,List<Integer>> one = it1.next();
			it2 = nodeSet2.iterator();
			
			while(it2.hasNext())
			{
				Pair<Node,List<Integer>> two = it2.next();
				if(one.getFirst().getId()==two.getFirst().getId())
				{
					one.getSecond().add(two.getSecond().get(0));
					result.add(one);
				}
			}
		}
		return result;
	}
	
//	public List<Node> getEndNodes(List<Path> paths)
//	{
//		List<Node> nodes = new ArrayList<Node>();
//		for(Path path: paths)
//		{
//			nodes.add(path.endNode());
//		}
//		return nodes;
//	}
	
	public List<Pair<Node,List<Integer>>> getEndNodes(List<Path> paths)
	{
		List<Pair<Node,List<Integer>>> nodes = new ArrayList<Pair<Node,List<Integer>>>();
		for(Path path: paths)
		{
			List<Integer> distance = new ArrayList<Integer>();
			distance.add(path.length());
			Pair<Node,List<Integer>> pair = new Pair<Node,List<Integer>>(path.endNode(),distance);
			nodes.add(pair);
		}
		return nodes;
	}
	
	protected Traverser getTocNodes(final Node node)
	{
		TraversalDescription td = Traversal.description()
	            .depthFirst()
	            .relationships( RelTypes.TOC, Direction.OUTGOING )
	            .evaluator( Evaluators.excludeStartPosition() );
	    return td.traverse( node );
	}
	
	//To find all the TOC components under this page.
	public List<Node> findTocComponents(Node startNode)
	{
		Traverser Tocs = getTocNodes(startNode);
		List<Node> result = new ArrayList<Node>();
		for(Path Toc : Tocs)
		{
			result.add(Toc.endNode());
		}
		return result;
	}
	
	protected Traverser getLink(final Node node)
	{
		TraversalDescription td = Traversal.description()
	            .depthFirst()
	            .relationships( RelTypes.INTERNAL, Direction.OUTGOING )
	            .relationships( RelTypes.ANCHOR, Direction.OUTGOING )
	            .evaluator( Evaluators.excludeStartPosition() )
	            .evaluator(Evaluators.toDepth(1));
	    return td.traverse( node );
	}
	
	//This is used to determine how many links are under these nodes.
	protected int getLinkNumber(List<Node> nodes)
	{
		int result = 0;
		for(Node node: nodes)
		{
			Traverser Tocs = getLink(node);
			//System.out.println(Tocs.getClass());
			for(Path Toc:Tocs)
			{
				result++;
			}
		}
		return result;
	}
	
	//TOC number and Link number under startNode will be returned. To calculate how big a page is.
	//Can also used to calculate a TOC node's sibling numbers if nodeName is a TOC.
	public Pair<Integer,Integer> getComponentNumbers(String nodeName)
	{
		Node startNode = findRoot(nodeName);
		List<Node> nodes;
		nodes = findTocComponents(startNode);
		int TocNumber = nodes.size();
		nodes.add(startNode);
		int LinkNumber = getLinkNumber(nodes);
		return new Pair<Integer,Integer>(TocNumber,LinkNumber);
	}
	
	protected Traverser getRootNode(final Node node)
	{
		TraversalDescription td = Traversal.description()
	            .depthFirst()
	            .relationships( RelTypes.TOC, Direction.INCOMING )
	            .evaluator( Evaluators.excludeStartPosition() );
	    return td.traverse( node );
	}
	
	public Node findRoot(String name)
	{
		Node startNode = findPage(name);
		if(isPageNode(startNode))
		{
			return startNode;
		}
		else
		{
			Traverser roots = getRootNode(startNode);
			for(Path root:roots)
			{
				if(isPageNode(root.endNode()))
				{
					return root.endNode();
				}
			}
			return null;
		}
	}
	
	protected List<String> extractTitles(Path path) {
        if (path == null) {
            return null;
        }
        List<String> pages = new ArrayList<String>();
        for (Node node : path.nodes()) {
        	//String TocName = node.getProperty(TOC_KEY,null).toString();
        	//List<String> nameList = (List<String>)node.getPropertyKeys();
        	if(!isPageNode(node))
        	{
        		pages.add(node.getProperty(TOC_KEY).toString());
        	}
        	else
        	{
        		pages.add((String) node.getProperty(USERNAME_KEY));
        	}
        }
        return pages;
    }
    
    public boolean isPageNode(Node node)
    {
    	List<String> nameList = (List<String>)node.getPropertyKeys();
    	if(nameList.size()>1)
    		return false;
    	else {
			return true;
		}
    }
    
    
    public void tryQuery()
    {
    	IndexHits<Node> hits;
    	
    	String query = "Game of Thrones Tyrion Lannister Robb Stark";
    	hits = fullTextIndex.query(USERNAME_KEY,query);
        System.out.println("Search by page title. Result for query: "+ query);
        int i = 1;
    	for(Node node:hits)
    	{
    		if(i>10) break;
    		System.out.println(i+++": "+node.getProperty(USERNAME_KEY).toString());
    	}
    	System.out.println("\n");
    	
    	query = "Game of Thrones Plot";
    	hits = fullTextIndex.query(TOC_KEY,query);
    	System.out.println("Search by TOC title. Result for query: "+ query);
        i = 1;
    	for(Node node:hits)
    	{
    		if(i>10) break;
    		System.out.println(i+++": "+node.getProperty(TOC_KEY).toString());
    	}
    	System.out.println("\n");

    	
    	//List<String> result = new ArrayList<String>();
    	//Query query1 = new WildcardQuery( new Term( USERNAME_KEY, "Game of*" ) ) ;
//    	hits = nodeIndex.query("pageName:*Game* AND pageName:*Thrones* AND pageName:*TV*");
//    	System.out.println("The result");
//    	for(Node node:hits)
//    	{
//    		System.out.println(node.getProperty(USERNAME_KEY).toString());
//    	}
    	
    	Query queryLucene;
        //要查找的字符串数组
        String [] stringQuery={"Game of thrones", "Characters"};	//,"Thrones"
        //待查找字符串对应的字段
        String[] fields={USERNAME_KEY,TOC_KEY};		//,USERNAME_KEY
        //Occur.MUST表示对应字段必须有查询值， Occur.MUST_NOT 表示对应字段必须没有查询值
        Occur[] occ={Occur.SHOULD,Occur.SHOULD};
        Analyzer ana = new SimpleAnalyzer(Version.LUCENE_36);
        try
        {
        	queryLucene=MultiFieldQueryParser.parse(Version.LUCENE_36,stringQuery,fields,occ,ana);
        	i = 1;
	    	hits = fullTextIndex.query(queryLucene);
	    	System.out.println("Multifiled query:");
	    	for(Node node:hits)
	    	{
	    		if(isPageNode(node))
	    		{
	    			System.out.println(node.getProperty(USERNAME_KEY).toString());
	    		}
	    		else {
	    			 System.out.println(node.getProperty(TOC_KEY).toString());
				}
	    		if(i++>30) break;
	    	}
    	
        }catch(Exception e)
        {
        	System.out.println(e);
        }
        
        
//        Term t1 = new Term(USERNAME_KEY,"Game");
//        //Term t2 = new Term(USERNAME_KEY,"Thrones");
//        
//        TermQuery tq1 = new TermQuery(t1);
//        //TermQuery tq2 = new TermQuery(t2);
//        
//        BooleanQuery bq = new BooleanQuery();
//        bq.add(tq1,Occur.MUST);
//        //bq.add(tq2,Occur.MUST);
//        
//        hits = nodeIndex.query(bq);
//        System.out.println("The result 3:");
//    	for(Node node:hits)
//    	{
//    		System.out.println(node.getProperty(USERNAME_KEY).toString());
//    	}
    }
    
    public List<Pair<String,Integer>> querySuggestion(String nodeName)
    {
    	//String name = captalize(nodeName);
    	String pageTitle = FormalString.formalize(nodeName);
    	String tocTitle = FormalString.capitalize(nodeName);
    	
//    	System.out.println("\n\nThe original name: "+nodeName);
//    	System.out.println("The formal page name: "+pageTitle);
//    	System.out.println("The formal TOC name: "+tocTitle+"\n");
    	
    	IndexHits<Node> hits;
    	List<String> result = new ArrayList<String>();
    	
    	//search by the capitalized name
    	hits = nodeIndex.query( new WildcardQuery( new Term( USERNAME_KEY, "*"+pageTitle+"*" ) ) );
    	for ( Node node : hits )
    	{
    	    //System.out.println( movie.getProperty( USERNAME_KEY ) );
    		result.add(node.getProperty(USERNAME_KEY).toString());
    	}
    	
    	//example: King's Landing
    	hits = TocIndex.query( new WildcardQuery( new Term( TOC_KEY, "*"+pageTitle+"*" ) ) );
    	for ( Node node : hits )
    	{
    	    //System.out.println( movie.getProperty( USERNAME_KEY ) );
    		result.add(node.getProperty(TOC_KEY).toString());
    	}
    	
    	//example: Cast and characters
    	hits = TocIndex.query( new WildcardQuery( new Term( TOC_KEY, "*"+tocTitle+"*" ) ) );
    	for ( Node node : hits )
    	{
    	    //System.out.println( movie.getProperty( USERNAME_KEY ) );
    		result.add(node.getProperty(TOC_KEY).toString());
    	}
    	
    	List<Pair<String,Integer>> sortedResult = sortQuerySuggestion(nodeName, result);
    	
    	return sortedResult;
    }
    
    public void printQuerySuggestion(String nodeName)
    {
    	List<Pair<String,Integer>> suggestions = querySuggestion(nodeName);
    	if(suggestions.isEmpty())
    	{
    		System.out.println("Can not find query suggestion for \""+nodeName+"\"!");
    	}
    	else 
    	{
	    	System.out.println("Here is the query suggestion for \""+nodeName+"\": " +
	    			"(Top "+DefaultQuerySuggestionNumber+" suggestions will be returned)");
    		for(int i = 0; i < DefaultQuerySuggestionNumber && i < suggestions.size();i++)
	    	{
	    		System.out.println(i+1 +". "+suggestions.get(i).getFirst());
	    	}
    	}
    }
    
    public List<Node> luceneQuerySuggestion(String name)
    {
    	IndexHits<Node> hits;
    	List<Node> result = new ArrayList<Node>();
    	Query queryLucene;
        //要查找的字符串数组
        String [] stringQuery={name};	//,"Thrones"
        //待查找字符串对应的字段
        String[] fields={TEXT_KEY};		//,USERNAME_KEY
        //Occur.MUST表示对应字段必须有查询值， Occur.MUST_NOT 表示对应字段必须没有查询值
        Occur[] occ={Occur.SHOULD};
        Analyzer ana = new SimpleAnalyzer(Version.LUCENE_36);
        try
        {
        	queryLucene=MultiFieldQueryParser.parse(Version.LUCENE_36,stringQuery,fields,occ,ana);
        	int i = 1;
	    	hits = fullTextIndex.query(queryLucene);
	    	System.out.println("Multifiled query:");
	    	for(Node node:hits)
	    	{
	    		if(i++ > DefaultQuerySuggestionNumber) break;
	    		result.add(node);
	    	}
    	
        }catch(Exception e)
        {
        	System.out.println(e);
        }
        return result;
        
        
    }
    
    public void printQuerySuggestion(String nodeName, int querySuggestionNumber)
    {
    	List<Pair<String,Integer>> suggestions = querySuggestion(nodeName);
    	if(suggestions.isEmpty())
    	{
    		System.out.println("Can not find query suggestion for \""+nodeName+"\"!");
    	}
    	else 
    	{
	    	System.out.println("Here is the query suggestion for \""+nodeName+"\": " +
	    			"(Top "+querySuggestionNumber+" suggestions will be returned)");
    		for(int i = 0; i < querySuggestionNumber && i < suggestions.size();i++)
	    	{
	    		System.out.println(i+1 +". "+suggestions.get(i).getFirst());
	    	}
    	}
    }
    
    protected List<Pair<String,Integer>> sortQuerySuggestion(String query,List<String> suggestions)
    {
    	LevenshteinDistance<Character> levDistance = new LevenshteinDistance<Character>();
    	List<Pair<String,Integer>> result = new LinkedList<Pair<String,Integer>>();
    	for(String suggestion:suggestions)
    	{
    		Integer distance = (int)levDistance.getDistance( 
    				Conversion.convertToArray(query), Conversion.convertToArray(suggestion));
    		result.add(new Pair<String,Integer>(suggestion,distance));
    	}
    	Collections.sort(result, new SuggestionComparator());
    	return result;
    	
    }
	
    public void printPath(List<String> path)
    {
    	if(path!=null)
    	{
    		for(int i = 0;i < path.size()-1;i++)
    		{
    			System.out.print(path.get(i)+"->");
    		}
    		System.out.println(path.get(path.size()-1));
    	}
    }
    
    public void printAllPaths(List<List<String>> paths)
    {
    	int i = 1;
    	if(paths!=null)
    	{
	    	for(List<String> path : paths)
	    	{
	    		System.out.print(i++ +". ");
	    		printPath(path);
	    	}
	    	System.out.println();
    	}
    }
    

    protected boolean isCategoryLink(String value)
	{
		if(value.contains("Category:"))
			return true;
		else
			return false;
	}
    
    
    protected void shutdown()
    {
        graphDb.shutdown();
    }
	
    protected void registerShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                shutdown();
            }
        } );
    }
}
