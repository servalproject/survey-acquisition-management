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

public class WeatherReading extends SensorReading implements Comparable<WeatherReading> {
	
	/*
	 * private class level variables
	 */
	private int barometricTrend = 0;
	private float barometer = 0;
	private float temperature = 0;
	private int humidity = 0;
	private float windSpeed = 0;
	private float averageWindSpeed = 0;
	private int windDirection = 0;
	private float rainRate = 0;
	private float rainToday = 0;
	
	/**
	 * construct a new WeatherReading with the current time as the time stamp
	 */
	public WeatherReading() {
		super();
	}
	
	/**
	 * construct a new WeatherReading with the supplied time stamp
	 * @param timestamp
	 */
	public WeatherReading(long timestamp) {
		super(timestamp);
	}
	
	/*
	 * get and set methods
	 */
	
	/**
	 * get the barometric trend value
	 * -60 = Falling Rapidly, -20 = Falling Slowly, 0 = Steady, 20 = Rising Slowly, 60 = Rising Rapidly
	 * 
	 * @return the barometricTrend
	 */
	public int getBarometricTrend() {
		return barometricTrend;
	}

	/**
	 * get the barometer reading in hPa
	 * 
	 * @return the barometer
	 */
	public float getBarometer() {
		return barometer;
	}

	/**
	 * get the temperature in °C
	 * 
	 * @return the temperature
	 */
	public float getTemperature() {
		return temperature;
	}

	/**
	 * get the relative humidity reading
	 * 
	 * @return the humidity
	 */
	public int getHumidity() {
		return humidity;
	}

	/**
	 * get the wind speed in Kph
	 * 
	 * @return the windSpeed
	 */
	public float getWindSpeed() {
		return windSpeed;
	}

	/**
	 * get the average wind speed calculated over the past 10 minutes
	 * 
	 * @return the averageWindSpeed
	 */
	public float getAverageWindSpeed() {
		return averageWindSpeed;
	}

	/**
	 * get the direction of the wind 
	 * (0° is no wind data, 90° is East, 180° is South, 270° is West and 360° is north)
	 * 
	 * @return the weatherDirection
	 */
	public int getWindDirection() {
		return windDirection;
	}

	/**
	 * get the current rate of falling rain in mm
	 * 
	 * @return the rainRate
	 */
	public float getRainRate() {
		return rainRate;
	}

	/**
	 * get the current amount of rain that has fallen today in mm
	 * 
	 * @return the rainToday
	 */
	public float getRainToday() {
		return rainToday;
	}

	/**
	 * set the barometric trend
	 * 
	 * @param barometricTrend the barometricTrend to set
	 */
	public void setBarometricTrend(int barometricTrend) {
		this.barometricTrend = barometricTrend;
	}

	/**
	 * set the barometer reading in hPa
	 * 
	 * @param barometer the barometer reading to set
	 */
	public void setBarometer(float barometer) {
		this.barometer = barometer;
	}

	/**
	 * set the temperature in °C
	 * 
	 * @param temperature the temperature to set
	 */
	public void setTemperature(float temperature) {
		this.temperature = temperature;
	}

	/**
	 * the relative humidity value to set
	 * 
	 * @param humidity the humidity to set
	 */
	public void setHumidity(int humidity) {
		this.humidity = humidity;
	}

	/**
	 * the wind speed in Kph
	 * 
	 * @param windSpeed the windSpeed to set
	 */
	public void setWindSpeed(float windSpeed) {
		this.windSpeed = windSpeed;
	}

	/**
	 * the average wind speed over the past 10 minutes
	 * 
	 * @param averageWindSpeed the averageWindSpeed to set
	 */
	public void setAverageWindSpeed(float averageWindSpeed) {
		this.averageWindSpeed = averageWindSpeed;
	}

	/**
	 * set the direction of the wind
	 * @param windDirection the weatherDirection to set
	 */
	public void setWindDirection(int windDirection) {
		
		if(windDirection < 0 || windDirection > 360) {
			throw new IllegalArgumentException("the wind direction must be between 0 and 360");
		}
		
		this.windDirection = windDirection;
	}

	/**
	 * set the rate of rain currently falling in mm
	 * 
	 * @param rainRate the rainRate to set
	 */
	public void setRainRate(float rainRate) {
		this.rainRate = rainRate;
	}

	/**
	 * set the amount of rain that has fallen today in mm
	 * @param rainToday the rainToday to set
	 */
	public void setRainToday(float rainToday) {
		this.rainToday = rainToday;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		StringBuilder mBuilder = new StringBuilder();
		
		mBuilder.append(this.getTimestamp() + "\t");
		mBuilder.append(barometricTrend + "\t");
		mBuilder.append(barometer + "\t");
		mBuilder.append(temperature + "\t");
		mBuilder.append(humidity + "\t");
		mBuilder.append(windSpeed + "\t");
		mBuilder.append(averageWindSpeed + "\t");
		mBuilder.append(windDirection + "\t");
		mBuilder.append(rainRate + "\t");
		mBuilder.append(rainToday + "\t");
		
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
		if((o instanceof WeatherReading) == false) {
			return false;
		}
		
		// compare the two objects
		WeatherReading reading = (WeatherReading) o;
		
		if(reading.getTimestamp() == this.getTimestamp()) {
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
	public int compareTo(WeatherReading another) {
		
		if(this.getTimestamp() == another.getTimestamp()) {
			return 0;
		} else {
			return (int) (this.getTimestamp() - another.getTimestamp());
		}
	}

}
