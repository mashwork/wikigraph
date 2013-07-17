package com.mashwork.wikipedia.ParallelXML;

import java.io.FileOutputStream;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

//import com.mashwork.wikipedia.ParseXML.MyCallBackHandler;

import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

public class ParallelAnalyzer extends Thread
{
	static String DumpDirPrefix;
	static String XMLDirPrefix;
	static int counter = 1;
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
			addCounter();
		}
		execute(specificDumpDir,specificXMLDir,Id);
	}
	
	public synchronized void addCounter()
	{
		counter++;
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
        	XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        	XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(specificXMLDir), "UTF-8");
            writer.writeStartDocument();
            writer.writeStartElement("d");
        	
            ParallelHandler MBH = new ParallelHandler(writer,782367/8,Id);
            
            wxsp.setPageCallback(MBH);
                
            long startTime = System.currentTimeMillis();
            
            wxsp.parse();

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
            
            long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
            
            //System.out.println("Totally "+ParallelAnalyzer.pageCount+" pages  crawled. Totally "
            		//+ParallelAnalyzer.linkCount+" links crawled.");
            System.out.println("For portion "+Id+":totally "+elapsedSeconds+" seconds used.");
        }catch(Exception e) {
                e.printStackTrace();
        }
	}
	
	public ParallelAnalyzer(String DumpDirPrefix,String XMLDirPrefix, int totalPortionNumbers)
	{		
		ParallelAnalyzer.DumpDirPrefix = DumpDirPrefix;
		ParallelAnalyzer.XMLDirPrefix = XMLDirPrefix;
		ParallelAnalyzer.totalPortionNumbers = totalPortionNumbers;
	}
	public static void main(String[] args)
	{
		String PortionDumpDirPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Poriton/GOT_D3_Pages";
		String XMLoutputPrefix = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/GOT_D3_Poriton/GOT_D3_Pages";
		
		ParallelAnalyzer parallelAnalyzer = new ParallelAnalyzer(PortionDumpDirPrefix,XMLoutputPrefix,8);
		Thread AnalyzerThread1 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread2 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread3 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread4 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread5 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread6 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread7 = new Thread(parallelAnalyzer);
		Thread AnalyzerThread8 = new Thread(parallelAnalyzer);
		
		AnalyzerThread1.start();
		AnalyzerThread2.start();
		AnalyzerThread3.start();
		AnalyzerThread4.start();
		AnalyzerThread5.start();
		AnalyzerThread6.start();
		AnalyzerThread7.start();
		AnalyzerThread8.start();
	}

}
