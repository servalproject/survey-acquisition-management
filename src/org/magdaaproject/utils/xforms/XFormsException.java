/*
 * Copyright (C) 2012 The MaGDAA Project
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
package org.magdaaproject.utils.xforms;

/**
 * an exception that may be thrown when manipulating XForms data
 */
public class XFormsException extends Exception {

	private static final long serialVersionUID = -425084215005956786L;
	
	public XFormsException() {
		super();
	}
	
	public XFormsException(String message) {
		super(message);
	}
	
	public XFormsException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public XFormsException(Throwable cause) {
		super(cause);
	}
}
