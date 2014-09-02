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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.magdaaproject.utils.FileUtils;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.text.TextUtils;

/**
 * a utility class which exposes utility methods for interacting with XForms data
 *
 */
public class XFormsUtils {

	/*
	 * private class level variables
	 */
	private DocumentBuilderFactory factory;
	private DocumentBuilder builder;
	private DOMImplementation domImpl;
	private Document xmlDoc;
	private Element rootElement;
	private Element element;
	
	private TransformerFactory transFactory;
	private Transformer transformer;
	private StringWriter stringWriter;

	private boolean debugOutput = false;

	/**
	 * set the debug output flag, determines if output is indented or compact
	 * @param value the new debug output value
	 */
	public void setDebugOutput(boolean value) {
		debugOutput = value;
	}

	/**
	 * get the debug output flag, determines if output is indented or compact
	 * @return the current debug output value
	 */
	public boolean getDebugOutput() {
		return debugOutput;
	}


	/**
	 * build an XForms instance XML string using the elements defined in the elements HashMap
	 * 
	 * @param elements a HashMap containing the elements to include in the XForms instance XML string
	 * @param formId an identifier matching the form that this instance is based on
	 * 
	 * @return a string of XML in the XForms instance format
	 * 
	 * @throws XFormsException if something bad happens
	 */
	public final String buildXFormsData(HashMap<String, String> elements, String formId) throws XFormsException {

		// validate the parameters
		if(elements == null || elements.isEmpty() == true) {
			throw new IllegalArgumentException("the elements parameter is required");
		}

		if(TextUtils.isEmpty(formId)) {
			throw new IllegalArgumentException("the formId parameter is required");
		}

		// create the xml document builder factory object
		factory = DocumentBuilderFactory.newInstance();

		// create the xml document builder object and get the DOMImplementation object
		try {
			builder = factory.newDocumentBuilder();
		} catch (javax.xml.parsers.ParserConfigurationException e) {
			throw new XFormsException("unable to start to build xml", e);
		}

		domImpl = builder.getDOMImplementation();

		// create a document 
		xmlDoc = domImpl.createDocument(null, "data", null);

		// get the root element
		rootElement = this.xmlDoc.getDocumentElement();

		// add the id attribute to the root element
		rootElement.setAttribute("id", formId);

		// iterate over the hashmap adding elements
		for(Map.Entry<String, String> mEntry : elements.entrySet()) {

			element = xmlDoc.createElement(mEntry.getKey());
			element.setTextContent(mEntry.getValue());

			rootElement.appendChild(element);
		}

		// build the output
		try {
			// create a transformer 
			transFactory = TransformerFactory.newInstance();
			transformer = transFactory.newTransformer();

			// set some options on the transformer
			transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			
			// output formatted XML if required
			if(debugOutput) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			}

			// get a transformer and supporting classes
			stringWriter = new StringWriter();
			
			StreamResult result = new StreamResult(stringWriter);
			DOMSource    source = new DOMSource(xmlDoc);

			// transform the internal objects into XML
			transformer.transform(source, result);
			
			return stringWriter.toString();

		} catch (javax.xml.transform.TransformerException e) {
			throw new XFormsException("unable to transform the xml into a string", e);
		}
	}
	
	/**
	 * checks to see if the xform at the specified path has a location question
	 * 
	 * @param filePath the path to the file
	 * @return true if the file includes a location question
	 * @throws XFormsException when something bad happens
	 */
	public static boolean hasLocationQuestion(String filePath) throws XFormsException {
		
		boolean mFoundTag = false;
		
		// check the parameters
		if(FileUtils.isFileReadable(filePath) == false) {
			throw new XFormsException("unable to find specified file");
		}
		
		// open the file for reading
		InputStream mInput = null;
		try {
			 mInput = new FileInputStream(filePath);
		} catch (FileNotFoundException e) {
			throw new XFormsException("unable to open specified file", e);
		}
		
		// instantiate the xml pull related classes
		XmlPullParserFactory mFactory;
		XmlPullParser mParser;
		
		try {
			mFactory = XmlPullParserFactory.newInstance();
			mFactory.setNamespaceAware(true);
			mParser = mFactory.newPullParser();
		} catch (XmlPullParserException e) {
			throw new XFormsException("unable to instantiate required XML related classes", e);
		}
		
		// use the input stream to read the xml
		try {
			mParser.setInput(mInput, null);
		} catch (XmlPullParserException e) {
			throw new XFormsException("unable to use input stream as input to parser", e);
		}
		
		// start parsing the xml
		try {
			int mEventType = mParser.getEventType();
			
			// loop through the document
			while ((mEventType = mParser.next()) != XmlPullParser.END_DOCUMENT && mFoundTag == false) {
				if (mEventType == XmlPullParser.START_TAG) { // start of a tag
					if(mParser.getName().equals("bind") == true) { // this is a bind tag
						
						Map<String, String> mAttributes = getAttributes(mParser); // get the attributes on the bind tag
						
						if(mAttributes.containsKey("type") == true) { // check to see if there is an attribute named type
							if(mAttributes.get("type").equals("geopoint") == true) { // check to see if the value of the attribute of geopoint
								// this form has a bind with a type of geopoint
								// therefore it contains a location question
								mFoundTag = true;
							}
						}
					}
				}
			}
		} catch (XmlPullParserException e) {
			throw new XFormsException("unable to parse xml document", e);
		} catch (IOException e) {
			throw new XFormsException("unable to parse xml document", e);
		} finally {
			try {
				mInput.close();
			} catch (IOException e) {
				throw new XFormsException("unable to close xml document file", e);
			}
		}
		
		return mFoundTag;
	}
	
	// get the attributes associated with a tag
	private static Map<String, String> getAttributes(XmlPullParser parser) {
		Map<String, String> mAttributes = null;
		
		// get the number of attributes
		int mCount = parser.getAttributeCount();
		
		if(mCount > 0) { // attributes are associated with this tag
			mAttributes = new HashMap<String, String>(mCount);
			
			// process each attribute in turn
			for(int i = 0; i < mCount; i++) {
				mAttributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
			}
		}
		return mAttributes;
	}
	
	/**
	 * format a timestamp in the ISO 8601 format for 
	 * use in an XForms instance XML file
	 * @param timestamp the timestamp to format
	 * @return a formatted string
	 */
	@SuppressLint("SimpleDateFormat")
	public static String formatTimestamp(long timestamp) {
		// this specific date format is used to support interoperability with ODK and other XForms related systems
		SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSZ");
        return mSimpleDateFormat.format(timestamp);
	}
}
