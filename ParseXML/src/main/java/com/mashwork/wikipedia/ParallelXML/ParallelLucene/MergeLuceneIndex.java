package com.mashwork.wikipedia.ParallelXML.ParallelLucene;

import java.io.File;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class MergeLuceneIndex
{
	public static void mergeIndex(String subIndexesPrefix, String mergedIndexPath, int portionNumber) throws Exception
	{
		//FSDirectory subIndexes = FSDirectory.open(new File(subIndexesPath));
		//File subIndexes = new File(subIndexesPath);
		FSDirectory mergedIndex = FSDirectory.open(new File(mergedIndexPath));
		
		WikiAnalyzer wikiAnalyzer = new WikiAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,wikiAnalyzer);
		TieredMergePolicy mergePolicy = new TieredMergePolicy();
		//config.setMergePolicy(mergePolicy.setMaxMergeAtOnce(8));
		//config.setMergePolicy(mergePolicy.setSegmentsPerTier(8));
		//config.setMergePolicy(mergePolicy.setMaxMergedSegmentMB(8));
		config.setRAMBufferSizeMB(500);
		
		IndexWriter indexWriter = new IndexWriter(mergedIndex, config);	

		//int fileNumber = subIndexes.list().length;
		Directory[] indexesArray = new Directory[portionNumber];
		for(int i = 0; i < portionNumber; i++)
		{
			String currentFileName = subIndexesPrefix+"-"+(i+1);
			System.out.println(currentFileName);
			indexesArray[i] = FSDirectory.open(new File(currentFileName));
		}
		
		long startTime = System.currentTimeMillis();
		System.out.println("Starting to merge "+ portionNumber + " indexes.");
		indexWriter.addIndexes(indexesArray);
		indexWriter.close();
		mergePolicy.close();
		long endTime = System.currentTimeMillis();
		System.out.println("Totally "+(endTime-startTime)/1000 +" seconds used to merge indexes.");
	}
	
	public static void main(String[] args)
	{
//		String subIndexesPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB";
//		String mergedIndexPath = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB_Merged";
		String subIndexesPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB_New_betterAlias/LuceneDB";
		String mergedIndexPath = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB_New_betterAlias/LuceneDB_merged";
		//FSDirectory.open(new File(LuceneIndexDir));
		try
		{
			mergeIndex(subIndexesPrefix,mergedIndexPath,64);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
