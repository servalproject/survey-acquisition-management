package org.zeroturnaround.zip;

@SuppressWarnings("serial")
public class ZipException extends RuntimeException {
  public ZipException(String msg) {
    super(msg);
  }

  public ZipException(Exception e) {
    super(e);
  }
}
