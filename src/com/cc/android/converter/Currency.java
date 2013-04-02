package com.cc.android.converter;


public class Currency {
	public String name, code, symbol, country;
	public boolean active;

	//TODO: add symbol property
	
	public Currency (String name, String code, String symbol, String country, boolean active)
    {
    	this.name = name;
    	this.code = code;
    	this.country = country;
    	this.symbol = symbol;
    	this.active = active;
    }
    
    
    
}