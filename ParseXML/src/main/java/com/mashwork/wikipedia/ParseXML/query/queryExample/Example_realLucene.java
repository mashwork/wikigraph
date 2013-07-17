package com.mashwork.wikipedia.ParseXML.query.queryExample;

import com.mashwork.wikipedia.ParseXML.query.LuceneQuery;

public class Example_realLucene
{
	public static void main(String[] args)
	{
		String LuceneIndexDir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/" +
				"GOT_D3_Poriton/MergedLuceneIndex";
		LuceneQuery luceneQuery = new LuceneQuery(LuceneIndexDir);
		
		String query = "As in anthropology, European witchcraft is seen by historians as an ideology for explaining misfortune.";
		
		String result = luceneQuery.search(query,10);
		System.out.println(result);
	}
}
