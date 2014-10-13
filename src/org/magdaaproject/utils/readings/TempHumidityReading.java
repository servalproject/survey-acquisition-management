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
 * a class to represent a temperature and humidity reading
 */
public class TempHumidityReading extends SensorReading implements Comparable<TempHumidityReading> {

	/*
	 * private class level variables
	 */
	private float temp;
	private float humidity;
	
	/**
	 * construct a new TempHumidityReading object using the current time as the timestamp
	 * 
	 * @param temp the temperature reading
	 * @param humidity the humidity reading
	 */
	public TempHumidityReading(float temp, float humidity) {
		
		super();
		this.temp = temp;
		this.humidity = humidity;
		
	}
	
	/**
	 * construct a new TempHumidityReading object
	 * 
	 * @param timestamp the timestamp of when the reading occurred
	 * @param temp the temperature reading
	 * @param humidity the humidity reading
	 */
	public TempHumidityReading(long timestamp, float temp, float humidity) {
		
		super(timestamp);
		this.temp = temp;
		this.humidity = humidity;
		
	}
	
	/*
	 * get methods
	 */

	/**
	 * @return return the humidity value for this reading
	 */
	public float getHumidity() {
		return humidity;
	}

	/**
	 * @return the temperature value for this reading
	 */
	public float getTemp() {
		return temp;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		StringBuilder mBuilder = new StringBuilder();
		
//		mBuilder.append("timestamp: ");
//		mBuilder.append(this.getTimestamp() + ",");
//		
//		mBuilder.append("temperature: ");
//		mBuilder.append(temp + "°C,");
//		
//		mBuilder.append("relative humidity: ");
//		mBuilder.append(humidity + "%");
		
		mBuilder.append(this.getTimestamp() + "\t");
		mBuilder.append(temp + "\t");
		mBuilder.append(humidity + "\t");
		
		return mBuilder.toString();
	}
	
	/*
     * methods required for participating in collections
     */
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		// check to make sure that the object is the same
		if((o instanceof TempHumidityReading) == false) {
			return false;
		}
		
		// compare the two objects
		TempHumidityReading reading = (TempHumidityReading) o;
		
		if(this.getTimestamp() == reading.getTimestamp() && this.getTemp() == reading.getTemp() && this.getHumidity() == reading.getHumidity()) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Long.toString(this.getTimestamp()).hashCode();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TempHumidityReading another) {
		
		if(this.getTimestamp() == another.getTimestamp()) {
			return 0;
		} else {
			return (int) (this.getTimestamp() - another.getTimestamp());
		}
	}

}
