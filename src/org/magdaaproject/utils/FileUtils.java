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

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import android.os.Environment;
import android.text.TextUtils;

/**
 * a utility class which exposes utility methods for interacting with files and the filesystem
 */
public class FileUtils {
	
	/**
	 * Maximum file size that the {@link #readFile(String) readFile} method will process
	 */
	public static final int MAX_READ_FILE_SIZE = 1024;
	
	/**
	 * the default name of the index file in a MaGDAA Bundle used by the {@link #getMagdaaBundleIndex(String)} method
	 */
	public static final String MAGDAA_BUNDLE_INDEX_FILE_NAME = "_index.txt";
	
	/**
	 * check to see if a directory is writeable if it exists, if it doesn't exist this method
	 * tries to create it
	 * 
	 * @param path the file system path to check
	 * @return true if the directory is writeable
	 */
	public static boolean isDirectoryWriteable(String path) {
		
		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}

		File mPath = new File(path);

		if(mPath.isDirectory() && mPath.canWrite()) {
			return true;
		} else {
			return mPath.mkdirs();
		}
	}
	
	/**
	 * check to see if a directory is readble if it exists
	 * 
	 * @param path the file system path to check
	 * @return true if the directory is readable
	 */
	public static boolean isDirectoryReadable(String path) {
		
		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}

		File mPath = new File(path);

		if(mPath.isDirectory() && mPath.canRead()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * check to see if a file exists and is readable
	 * 
	 * @param path the full path of a file to test
	 * @return true if the path is a file and is readable
	 */
	public static boolean isFileReadable(String path) {

		if(TextUtils.isEmpty(path) == true) {
			throw new IllegalArgumentException("the path parameter is required");
		}

		File mFile = new File(path);

		if(mFile.isFile() && mFile.canRead()) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * write a temporary file into the specified directory with the supplied contents
	 * 
	 * @param contents the contents of the file
	 * @param directory the path of the directory to contain the file
	 * @return the full path to the new file 
	 * @throws IOException if something bad happens
	 */
	public static String writeTempFile(String contents, String directory) throws IOException {
		
		// check to see if the supplied path is writeable
		if(isDirectoryWriteable(directory) == false) {
			throw new IOException("unable to access specified path '" + directory + "'");
		}
		
		if(TextUtils.isEmpty(contents)) {
			throw new IllegalArgumentException("the contents of the file is required");
		}
		
		// create the new temporary file
		File mFile = null;
		try {
			mFile = File.createTempFile("magdaa-", ".txt", new File(directory));
		} catch (IOException e) {
			throw new IOException("unable to create temp file", e);
		}
		
		// open the file
		PrintWriter mWriter = null;
		try {
			mWriter = new PrintWriter(new FileWriter(mFile));
			
			// write the supplied contents
			mWriter.write(contents);
			
			// close the file
			mWriter.close();
		} catch (FileNotFoundException e) {
			throw new IOException("unable to open temp file", e);
		} catch (IOException e) {
			throw new IOException("unable to write temporary file", e);
		}
		
		try {
			return mFile.getCanonicalPath();
		} catch (IOException e) {
			throw new IOException("unable to get canonical path", e);
		}
	}
	
	/**
	 * write a file with the specified content using the supplied name in the required directory
	 * 
	 * @param contents the contents of the file
	 * @param fileName the name of the new file
	 * @param directory the name of the directory
	 * @return the full path to the new file 
	 * @throws IOException if something bad happens
	 */
	public static String writeNewFile(String contents, String fileName, String directory) throws IOException {
		
		// check to see if the supplied path is writeable
		if(isDirectoryWriteable(directory) == false) {
			throw new IOException("unable to access specified path '" + directory + "'");
		}
		
		// check the other parameters
		if(TextUtils.isEmpty(contents)) {
			throw new IllegalArgumentException("the contents of the file is required");
		}
		
		if(TextUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("the name of the file is required");
		}
		
		// create the new  file
		if(directory.endsWith(File.separator) == false) {
			directory += File.separator;
		}
		
		File mFile = new File(directory + fileName);
		
		// open the file
		PrintWriter mWriter = null;
		try {
			mWriter = new PrintWriter(new FileWriter(mFile));
			
			// write the supplied contents
			mWriter.write(contents);
			
			// close the file
			mWriter.close();
		} catch (FileNotFoundException e) {
			throw new IOException("unable to open temp file", e);
		} catch (IOException e) {
			throw new IOException("unable to write temporary file", e);
		}
		
		try {
			return mFile.getCanonicalPath();
		} catch (IOException e) {
			throw new IOException("unable to get canonical path", e);
		}
	}
	
	/**
	 * check to see if external storage is available
	 * 
	 * @return true if external storage is available
	 */
	public static boolean isExternalStorageAvailable() {
		String mStorageState = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(mStorageState);
	}
	
	/**
	 * write a file with the specified content using the supplied name in the required directory
	 * compressing the file using the gzip algorithm
	 * 
	 * @param contents the contents of the file
	 * @param fileName the name of the new file, excluding the .gz extension
	 * @param directory the name of the directory
	 * @return the full path to the new file 
	 * @throws IOException if something bad happens
	 */
	public static String writeNewGzipFile(String contents, String fileName, String directory) throws IOException {
		
		// check to see if the supplied path is writeable
		if(isDirectoryWriteable(directory) == false) {
			throw new IOException("unable to access specified path '" + directory + "'");
		}
		
		// check the other parameters
		if(TextUtils.isEmpty(contents)) {
			throw new IllegalArgumentException("the contents of the file is required");
		}
		
		if(TextUtils.isEmpty(fileName)) {
			throw new IllegalArgumentException("the name of the file is required");
		}
		
		// create the new  file
		if(directory.endsWith(File.separator) == false) {
			directory += File.separator;
		}
		
		File mFile = new File(directory + fileName + ".gz");
		
		// open the file and write its contents
		FileOutputStream mOutput = null;
		Writer mWriter = null;
		try {
			mOutput = new FileOutputStream(mFile);
			
			mWriter = new OutputStreamWriter(new GZIPOutputStream(mOutput), "UTF-8");
			
			mWriter.write(contents);
			
			mWriter.close();
			
		} catch (FileNotFoundException e) {
			throw new IOException("unable to open the file for writing", e);
		} catch (UnsupportedEncodingException e) {
			throw new IOException("unable to encode the file using 'UTF-8'", e);
		} catch (IOException e) {
			throw new IOException("unable to write data to the file", e);
		}
		
		try {
			return mFile.getCanonicalPath();
		} catch (IOException e) {
			throw new IOException("unable to get canonical path", e);
		}
	}
	
	/**
	 * write a temporary file into the specified directory with the supplied contents
	 * 
	 * @param contents the contents of the file
	 * @param directory the path of the directory to contain the file
	 * @return the full path to the new file 
	 * @throws IOException if something bad happens
	 */
	public static String writeTempFile(byte[] contents, String directory) throws IOException {
		
		// check to see if the supplied path is writeable
		if(isDirectoryWriteable(directory) == false) {
			throw new IOException("unable to access specified path '" + directory + "'");
		}
		
		if(contents.length == 0) {
			throw new IllegalArgumentException("the contents of the file is required");
		}
		
		// create the new temporary file
		File mFile = null;
		try {
			mFile = File.createTempFile("magdaa-", ".txt", new File(directory));
		} catch (IOException e) {
			throw new IOException("unable to create temp file", e);
		}
		
		// open the file
		FileOutputStream mWriter;
		try {
			mWriter = new FileOutputStream(mFile);
			
			// write the supplied contents
			mWriter.write(contents);
			
			// close the file
			mWriter.close();
		} catch (FileNotFoundException e) {
			throw new IOException("unable to open temp file", e);
		} catch (IOException e) {
			throw new IOException("unable to write temporary file", e);
		}
		
		try {
			return mFile.getCanonicalPath();
		} catch (IOException e) {
			throw new IOException("unable to get canonical path", e);
		}
	}
	
	/**
	 * read a file and return the contents as a byte array
	 * @param path the path to the file to read
	 * @return the contents of the file as a byte array
	 * @throws IOException if something bad happens or the file size is greater than {@link #MAX_READ_FILE_SIZE MAX_READ_FILE_SIZE}
	 */
	public static byte[] readFile(String path) throws IOException {
		
		if(isFileReadable(path) == false) {
			throw new IOException("unable to find the specified file");
		}
		
		// read the file
		try {
			RandomAccessFile mFile = new RandomAccessFile(path, "r"); // only read the file
			
			// check on the size of the file
			if(mFile.length() <= MAX_READ_FILE_SIZE) {
			
				byte[] mBytes = new byte[(int) mFile.length()];
				
				mFile.read(mBytes);
				
				mFile.close();
				
				return mBytes;
			} else {
				throw new IOException("the file size exceeds '" + MAX_READ_FILE_SIZE + "' bytes and will not be read");
			}
		} catch (FileNotFoundException e) {
			throw new IOException("unable to open the file", e);
		} catch (IOException e) {
			throw new IOException("unable to read from the file", e);
		} 
	}
	
	/**
	 * create a zip file using the specified path and input file / directory
	 * 
	 * @param zipFile the file to the zip file to create
	 * @param inputPath the input path, either a file or directory
	 * @throws IOException if something bad happens
	 */
	public static void writeNewZipFile(String zipFile, String inputPath) throws IOException {

		// double check the parameters
		if(TextUtils.isEmpty(zipFile) == true || TextUtils.isEmpty(inputPath) == true) {
			throw new IllegalArgumentException("both parameters to this method is required");
		}
		
		if(isFileReadable(zipFile) == true) {
			throw new IOException("output file already exists");
		}
		
		if(isDirectoryReadable(inputPath) == false) {
			throw new IOException("unable to access the specified input directory '" + inputPath + "'");
		}
		
		ZipUtil.pack(new File(inputPath), new File(zipFile));
	}
	
	/**
	 * extract the contents of a zip file to a specified path
	 * 
	 * @param zipFile the path to the zip file
	 * @param outputPath the path where to output the zip file
	 * @throws IOException if something bad happens
	 */
	public static void extractFromZipFile(String zipFile, String outputPath) throws IOException {
		
		// double check the parameters
		if(TextUtils.isEmpty(zipFile) == true || TextUtils.isEmpty(outputPath) == true) {
			throw new IllegalArgumentException("both parameters to this method is required");
		}
		
		if(isFileReadable(zipFile) == false) {
			throw new IOException("unable to access the specified file");
		}
		
		if(isDirectoryWriteable(outputPath) == false) {
			throw new IOException("unable to access the specified output directory '" + outputPath + "'");
		}
		
		if(outputPath.endsWith(File.separator) == false) {
			outputPath += File.separator;
		}
		
		final String mMacOsxDir = "__MACOSX";

		// use the ZipUtil library for ease of development
		ZipUtil.unpack(new File(zipFile), new File(outputPath), new NameMapper() {
			public String map(String name) {
				if(name.contains(mMacOsxDir) == true) {
					return null;
				} else {
					return name;
				}
			}
		});
	}
	
	/**
	 * extract the bundle index file from a MaGDAA Bundle file
	 * @param bundleFile the path to the bundle file
	 * @return the contents of the index file or null if no index file was found
	 * @throws IOException if something bad happens
	 */
	public static String getMagdaaBundleIndex(String bundleFile) throws IOException {
		
		// double check the parameters
		if(TextUtils.isEmpty(bundleFile) == true) {
			throw new IllegalArgumentException("the path to the input file is required");
		}
		
		if(isFileReadable(bundleFile) == false) {
			throw new IOException("unable to access the specified file");
		}
		
		if(ZipUtil.containsEntry(new File(bundleFile), MAGDAA_BUNDLE_INDEX_FILE_NAME) == true) {
			
			byte[] mBytes = ZipUtil.unpackEntry(new File(bundleFile), MAGDAA_BUNDLE_INDEX_FILE_NAME);
			
			return new String(mBytes, "UTF-8");
			
		} else {
			// bundle file is missing
			return null;
		}
	}
	
	/**
	 * get a list of files in a directory
	 * 
	 * @param dirPath the directory to search for files
	 * @param extension an extension to filter the list of files, if empty all files are returned
	 * @return an array of file names or null if no files match
	 * @throws IOException
	 */
	public static String[] listFilesInDir(String dirPath, String extension) throws IOException {
		String[] mExtensions = new String[1];
		
		if(TextUtils.isEmpty(extension)) {
			mExtensions = null;
		} else {
			mExtensions[0] = extension;
		}
		
		return listFilesInDir(dirPath, mExtensions);
	}
	
	/**
	 * get a list of files in a directory
	 * 
	 * @param dirPath the directory to search for files
	 * @param extensions a list of extensions to filter the list of files, if null all files are returns
	 * @return an array of file names or null if no files match
	 * @throws IOException
	 */
	public static String[] listFilesInDir(String dirPath, String[] extensions) throws IOException {

		String[] mFileList = null;

		// check the parameters
		if(TextUtils.isEmpty(dirPath) == true) {
			throw new IllegalArgumentException("the dirPath paramter is required");
		}

		if(isDirectoryWriteable(dirPath) == false) {
			throw new IOException("unable to access the required directory: " + dirPath);
		}

		// get a list of files
		File mDir = new File(dirPath);

		File[] mFiles = mDir.listFiles(new ExtensionFileFilter(extensions));

		if(mFiles != null && mFiles.length > 0) {

			mFileList = new String[mFiles.length];

			for(int i = 0; i < mFiles.length; i++) {
				mFileList[i] = mFiles[i].getCanonicalPath();
			}

			Arrays.sort(mFileList);
		}

		return mFileList;
	}
	
	
	
	/**
	 * recursively delete files and folders below the given path
	 * @param path the path to delete
	 * @throws IOException
	 */
	public static void recursiveDelete(String path) throws IOException {
		recursiveDelete(new File(path), false);
	}
	
	/**
	 * recursively delete files and folders below the given path
	 * @param path the path to delete
	 * @throws IOException
	 */
	public static void recursiveDelete(File path) throws IOException {
		recursiveDelete(path, false);
	}
	
	/**
	 * recursively delete files and folders below the given path
	 * @param path the path to delete
	 * @param deleteParent if set to true the parent directory of the path will also be deleted
	 * @throws IOException
	 */
	public static void recursiveDelete(File path, boolean deleteParent) throws IOException {
		
		// based on the code available here: http://stackoverflow.com/a/5059468
		// and considered to be in the public domain
		
		if(path.isDirectory()) {
			
			// get a list of files and directories in this directory
			String[] mChildPaths = path.list();
			
			// loop through deleting all child files and directories
			for (int i = 0; i < mChildPaths.length; i++) {
				File mChildPath = new File (path, mChildPaths[i]);
				
				if(mChildPath.isDirectory()) {
					recursiveDelete(mChildPath, deleteParent);
				}
				else {
					if (mChildPath.delete() == false) {
						throw new IOException("unable to delete path" + mChildPath.getAbsolutePath());
					}
				}
			}
			
			// delete the empty directory
			if(deleteParent == true) {
				path.delete();
			}
		}
	}

	// file filter using extensions
	private static class ExtensionFileFilter implements FileFilter {

		private String[] extensions;

		public ExtensionFileFilter(String[] extensions) {
			this.extensions = extensions;
		}

		public boolean accept(File pathname) {

			if (pathname.isDirectory()) {
				return false;
			}

			if (pathname.canRead() == false) {
				return false;
			}

			String name = pathname.getName().toLowerCase(Locale.US);

			if(extensions == null) {
				if(!name.equals("..") || !name.equals(".")) {
					return true;
				}
			} else {
				for(String mExtension: extensions) {
					if(name.endsWith(mExtension)) {
						return true;
					}
				}

				return false;
			}

			return false;
		}
	}

	/**
	 * get the extension component of a file name
	 * 
	 * @param fileName the name of the file
	 * @return the extension of the file, or null
	 */
	public static String getExtension(String fileName) {

		if(fileName == null) {
			return null;
		}

		int mLocation =  fileName.lastIndexOf(".");

		if(mLocation == -1) {
			return null;
		} else {
			return fileName.substring(mLocation + 1);
		}
	}
}
