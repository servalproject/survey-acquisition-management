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

/**
 * an abstract class to represent a sensor reading
 */
public class SensorReading {
	
	/*
	 * private class level variables
	 */
	private long timestamp;
	
	/**
	 * instantiate a new SensorReading object using the current time as the timestamp
	 */
	public SensorReading() {
		this.timestamp = System.currentTimeMillis();
	}
	
	/**
	 * instantiate a new SensorReading object specifying when it was created
	 * @param timestamp the timestamp of when the object was created
	 */
	public SensorReading(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the timestamp of when this sensor reading occurred
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * set the timestamp of when this sensor reading occurred
	 * @param timestamp the timestamp of when this sensor reading occurred
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
