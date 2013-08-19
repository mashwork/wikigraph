package com.mashwork.wikipedia.ParallelXML;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//import javax.xml.stream.XMLOutputFactory;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	
 * This class is used to preprocess the wikiDump. After we use the DumpDevider to devide the dump into several parts.
 * We can use this class to preprocess the file in parallel. All the dump files will be converted into simplefied
 * Page-TabelOfContent-Link xml. The xml file will be in "xxx-#-structure.xml"
 */
public class ParallelAnalyzer implements Runnable//extends Thread
{
	static String DumpDirPrefix;
	static String XMLDirPrefix;
	int counter;
	static int totalPortionNumbers;
	public void run()
	{
		String specificDumpDir;
		String specificXMLDir;
		int Id;
		synchronized(this)
		{
			specificDumpDir = getSpecificDumpDir();
			System.out.println(specificDumpDir);
			specificXMLDir = getSpecificXMLDir();
			System.out.println(specificXMLDir);
			Id = getCounter();
			//addCounter();
		}
		execute(specificDumpDir,specificXMLDir,Id);
	}
	
	
	public synchronized String getSpecificDumpDir()
	{
		return DumpDirPrefix + "-"+ counter +".xml";
	}
	
	public synchronized String getSpecificXMLDir()
	{
		return XMLDirPrefix + "-"+ counter +"-structure.xml";
	}
	
	public synchronized int getCounter()
	{
		return counter;
	}
	
	public void execute(String specificDumpDir,String specificXMLDir, int Id)
	{
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(specificDumpDir);
		try {
        	//XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        	
        	//BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(specificXMLDir));
        	BufferedWriter bufferedWriter = new BufferedWriter(
        			new OutputStreamWriter(new FileOutputStream(specificXMLDir),"UTF-8"));
        	bufferedWriter.write("<?xml version=\"1.0\" ?>");
        	bufferedWriter.write("<d>");
        	           
            ParallelHandler MBH = new ParallelHandler(bufferedWriter,13539091/64,Id);
            
            wxsp.setPageCallback(MBH);
                
            long startTime = System.currentTimeMillis();
            
            wxsp.parse();
           
            bufferedWriter.write("</d>");
            bufferedWriter.flush();
            bufferedWriter.close();
            //bufferedWriter.write(" </mediawiki>");
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            
            System.out.println("For portion "+Id+":totally "+elapsedSeconds+" seconds used.");
        }catch(Exception e) {
                e.printStackTrace();
        }
	}
	
	public ParallelAnalyzer(String DumpDirPrefix,String XMLDirPrefix, int totalPortionNumbers,int counter)
	{		
		ParallelAnalyzer.DumpDirPrefix = DumpDirPrefix;
		ParallelAnalyzer.XMLDirPrefix = XMLDirPrefix;
		ParallelAnalyzer.totalPortionNumbers = totalPortionNumbers;
		this.counter = counter;
	}
	public static void main(String[] args)
	{
		
		if (args.length < 4) {
		      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <numberOfThreads> <numberOfPortions>");
		      System.exit(255);
		      }
		
//		String PortionDumpDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki64/wholeWiki";
//		String XMLoutputPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki64Structure/wholeWiki";
		
		String PortionDumpDirPrefix = args[0];
		String XMLoutputPrefix = args[1];
		int numThread = Integer.parseInt(args[2]);
		int numPortions = Integer.parseInt(args[3]);
		
		ExecutorService service = Executors.newFixedThreadPool(numThread);
		for(int i = 1;i <= numPortions;i++)
		{
			//System.out.println("Starting part "+(i+1));
			
			Runnable thread = new ParallelAnalyzer(PortionDumpDirPrefix,XMLoutputPrefix,numPortions,i);
			service.execute(thread);

		}
		service.shutdown();
        try
		{
			service.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e)
		{
			System.out.println("Termination error!");
			e.printStackTrace();
		}
        System.out.println("all thread complete");
	}

}
