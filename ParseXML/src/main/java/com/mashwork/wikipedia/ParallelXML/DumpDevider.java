package com.mashwork.wikipedia.ParallelXML;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;


/**
 * @author  Jiali Huang
 *			Computer Science Department, 
 *			Courant Institute Mathematical Sciences, NYU
 * @time	2013-8
 * @comment	This class is used for dividing the original wikidump into <code>portion</code> parts. So that later on this
 * 			files can be processed in parallel.
 */
public class DumpDevider
{
	BufferedReader BR;
	String page;
	String output;
	FileWriter FW;

	protected static int total = 30346;			//13539091	//782367	//813394TSBD2
	int portionSize;
	int portion;
	int portionCounter = 1;
	
	/**
	 * @param dir	the input file dir.
	 * @param output the output file dir.
	 * @param portion	the number of parts you want to divide into.
	 * @param totalPageNumber	the total page number. For wiki 2012-06-04 dump, there are 13539091 pages
	 * @throws IOException
	 */
	public DumpDevider(String dir, String output,int portion,int totalPageNumber) throws IOException
	{
		FileInputStream FS = new FileInputStream(dir);
		InputStreamReader SR = new InputStreamReader(FS);
		this.BR = new BufferedReader(SR);
		this.portionSize = getPortinSize(portion);
		this.portion = portion;
		this.output = output;
		DumpDevider.total = totalPageNumber;
	}
	
	/**
	 * @param output	your input dir.
	 * @return		the sequential output file names.
	 * if the output dir is aaa.xml, the returned value will be aaa-1.xml, aaa-2.xml and ect.
	 */
	public String getOutPutName(String output)
	{
		String result = output.split("\\.")[0];
		return result+"-"+portionCounter+".xml";
	}
	
	public int getPortinSize(int portion)
	{
		return total/portion;
	}
	
	public String returnPage()
	{
		return page;
	}
	
	public String getHead()
	{
		return "<mediawiki>\n";
	}
	
	public String getEnd()
	{
		return "</mediawiki>\n";
	}
	
	public void write(int portionCounter) throws IOException
	{
		String line;
		StringBuilder SB = new StringBuilder();
		int counter = 0;
		FW.write(getHead());
		while((line = BR.readLine())!=null && counter < portionSize)	// && limit-- >0
		{		
			if(line.contains("<page>"))			
			{
				counter++;
				//System.out.println("Counter: "+counter+" Total/100: " + total/100);
				DecimalFormat df = new DecimalFormat("0.00");
				double percentage = ((double)counter/portionSize*100);
				if(counter%(portionSize/100) == 0 )
				{
					FW.flush();
					System.out.println("Processing: portion "+portionCounter +"  "+df.format(percentage)+"% ");
				}

				SB = new StringBuilder();
				SB.append(line+"\n");
				while(!(line = BR.readLine()).contains("</page>"))
				{
					SB.append(line+"\n");
				}
				SB.append(line+"\n");
				page = SB.toString();
				//System.out.println(page);
				FW.write(page);
			}
		}
		FW.write(getEnd());
		FW.flush();
		FW.close();
	}
	
	public void devide() throws IOException
	{
		while(portionCounter<=portion)
		{
			this.FW = new FileWriter(getOutPutName(output));
			write(portionCounter);
			portionCounter++;
		}
	}
	
	public static void main(String[] args) throws IOException
	{
      if (args.length < 4) {
      System.out.println("USAGE: ExtractLinks <input-file> <output-file> <portion> <totalPageNumber>");
      System.exit(255);
      }
//		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
//		String output = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki.xml";
//		String dir = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/new/TSB_D1_Pages.xml";
//		String output = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/TSB/TSB_D1/TSB_D1.xml";
//		int portion = 64;
		
		String dir = args[0];
		String output = args[1];
		int portion = Integer.parseInt(args[2]);
		int totalPageNumber = Integer.parseInt(args[3]);
		
		DumpDevider dumpDevider = new DumpDevider(dir,output,portion,totalPageNumber);
		dumpDevider.devide();
	}
}
