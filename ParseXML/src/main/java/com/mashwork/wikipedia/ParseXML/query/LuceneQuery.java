package com.mashwork.wikipedia.ParseXML.query;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.Node;

import com.mashwork.wikipedia.ParallelXML.ParallelLucene.WikiAnalyzer;
import com.mashwork.wikipedia.ParseXML.neo4j.Pair;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used for lucene query. Every time a query comes, this class can provide us the best matched documents.
 * See detailed documentation for each of the functions.
 */
public class LuceneQuery
{
	IndexSearcher indexSearcher;
	QueryParser titleParser;
	QueryParser textParser;
	MultiFieldQueryParser queryParser;
	WikiQuery wikiQuery;
	
	float entropy_max_threshold = (float) -0.23;
	float entropy_sum_threshold = (float) -0.5;
	float avg_score_threshold = (float) 0.2;
	
	public LuceneQuery(String luceneIndexDir, String graphDbDir)
	{
		this.wikiQuery = new WikiQuery(graphDbDir);
		try
		{
			indexSearcher = new IndexSearcher(IndexReader.open(FSDirectory
					.open(new File(luceneIndexDir))));
		}catch(IOException e)
		{
			System.out.println("Can not load lucene index!");
			e.printStackTrace();
		}

		InputStream stopWordDir = LuceneQuery.class.getClassLoader().getResourceAsStream("stopwords");
		Set<String> stopWords = null;
		try
		{
			stopWords = getStopWords(stopWordDir);
		}catch(IOException e)
		{
			System.out.println("Read Stop Words Error!");
			e.printStackTrace();
		}
		
		String[] fields = new String[] { "title", "text" ,"alias", "abbreviation"};
		HashMap<String,Float> boosts = new HashMap<String,Float>();
		
		// Give different fields different weights. Text information is more helpful in find the topic for a tweet.
		boosts.put("title", (float) 1.0); //10
		boosts.put("text", (float) 5.0);	//5
		boosts.put("alias", (float) 10.0);		//5 or 10
		boosts.put("abbreviation", (float) 0.0);
		this.queryParser = new MultiFieldQueryParser(
			Version.LUCENE_36,
		    fields, 
		    new WikiAnalyzer(Version.LUCENE_36,stopWords),
		    boosts
		);
	}
	
	//First need to load the stop words. The stop words are a combination of MySQL stop words and emotion tokens.
	public Set<String> getStopWords(InputStream stopWordStream) throws IOException
	{
		InputStreamReader Sr = new InputStreamReader(stopWordStream);
		BufferedReader Br = new BufferedReader(Sr);
		Set<String> stopWords = new HashSet<String>();
		String word = null;
		while((word = Br.readLine())!=null)
		{	
			stopWords.add(word);
			//System.out.println(word);
		}
		return stopWords;
	}
	
	public Float AnalyseScore(ScoreDoc[] hits)
	{
		float score = 0;
		for(ScoreDoc hit:hits)
		{
			score += hit.score;
		}
		score = score / hits.length;
		return score;
	}
	
	
	/*
	 * This function is used to calculate the entropy of the results.
	 * Entropy is used to evaluate how clean the results are.
	 * If 9 out of 10 results belong to the same class, the value will be small.
	 * By the entropy value, we can filter out some noise. Because noise query usually
	 * gets random returned results belonging to different classes which will make the
	 * value high.
	 */
	public Pair<String,Float> calculateEntropy(HashMap<String,Float> Entrophy)
	{
		float entropy_max = -Float.MAX_VALUE;
		float entropy_sum = 0;
		String entropy_max_name = null;
		for(String name:Entrophy.keySet())
		{
			Entrophy.put(name,Entrophy.get(name)* (float)Math.log(Entrophy.get(name)));
		}
		for(String name:Entrophy.keySet())
		{
			float value = Entrophy.get(name);
			entropy_sum = entropy_sum + value;
			if(entropy_max < value)
			{ 
				entropy_max = value;
				entropy_max_name = name;
			}
		}
		
		if(entropy_max > entropy_max_threshold && entropy_sum > entropy_sum_threshold)
		{
			return(new Pair<String,Float>(entropy_max_name,entropy_max));
		}
		else
		{
			return null;
		}
	}
	
	//calculate the frequency of each of the class,frequency will be used to calculate entropy later.
	public Pair<String,Float> AnalyseName(List<String> names)
	{
		HashMap<String,Integer> count = new HashMap<String,Integer>();
		for(String name:names)
		{
			name = name.split("#")[0];
			if(count.containsKey(name))
			{
				count.put(name, count.get(name)+1);
			}
			else 
			{
				count.put(name,1);
			}
		}
		
		HashMap<String,Float> Entrophy = new HashMap<String,Float>();
		for(String name:count.keySet())
		{
			Entrophy.put(name,(float)count.get(name)/names.size());
		}
		
		return calculateEntropy(Entrophy);
	}
	
	//based on the entropy value and the mode of the results, try to estimate which class a query belongs to.
	public String LuceneEstimate(String queryString)
	{
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
			//e.printStackTrace();
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, 10);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}
		
		float avg_score = 0;
		ScoreDoc[] hits = results.scoreDocs;
		List<String> names = new ArrayList<String>();
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
				names.add(doc.get("title"));
				avg_score += hit.score;
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
		}
		avg_score = avg_score / hits.length;
		StringBuilder SB = new StringBuilder();
		Pair<String,Float> result = AnalyseName(names);
		if(avg_score > avg_score_threshold && result !=null)
		{
			
//			System.out.println("The page estimate is "+ result.getFirst());
//			System.out.println("The avg score is : "+avg_score);
//			System.out.println("The entropy for this name is :"+ result.getSecond());
			SB.append("The page estimate is: "+ result.getFirst()+"\n");
			SB.append("The avg score is : "+avg_score+"\n");
			SB.append("The entropy for this name is :"+ result.getSecond()+"\n");
		}
		else
		{
//			System.out.println("Result too noisy. Refuse to tag this tweet!å");
			SB.append("Result too noisy. Refuse to tag this tweet!");
		}
		return SB.toString();
	}
	
	//returns the matched results based on lucene score. No estimation will be made.
	//Top numToShow results will be returned.
	public String search(String queryString, int numToShow)
	{
		long startTime = System.currentTimeMillis();
		StringBuilder SB = new StringBuilder();
		System.out.println("Now search for query: "+queryString);
		
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
			//e.printStackTrace();
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, numToShow);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}
		
		SB.append("Totally "+results.totalHits+" matches found based on text.\n");
		SB.append("Showing the top "+numToShow+" results:\n");
		ScoreDoc[] hits = results.scoreDocs;
		
		SB.append("No.	Score		Title\n");
		int i = 1;
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
			SB.append(i++ +".	"+hit.score+ "	"+doc.get("title")+"		"+ doc.get("alias")+"		"+ doc.get("abbreviation")+"\n");
		}
		long endTime = System.currentTimeMillis();
		SB.append("Totally "+(endTime-startTime) +" ms used to search.\n");
		return SB.toString();
	}
	
	//return the top3 matched results.
	public String[] getTop3(String queryString)
	{
		//long startTime = System.currentTimeMillis();
		String[] result = new String[3];
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, 3);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}

		ScoreDoc[] hits = results.scoreDocs;

		int i = 1;
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
			result[i++-1] = doc.get("title");
			//System.out.println(result[i-1]);
		}
		//long endTime = System.currentTimeMillis();
		return result;
	}
	
	//Does not use entropy, but use avg score and mode to determine the best result of a query.
	public String labelEstimate(String queryString, int numToShow)
	{
		if(queryString == null) return null;
		long startTime = System.currentTimeMillis();
		StringBuilder SB = new StringBuilder();
		//System.out.println("Now search for query: "+queryString);
		
		Query query = null;
		try
		{
			//query = textParser.parse(queryString);
			query = queryParser.parse(queryString);
		}catch(ParseException e)
		{
			System.out.println("Parser error!");
			System.out.println("Can not parse " + queryString);
			return null;
			//e.printStackTrace();
		}
		
		TopDocs results = null;
		try
		{
			results = indexSearcher.search(query, numToShow);
		}catch(IOException e)
		{
			System.out.println("Index search error!");
			e.printStackTrace();
		}
		
		SB.append("Totally "+results.totalHits+" matches found based on text.\n");
		ScoreDoc[] hits = results.scoreDocs;
		HashMap<String,Float> names = new HashMap<String,Float>();
		float avg_score = 0;
		SB.append("No.	Score		Title\n");
		int i = 1;
		for (ScoreDoc hit : hits) {
			Document doc = null;
			try
			{
				doc = indexSearcher.doc(hit.doc);
			}catch(Exception e)
			{
				System.out.println("Can not read results!");
				e.printStackTrace();
			}
			names.put(doc.get("title"),hit.score);
			avg_score = avg_score + hit.score;
			SB.append(i++ +".	"+hit.score+ "	"+doc.get("title")+"		"+ doc.get("alias")+"		"+ doc.get("abbreviation")+"\n");
		}
		long endTime = System.currentTimeMillis();
		float total_score;
		total_score = avg_score;
		avg_score = avg_score/(i-1);
		if(avg_score > 0.0)
		{
			SB.append(analyzeName(names,total_score));
		}
		else
		{
			return null;
			//SB.append("Avg_score is "+avg_score+". Too low. Refuse to tag.");
		}
		SB.append("Totally "+(endTime-startTime) +" ms used to search.\n");
		return SB.toString();
	}
	
	//get the document name. As table of content can be a document. So the title name will be "Game of Thrones（TV Series）#Plot",
	//Both "TV Series" and "Game of Thrones" will be counted.
	public String analyzeName(HashMap<String,Float> names, float total_score)
	{
		HashMap<String,Float> stat = new HashMap<String,Float>();
		HashMap<String,Integer> stat2 = new HashMap<String,Integer>();
		Iterator<String> it = names.keySet().iterator();
		while(it.hasNext())
		{
			String name = it.next();
			float score = names.get(name);
			String[] names_split = name.split("#");
//			names_split = names_split[0].split("\\(");
			String reg = "(.*?)( ?)((\\()(.*?)(\\)))?$";
            Pattern pattern = Pattern.compile(reg); 
            //System.out.println("AAA"+names_split[0]);
            Matcher matcher = pattern.matcher(names_split[0]); 
            matcher.find();
            String name1 = matcher.group(1);
            //name1 = name1.substring(0, name1.length()-1);
            String name2 = matcher.group(5);
            if(stat.containsKey(name1))
			{
				stat.put(name1, stat.get(name1) + score);
				stat2.put(name1, stat2.get(name1) + 1);
			}
			else
			{
				stat.put(name1, score);
				stat2.put(name1, 1);
			}
            if(name2 != null && !name2.equals(""))
            {
            	if(stat.containsKey(name2))
    			{
    				stat.put(name2, stat.get(name2) + score);
    				stat2.put(name2, stat2.get(name2) + 1);
    			}
    			else
    			{
    				stat.put(name2, score);
    				stat2.put(name2, 1);
    			}
            }

		}
		
		Iterator<String> it2 = stat.keySet().iterator();
		StringBuilder SB = new StringBuilder();
		SB.append("Estimated topic: ");
		boolean hit = false;
		float percent;
		while(it2.hasNext())
		{
			String label = it2.next();
			percent = stat.get(label)/total_score;
			Node node = wikiQuery.findPage(label);
			if(node == null) continue;
			int TocNumber = wikiQuery.findTocComponents(node).size();
			if(stat.get(label) > 0.8  && percent > 0.5 )		//names.size()/2
			{
				if(stat2.get(label) > TocNumber*0.8)
				{
					hit = true;
					SB.append(label + " ");
				}
				
			}
		}
		
		if(hit)
		{
			SB.append("\n");
			return SB.toString();
		}
		else 
		{
			return "Result too noisy!\n";
		}
		
	}
}
