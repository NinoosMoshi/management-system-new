package com.ninos.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Service
public class LoginAttemptService {
	private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;
	private static final int ATTEMPT_INCREMENT = 1;
	private LoadingCache<String, Integer> loginAttemptCache;     // LoadingCache<user1, number of attempt>
	
	// initialize cache from google guava
	public LoginAttemptService() {
		super();
		loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(1,TimeUnit.MINUTES)   // before you finish from your attempt, you will wait 1 minutes to reset from the beginner of attempt
				.maximumSize(100).build(new CacheLoader<String, Integer>(){                   // number of the user is 100
					public Integer load(String key) {
						return 0;
					}
				});
	}
	
	// remove a user from the cache
	public void evictUserFromLoginAttemptCache(String username) {
        loginAttemptCache.invalidate(username);                    // will find a key(username) and remove it from the cache (remove key with the value)
    }
	
	
	// count a number of attempt for each user
	 public void addUserToLoginAttemptCache(String username) {
	        int attempts = 0;
	            try {
					attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);  // get user and increase his attempt by 1
				} catch (ExecutionException e) {
					e.printStackTrace();
				}   
	            loginAttemptCache.put(username, attempts);              // save a user with his attempt in cache
	    }
	 
	 
	 // return true or false, check the maximum of attempt 
	  public boolean hasExceededMaxAttempts(String username) {
	            try {
					return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				return false;
	      }
	
	
	
	

}
