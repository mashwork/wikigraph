package com.mashwork.wikipedia.ParallelXML.ParallelLucene;


import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class BuildLuceneIndex extends Thread
{
	static String DumpDirPrefix;
	static String LuceneIndexDirPrefix;
	static int counter = 1;
	static int totalPortionNumbers;
	
	public BuildLuceneIndex(String DumpDirPrefix,String LuceneIndexDirPrefix, int totalPortionNumbers) throws Exception
	{
		BuildLuceneIndex.DumpDirPrefix = DumpDirPrefix;
		BuildLuceneIndex.LuceneIndexDirPrefix = LuceneIndexDirPrefix;
		BuildLuceneIndex.totalPortionNumbers = totalPortionNumbers;
	}
	
	public void run()
	{
		String specificDumpDir;
		String specificLuceneIndexDir;
		int Id;
		synchronized(this)
		{
			specificDumpDir = getSpecificDumpDir();
			System.out.println(specificDumpDir);
			specificLuceneIndexDir = getSpecificLuceneIndexDir();
			System.out.println(specificLuceneIndexDir);
			Id = getCounter();
			addCounter();
		}
		try
		{
			execute(specificDumpDir,specificLuceneIndexDir,Id);
		}catch(Exception e)
		{
			System.out.println("Build Lucene Error!!!");
		}
	}
	
	public synchronized void addCounter()
	{
		counter++;
	}
	
	public synchronized String getSpecificDumpDir()
	{
		return DumpDirPrefix + "-"+ counter +".xml";
	}
	
	public synchronized String getSpecificLuceneIndexDir()
	{
		return LuceneIndexDirPrefix + "-"+ counter;
	}
	
	public synchronized int getCounter()
	{
		return counter;
	}
	
	public void execute(String DumpDir,String LuceneIndexDir,int Id) 
	{
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(DumpDir);
		
		FSDirectory dir = null;
		
		try
		{
			dir = FSDirectory.open(new File(LuceneIndexDir));
		}catch(IOException e)
		{
			System.out.println("Can not open index dir! " + e);
		}
		
		//StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		WikiAnalyzer wikiAnalyzer = new WikiAnalyzer();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,wikiAnalyzer);
		IndexWriter indexWriter = null;
		try
		{
			indexWriter = new IndexWriter(dir, config);
		}catch(IOException e)
		{
			System.out.println("Initialize lucene writer error! "+e);
		}
		
		ParallelIndexBuilder PIB = new ParallelIndexBuilder(indexWriter,Id);
		try
		{
			wxsp.setPageCallback(PIB);
		}catch(Exception e)
		{
			System.out.println("Set page callback handler error! "+e);
		}
		
		long startTime = System.currentTimeMillis();
		System.out.println("Creating lucene.");
		
		try
    	{
			wxsp.parse();
    	}catch(Exception e)
    	{
    		System.out.println("parse() error! "+e);
    		e.printStackTrace();
    	} 
		
		try
    	{
			indexWriter.close();
    	}catch(Exception e)
    	{
    		System.out.println("Can not close index writer! "+e);
    		e.printStackTrace();
    	}
    	System.out.println("Totally  " + PIB.docCount+" documents has been processed.");
    	long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("Totally "+elapsedSeconds+" seconds used to build lucene text into neo4j.");
	}
	
	public static void main(String[] args) throws Exception
	{
		String DumpDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki";
		String LuceneIndexDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/LuceneDB";
//		String DumpDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki-1-8/wholeWiki-1";
//		String LuceneIndexDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB/test/LuceneDB";
		
		BuildLuceneIndex buildLuceneIndex = new BuildLuceneIndex(DumpDirPrefix,LuceneIndexDirPrefix,8);
		
		Thread builder1 = new Thread(buildLuceneIndex);
		Thread builder2 = new Thread(buildLuceneIndex);
		Thread builder3 = new Thread(buildLuceneIndex);
		Thread builder4 = new Thread(buildLuceneIndex);
		Thread builder5 = new Thread(buildLuceneIndex);
		Thread builder6 = new Thread(buildLuceneIndex);
		Thread builder7 = new Thread(buildLuceneIndex);
		Thread builder8 = new Thread(buildLuceneIndex);
		
		
		builder1.start();
		builder2.start();
		builder3.start();
		builder4.start();
		builder5.start();
		builder6.start();
		builder7.start();
		builder8.start();
	}
}
