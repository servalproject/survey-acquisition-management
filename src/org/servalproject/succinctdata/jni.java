package org.servalproject.succinctdata;

/*
 * To use these in other classes, you need:
 * 
 *      import org.servalproject.succinctdata.jni;
 * 
 * and then to call, use something like:
 * 
 * 		// XXX Testing JNI
 *      org.servalproject.succinctdata.jni.xml2succinctdata("foo","bar","quux");
 *
 */

public class jni {

	static {
		System.loadLibrary("smac");
	}

    // Old deprecated call that returned bytes
    //    public static native byte[] xml2succinct(String xmlforminstance, String formname, String formversion, String succinctpath);
    // New call that provides encoded strings limited to some MTU
    public static native String[] xml2succinctfragments(String xmlforminstance, String formname, String formversion, String succinctpath, int mtu);
    // We no longer generate CSV locally
    // public static native int updatecsv(String succinctpath,String rxspooldir,String outputdir);	
	
}
