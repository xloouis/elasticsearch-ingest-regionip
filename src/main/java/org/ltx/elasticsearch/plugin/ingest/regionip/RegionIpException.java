package org.ltx.elasticsearch.plugin.ingest.regionip;

/** @author ltxlouis 8/14/2019 */
public class RegionIpException extends RuntimeException {

  private static final long serialVersionUID = 6630221810157374604L;

  public RegionIpException() {
    super();
  }

  public RegionIpException(String msg) {
    super(msg);
  }

  public RegionIpException(String msg, Throwable throwable) {
    super(msg, throwable);
  }

  public RegionIpException(Throwable throwable) {
    super(throwable);
  }
}
