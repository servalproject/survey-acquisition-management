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
package org.magdaaproject.utils;

/**
 * a utility class which exposes utility methods for converting values between different
 * scales
 */
public class UnitConversionUtils {
	
	/**
	 * constant to define the celsius temperature scale
	 */
	public static final int CELSIUS = 0;
	
	/**
	 * constant to define the fahrenheit scale
	 */
	public static final int FAHRENHEIT = 1;
	
	/**
	 * constant to define the kelvin temperature scale
	 */
	public static final int KELVIN = 2;
	
	/**
	 * constant to define the kilometers per hour speed scale
	 */
	public static final int KPH = 10; 
	
	/**
	 * constant to define the miles per hour speed scale
	 */
	public static final int MPH = 11;
	
	/**
	 * constant to define the hPa barometric scale
	 */
	public static final int HPA = 20;
	
	/**
	 * constant to define the hg inch barometric scale
	 */
	public static final int HG_INCH = 21;
	
	/**
	 * constant to define the millimetre scale
	 */
	public static final int MILLIMETRE = 30;
	
	/**
	 * constant to define the inch scale
	 */
	public static final int INCH = 31;
	
	
	/**
	 * convert a temperature value from one scale to another
	 * 
	 * @param value the temperature
	 * @param fromScale the scale to convert from, as defined by one of the temperature related constants in this class
	 * @param toScale the scale to convert to , as defined by one of the temperature related constants in this class
	 * @return the converted temperature
	 * @throws IllegalArgumentException of the fromScale or toScale parameters are invalid
	 */
	public static float comvertTemperature(float value, int fromScale, int toScale) {
		
		/*
		 * determine which conversion to undertake
		 */
		switch(fromScale) {
		case CELSIUS:
			// convert from celsius
			switch(toScale) {
			case FAHRENHEIT:
				// switch from celsius to fahrenheit
				return convertFromCelsiusToFahrenheit(value);
			case KELVIN:
				// switch from celsius to Kelvin
				return convertFromCelsiusToKelvin(value);
			default:
				throw new IllegalArgumentException("the toScale is invalid");
			}
		case FAHRENHEIT:
			// convert from FAHRENHEIT
			switch(toScale) {
			case CELSIUS:
				// convert from fahrenheit to celsius
				return convertFromFahrenheitToCelsius(value);
			case KELVIN:
				// convert from fahrenheit to kelvin
				return convertFromFahrenheitToKelvin(value);
			default:
				throw new IllegalArgumentException("the toScale is invalid");
			}
		case KELVIN:
			// convert from kelvin
			switch(toScale) {
			case CELSIUS:
				return convertFromKelvinToCelsius(value);
			case FAHRENHEIT:
				return convertFromKelvinToFahrenheit(value);
			default:
				throw new IllegalArgumentException("the toScale is invalid");
			}
		default:
			throw new IllegalArgumentException("the fromScale is invalid");
		}
	}
	
	private static float convertFromCelsiusToFahrenheit(float value) {
		return value * 9/5 + 32;
	}
	
	private static float convertFromCelsiusToKelvin(float value) {
		return value + 273.15f;
	}
	
	private static float convertFromFahrenheitToCelsius(float value) {
		return (value - 32) * 5/9;
	}
	
	private static float convertFromFahrenheitToKelvin(float value) {		
		return convertFromCelsiusToKelvin(convertFromFahrenheitToCelsius(value));
	}
	
	private static float convertFromKelvinToCelsius(float value) {
		return value - 273.15f;
	}
	
	private static float convertFromKelvinToFahrenheit(float value) {
		return convertFromCelsiusToFahrenheit(convertFromKelvinToCelsius(value));
	}
	
	/**
	 * convert a speed measurement from one scale to another
	 * @param speed the speed measurement to convert
	 * @param fromScale the from scale, as defined by one of the speed related constants in this class
	 * @param toScale the to scale, as defined by one of the speed related constants in this class
	 * @return the converted speed measurement
	 * @throws IllegalArgumentException if an invalid scale is provided
	 */
	public static float convertSpeed(float speed, int fromScale, int toScale) {
		switch(fromScale) {
		case KPH:
			// convert from kilometers per hour
			switch(toScale) {
			case MPH:
				return convertFromKphToMph(speed);
			default: 
				throw new IllegalArgumentException("invalid toScale");
			}
		case MPH:
			// convert from miles per hour
			switch(toScale) {
			case KPH:
				return convertFromMphToKph(speed);
			default:
				throw new IllegalArgumentException("invalid toScale");
			}
		default:
			throw new IllegalArgumentException("invalid fromScale");
		}
	}
	
	private static float convertFromMphToKph(float mph) {
		float conversionFactor = 1.609344f;
		return mph * conversionFactor;
	}
	
	private static float convertFromKphToMph(float kph) {
		float conversionFactor = 1.609344f;
		return kph / conversionFactor;
	}
	
	/**
	 * convert from one unit of barometric pressure to another
	 * @param pressure the pressure reading to convert
	 * @param fromScale the from scale, as defined by one of the pressure related constants in this class
	 * @param toScale the to scale, as defined by one of the pressure related constants in this class
	 * @return the converted pressure measurement
	 * @throws IllegalArgumentException if an invalid scale is provided
	 */
	public static float convertBarometricPressure(float pressure, int fromScale, int toScale) {
		switch(fromScale) {
		case HPA:
			// convert from hPa
			switch(toScale) {
			case HG_INCH:
				return convertFromHpaToHgInch(pressure);
			default:
				throw new IllegalArgumentException("invalid toScale");
			}
		case HG_INCH:
			// convert from hg inches
			switch(toScale) {
			case HPA:
				return convertFromHgInchToHpa(pressure);
			default:
				throw new IllegalArgumentException("invalid toScale");
			}
		default:
			throw new IllegalArgumentException("invalid fromScale");
		}
	}
	
	private static float convertFromHgInchToHpa(float hgInch) {
		float conversionFactor = 33.8638866667f;
		return hgInch * conversionFactor;
	}
	
	private static float convertFromHpaToHgInch(float hpa) {
		float conversionFactor = 33.8638866667f;
		return hpa / conversionFactor;
	}
	
	/**
	 * convert from one unit of length to another
	 * @param length the length reading to convert
	 * @param fromScale the from scale, as define by one of the length related constants in this class
	 * @param toScale the to scale, as defined by one of the length related constants in this class
	 * @return the converted length measurement
	 * @throws IllegalArgumentException if an invalid scale is provided
	 * @return the length in the to measurement scale
	 */
	public static float convertLength(float length, int fromScale, int toScale) {
		switch(fromScale){
		case MILLIMETRE:
			// convert from millimetre
			switch(toScale) {
			case INCH:
				return convertFromMillimetreToInch(length);
			default:
				throw new IllegalArgumentException("invalid to scale");
			}
		case INCH:
			// convert from inch
			switch(toScale) {
			case MILLIMETRE:
				return convertFromInchToMillimetre(length);
			default:
				throw new IllegalArgumentException("invalid to scale");
			}
		default:
			throw new IllegalArgumentException("invalid from scale");
		}
	}
	
	private static float convertFromMillimetreToInch(float length) {
		return length * 0.0393700787f;
		
	}
	
	private static float convertFromInchToMillimetre(float length) {
		return length * 25.4f;
	}
}
