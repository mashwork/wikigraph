package com.mashwork.wikipedia.ParallelXML;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class DumpDevider
{
	BufferedReader BR;
	String page;
	String output;
	FileWriter FW;

	protected static int total = 13539091;			//13539091	//782367
	int portionSize;
	int portion;
	int portionCounter = 1;
	
	public DumpDevider(String dir, String output,int portion) throws IOException
	{
		FileInputStream FS = new FileInputStream(dir);
		InputStreamReader SR = new InputStreamReader(FS);
		this.BR = new BufferedReader(SR);
		this.portionSize = getPortinSize(portion);
		this.portion = portion;
		this.output = output;
	}
	
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
		String dir = "/Users/Ricky/mashwork/wikidump/new/enwiki-20130604-pages-articles.xml";
		String output = "/Users/Ricky/mashwork/wikiXmlParser/crawledXML/wholeWiki/wholeWiki.xml";
		int portion = 8;
		DumpDevider dumpDevider = new DumpDevider(dir,output,portion);
		dumpDevider.devide();
	}
}
