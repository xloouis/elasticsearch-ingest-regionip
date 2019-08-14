package org.ltx.elasticsearch.plugin.ingest.regionip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.elasticsearch.common.network.InetAddresses;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;

/** @author ltxlouis 8/14/2019 */
public class Ip2RegionSearcher {

  private DbSearcher dbSearcher;

  public Ip2RegionSearcher() {
    this.dbSearcher = getDbSearcher();
  }

  private DbSearcher getDbSearcher() {

    DbSearcher dbSearcher;

    try {
      InputStream is = this.getClass().getResourceAsStream("/ip2region.db");
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      int nRead;
      byte[] data = new byte[1024];
      while ((nRead = is.read(data, 0, data.length)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
      byte[] dbByteArray = buffer.toByteArray();

      DbConfig dbConfig = new DbConfig();
      dbSearcher = new DbSearcher(dbConfig, dbByteArray);
    } catch (IOException e) {
      throw new RegionIpException(e);
    } catch (DbMakerConfigException e) {
      throw new IllegalArgumentException("configure ip2region db failed", e);
    }

    return dbSearcher;
  }

  public RegionIp searchIp(String ip, String algorithm) {

    if (ip == null || !InetAddresses.isInetAddress(ip)) {
      return null;
    }

    int defaultAlgorithm = DbSearcher.MEMORY_ALGORITYM;
    if (algorithm != null && !algorithm.isEmpty()) {
      String trim = algorithm.trim();
      if ("BTREE".equalsIgnoreCase(trim)) {
        defaultAlgorithm = DbSearcher.BTREE_ALGORITHM;
      }
      if ("BINARY".equalsIgnoreCase(trim)) {
        defaultAlgorithm = DbSearcher.BINARY_ALGORITHM;
      }
      if ("MEMORY".equalsIgnoreCase(trim)) {
        defaultAlgorithm = DbSearcher.MEMORY_ALGORITYM;
      }
    }

    Method method;
    try {
      switch (defaultAlgorithm) {
        case DbSearcher.BTREE_ALGORITHM:
          method = dbSearcher.getClass().getMethod("btreeSearch", String.class);
          break;

        case DbSearcher.BINARY_ALGORITHM:
          method = dbSearcher.getClass().getMethod("binarySearch", String.class);
          break;

        case DbSearcher.MEMORY_ALGORITYM:
        default:
          method = dbSearcher.getClass().getMethod("memorySearch", String.class);
      }
    } catch (NoSuchMethodException e) {
      throw new RegionIpException("ip2region method not found", e);
    }

    DataBlock dataBlock = null;
    if (method != null) {
      try {
        dataBlock = (DataBlock) method.invoke(dbSearcher, ip);
      } catch (IllegalAccessException e) {
        throw new RegionIpException(e);
      } catch (InvocationTargetException e) {
        throw new RegionIpException("failed invoking ip2region method", e);
      }
    }

    if (dataBlock != null) {
      String region = dataBlock.getRegion();
      String[] split = region.split("\\|");

      RegionIp regionIp = new RegionIp();
      String countryName = split[0];
      if ("0".equals(countryName)) {
        return null;
      }

      String regionName = split[2];
      String cityName = split[3];

      if ("中国".equals(countryName) && "0".equals(regionName) && "0".equals(cityName)) {
        return null;
      }

      regionIp.setCountryName(countryName);
      regionIp.setRegionName(regionName);
      regionIp.setCityName(cityName);
      regionIp.setIspName(split[4]);
      return regionIp;
    }

    return null;
  }
}
