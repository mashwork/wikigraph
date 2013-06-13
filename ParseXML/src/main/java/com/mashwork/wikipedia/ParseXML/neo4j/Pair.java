package com.mashwork.wikipedia.ParseXML.neo4j;
public class Pair<A,B> {
	public A first;
	public B second;
	public Pair(A first,B second)
	{
		this.first = first;
		this.second = second;
	}
	public A getFirst()
	{
		return this.first;
	}
	public B getSecond()
	{
		return this.second;
	}
}
