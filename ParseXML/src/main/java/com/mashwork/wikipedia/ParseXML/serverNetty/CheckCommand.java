package com.mashwork.wikipedia.ParseXML.serverNetty;

import java.util.List;
import java.util.Map;

public class CheckCommand
{
	public static String isErrorCommand(String code, Map<String, List<String>> command)
	{
		if(code.contains("1"))
		{
			if(command.get("startNode").get(0)==null)
			{
				return errorCommand(1);
			}
		}
		if(code.contains("2"))
		{
			if(command.get("endNode").get(0)==null)
			{
				return errorCommand(2);
			}
		}
		if(code.contains("3"))
		{
			if(command.get("maxDepth").get(0)==null  ||  Integer.parseInt(command.get("maxDepth").get(0))<0)
			{
				return errorCommand(3);
			}
		}
		if(code.contains("4"))
		{
			if(!command.get("isPageOnly").get(0).equals("true") || !command.get("isPageOnly").get(0).equals("false"))
			{
				return errorCommand(4);
			}
		}
		if(code.contains("5"))
		{
			if(command.get("listLength").get(0)==null || Integer.parseInt(command.get("listLength").get(0))<0)
			{
				return errorCommand(5);
			}
		}
		if(code.contains("6"))
		{
			if(command.get("nodeList").size()!=Integer.parseInt(command.get("listLength").get(0)))
			{
				return errorCommand(6);
			}
		}
		
		return null;
		
	}
	
	public static String errorCommand(int type)
	{
		if(type==0)
		{
			return "Can not find such method!\n";
		}
		else if(type==1)
		{
			return "Missing startNode!\n";
		}
		else if(type==2)
		{
			return "Missing endNode!\n";
		}
		else if(type==3)
		{
			return "maxDepth error!";
		}
		else if(type==4)
		{
			return "List legnth error!";
		}
		else if(type==5)
		{
			return "List legnth does not exist or is less than 0!";
		}
		else if(type==6)
		{
			return "List legnth does not match param number!";
		}
		else
		{
			return "No such error code!";
		}
	}
}
