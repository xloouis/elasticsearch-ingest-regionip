package org.ltx.elasticsearch.plugin.ingest.regionip;

/** @author ltxlouis 8/14/2019 */
public class RegionIp {

  private String ip;

  private String countryName;

  private String regionName;

  private String cityName;

  private String ispName;

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public String getCountryName() {
    return countryName;
  }

  public void setCountryName(String countryName) {
    this.countryName = countryName;
  }

  public String getRegionName() {
    return regionName;
  }

  public void setRegionName(String regionName) {
    this.regionName = regionName;
  }

  public String getCityName() {
    return cityName;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public String getIspName() {
    return ispName;
  }

  public void setIspName(String ispName) {
    this.ispName = ispName;
  }

  @Override
  public String toString() {
    return "RegionIp{"
        + "ip='"
        + ip
        + '\''
        + ", countryName='"
        + countryName
        + '\''
        + ", regionName='"
        + regionName
        + '\''
        + ", cityName='"
        + cityName
        + '\''
        + ", ispName='"
        + ispName
        + '\''
        + '}';
  }
}
