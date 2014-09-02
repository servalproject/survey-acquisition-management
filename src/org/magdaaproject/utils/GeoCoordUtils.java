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
 * a utility class which exposes utility methods for interacting with geocordinates
 *
 */
public class GeoCoordUtils {
	
	/**
	 * determine if a latitude is valid
	 * 
	 * @param latitude the latitude coordinate to check
	 * @return true if the latitude is valid
	 */
	public static boolean isValidLatitude(float latitude) {
		if(latitude < -90 || latitude > 90) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * determine if a longitude is valid
	 * 
	 * @param longitude the longitude coordinate to check
	 * @return true if the longitude is valid
	 */
	public static boolean isValidLongitude(float longitude) {
		if(longitude < -180 || longitude > 180) {
			return false;
		}else {
			return true;
		}
	}

}
