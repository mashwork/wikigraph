package com.mashwork.wikipedia.ParallelXML.ParallelLucene;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is a parallel controller. Several indexBuilder thread will run in parallel.
 * 
 */
public class BuildLuceneIndex implements Runnable//extends Thread
{
	static String DumpDirPrefix;
	static String LuceneIndexDirPrefix;
	int counter = 1;
	static int totalPortionNumbers;
	
	public BuildLuceneIndex(String DumpDirPrefix,String LuceneIndexDirPrefix, int totalPortionNumbers,int counter) throws Exception
	{
		BuildLuceneIndex.DumpDirPrefix = DumpDirPrefix;
		BuildLuceneIndex.LuceneIndexDirPrefix = LuceneIndexDirPrefix;
		BuildLuceneIndex.totalPortionNumbers = totalPortionNumbers;
		this.counter = counter;
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
			//addCounter();
		}
		try
		{
			execute(specificDumpDir,specificLuceneIndexDir,Id);
		}catch(Exception e)
		{
			System.out.println("Build Lucene Error!!!");
		}
	}
	
//	public synchronized void addCounter()
//	{
//		counter++;
//	}
	
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
	
	//the param id will indicate which part of dump it is.
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
		if (args.length < 4) {
		      System.out.println("USAGE: ExtractLinks <preprocessed-xml-file> <DBDir> <numberOfThreads> <numberOfPortions>");
		      System.exit(255);
		      }
		
		//String DumpDirPrefix = "/Volumes/Seagate Backup Plus Drive/Wikipedia/whileWiki64/wholeWiki";
		//String LuceneIndexDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/DataBase/LuceneDB_New_betterAlias/LuceneDB2";
		
		String DumpDirPrefix = args[0];
		String LuceneIndexDirPrefix = args[1];
		int numThread = Integer.parseInt(args[2]);
		int numPortion = Integer.parseInt(args[3]);
		
		
		ExecutorService service = Executors.newFixedThreadPool(numThread);
		for(int i = 0;i < numPortion;i++)
		{
			System.out.println("Starting part "+(i+1));
			Runnable thread = new BuildLuceneIndex(DumpDirPrefix,LuceneIndexDirPrefix,numPortion,i+1);
			service.execute(thread);
			//service.wait(200);
		}
		service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        System.out.println("all thread complete");
	}
}
