package com.mashwork.wikipedia.ParseXML.serverQuery;


/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time
 * a server information class that tells people how to use the query. This information should be
 * always shown to user.	
 */
public class ServerInfo
{
	public static String HelpInfo(String address,String port)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("------------------------------------Help Information----------------------------------\n");
		sb.append("This is the help information for query. Please set the correct query parametes.\n");
		sb.append("Before search, please make sure the name you input exists in the database.\n\n");
		sb.append("Parameter \"maxDepth\" means the maximum depth that the graph database will search for. The smaller the fewer results.\n");
		sb.append("Parameter \"isPageOnly\" means only page nodes will be returned. If it set to be false, table of content information will also be returned.\n");
		sb.append("Following is the query methods, please change the parameters by yourself and paste to the address bar.\n");
		sb.append("1.To find the shortest path between 2 pages, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findPath&startNode=Tyrion Lannister&endNode=Jon Snow&maxDepth=20\n");
		sb.append("2.To find all the paths between 2 nodes, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findShortestPaths&startNode=Tyrion Lannister&endNode=Jon Snow&maxDepth=20\n");
		sb.append("3.To find the categories that a page belongs to, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findCategories&startNode=Jon Snow&maxDepth=5\n");
		sb.append("4.To find father nodes of a page, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findFathers&startNode=Tyrion Lannister&maxDepth=3&isPageOnly=true\n");
		sb.append("5.To find common ancestors for several pages, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findCommonAncestors&nodeList=Tyrion Lannister&nodeList=Catelyn Stark&nodeList=Jeyne Westerling&listLength=3&maxDepth=4\n");
		sb.append("6.To find query suggestions for a name, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=printQuerySuggestion&startNode=king's landing\n");
		sb.append("7.To get how many components and links one page contains(accepts TOC page), please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=getComponentNumbers&startNode=Game of Thrones\n");
		sb.append("8.To find a root of a TOC page, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findRoot&startNode=Game of Thrones#Plot\n");
		sb.append("9.To check whether a page exist, please use:\n");
		sb.append("		http://"+address+":"+port+"/search.html?method=findPage&startNode=Tyrion Lannister\n");
		sb.append("------------------------------------------End-----------------------------------------\n");
		sb.append("");
		sb.append("");
		return sb.toString();
	}

}
