package com.example.assettracking.service.BL;

import java.util.concurrent.TimeUnit;


public interface CustomCacheServiceBL {

	
	public void put(String key, Object value, long ttl, TimeUnit unit) ;

    public Object get(String key);

    public void delete(String key) ;
}
