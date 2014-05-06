/*
 * Copyright (C) 2012, 2013 The MaGDAA Project
 *
 * This file is part of the MaGDAA Library Software
 *
 * MaGDAA Library Software is free software; you can redistribute it and/or modify
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
package org.magdaaproject.utils.readings;

import java.io.IOException;
import java.util.LinkedList;

import org.magdaaproject.utils.FileUtils;

/**
 * a list of sensor readings that automatically restricts the number
 * of items in the list to preset maximum
 */
public class ReadingsList extends LinkedList<SensorReading> {
	
	/*
	 * public class level constants
	 */
	
	/**
	 * default item limit for a readings list
	 */
	public static final int DEFAULT_LIMIT = 20;

	/*
	 * private class level constants
	 */
	private static final long serialVersionUID = 3667976620012107777L;
	
	/*
	 * private class level variables
	 */
	private int limit;
	
	/**
	 *construct a new readings list with the default item limit
	 */
	public ReadingsList() {
		super();
		this.limit = DEFAULT_LIMIT;
	}
	
	/**
	 * construct a new readings list with the specified item limit
	 * 
	 * @param limit the number maximum items allowed in the list
	 */
	public ReadingsList(int limit) {
		super();
		this.limit = limit;
	}
	
	/**
	 * add an item to the ReadingsList and remove any old items
	 */
	@Override
	public boolean add(SensorReading item) {
		
		// add the new item
		super.add(item);
		
		// remove any items over the limit
		while (super.size() > limit) { 
			super.remove(); 
		}
		
		return true;
	}
	
	/**
	 * remove readings older than the minimum age
	 * 
	 * @param minAge the minimum age for readings to remain in the list
	 */
	public void removeOld(long minAge) {
		
		SensorReading mReading;
		
		// loop through all of the objects
		for(int i = 0; i < this.size(); i++) {
			
			// get the next object in the list
			mReading = this.get(i);
			
			if(mReading.getTimestamp() < minAge) {
				this.remove(i);
				i--;
			}	
		}
	}
	
	/**
	 * dump the data contained in this list to a file
	 * 
	 * @param directory the directory used to store the file
	 * @return the full path of the file containing the data
	 * @throws IOException if something bad happens
	 */
	public String dumpData(String directory) throws IOException {
		
		// loop through all of the objects building the contents of the file
		StringBuilder mBuilder = new StringBuilder();
		SensorReading mReading;
		
		for(int i = 0; i < this.size(); i++) {
			mReading = this.get(i);
			
			mBuilder.append(mReading.toString() + "\n");
		}
		
		// save the output to a temporary file
		return FileUtils.writeTempFile(mBuilder.toString(), directory);
	}
}
