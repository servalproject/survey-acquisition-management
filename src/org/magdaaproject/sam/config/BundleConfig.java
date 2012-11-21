/*
 * Copyright (C) 2012 The MaGDAA Project
 *
 * This file is part of the MaGDAA SAM Software
 *
 * MaGDAA SAM Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.magdaaproject.sam.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

import android.text.TextUtils;

/**
 * a class used to represent a bundle config
 */
public class BundleConfig {
	
	/*
	 * private class level variables
	 */
	private HashMap<String, String> metadata;
	private ArrayList<String> forms;
	private String rawConfig;
	
	/**
	 * parse a the string representation of a config and construct an object from it
	 * @param config the config to parse
	 * @throws ConfigException if the config parameter is empty
	 */
	public BundleConfig(String config) throws ConfigException {
		
		// check on the parameters
		if(TextUtils.isEmpty(config) == true) {
			throw new ConfigException("a raw config text is required");
		}
		
		metadata = new HashMap<String, String>();

		forms      = new ArrayList<String>();
		
		this.rawConfig = config;
	}
	
	/**
	 * parse the text of the config into an object
	 * @throws ConfigException
	 */
	public void parseConfig() throws ConfigException {
		
		// use a scanner to iterate over the text
		Scanner mScanner = new Scanner(rawConfig);
		String mToken;
		
		// loop through all of the lines
		while (mScanner.hasNextLine()) {
			// get the text
			try {
				mToken = mScanner.nextLine();
				
				// parse the command lines
				if(mToken.startsWith("@") == true) {
					if(mToken.startsWith("@form") == true) {
						mToken = mToken.substring(mToken.indexOf("\t")  + 1, mToken.length());
						forms.add(mToken.trim());
					} else {
						metadata.put(
							mToken.substring(1, mToken.indexOf("\t")).trim(), 
							mToken.substring(mToken.indexOf("\t") + 1, mToken.length()).trim()
						);
					}
				}
			} catch (NoSuchElementException e) {
				throw new ConfigException("unable to parse the config", e);
			}
		}
	}
	
	/**
	 * get the metadata value matching the key
	 * @param key the metadata key to match against
	 * @return the metadata value or null if the key cannot be found
	 */
	public String getMetadataValue(String key) {
		if(TextUtils.isEmpty(key)) {
			return null;
		}
		return metadata.get(key);
	}
	
	/**
	 * get the list of forms
	 * @return the list of forms as a string array
	 */
	public String[] getForms() {
		return (String[]) forms.toArray();
	}
	
	/**
	 * validate the parsed configuration 
	 * 
	 * @throws ConfigException if the configuration doesn't validate
	 */
	public void validateConfig() throws ConfigException {
		
		if(forms.size() == 0) {
			throw new ConfigException("The config requires at least one form definition");
		}
		
		if(metadata.containsKey("title") == false) {
			throw new ConfigException("The config requires the 'title' metadata element");
		}
		
		if(metadata.containsKey("description") == false) {
			throw new ConfigException("The config requires the 'description' metadata element");
		}
		
		if(metadata.containsKey("version") == false) {
			throw new ConfigException("The configu requires the 'version' metadata element");
		}
		
		if(metadata.containsKey("author") == false) {
			throw new ConfigException("The configu requires the 'author' metadata element");
		}
		
		if(metadata.containsKey("email") == false) {
			throw new ConfigException("The configu requires the 'email' metadata element");
		}
		
		if(metadata.containsKey("generated") == false) {
			throw new ConfigException("The configu requires the 'generated' metadata element");
		}	
	}
}
