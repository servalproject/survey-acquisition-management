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

import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;

/**
 * a utility class which exposes utility methods for manipulating and formating time
 */
@SuppressLint("SimpleDateFormat")
public class TimeUtils {
	
	/**
	 * format string used to format time in the short format
	 */
	public static final String DEFAULT_SHORT_TIME_FORMAT = "HH:mm";
	
	/**
	 * format string used to format time in the default format
	 */
	public static final String DEFAULT_TIME_FORMAT = "h:mm:ss a";
	
	/**
	 * format string for the short date format
	 */
	public static final String DEFAULT_SHORT_DATE_FORMAT = "dd/MM/yyyy";
	
	/**
	 * format string for the long date format
	 */
	public static final String DEFAULT_LONG_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss z";
	
	/**
	 * a constant representing one hour in milliseconds
	 */
	public static final long ONE_HOUR_IN_MILLISECONDS = 3600000;
	

	/**
	 * format the provided date / time using the supplied format
	 * 
	 * @param dateTime the number of milliseconds since the epoch
	 * @param format the format string to use
	 * @return the formatted dateTime
	 */
	public static String formatDateTime(long dateTime, String format) {
		
		SimpleDateFormat mFormat = new SimpleDateFormat(format);
		
		return mFormat.format(dateTime);
	}
	
	/**
	 * format a time into the format used by all MaGDAA software
	 * @param time the time in milliseconds to format
	 * @return a formated time string
	 */
	public static String formatTime(long time) {
		return formatDateTime(time, DEFAULT_TIME_FORMAT);
	}
	
	/**
	 * format a date into the format used by all MaGDAA software
	 * @param date the date in milliseconds to format
	 * @return a formated date string
	 */
	public static String formatDate(long date) {
		return formatDateTime(date, DEFAULT_SHORT_DATE_FORMAT);
	}
	
	/**
	 * format a date and time into the format used by all MaGDAA software
	 * @param dateTime the date and time in milliseconds to format
	 * @return a formated date and time string
	 */
	public static String formatLongDate(long dateTime) {
		return formatDateTime(dateTime, DEFAULT_LONG_DATE_FORMAT);
	}

}
