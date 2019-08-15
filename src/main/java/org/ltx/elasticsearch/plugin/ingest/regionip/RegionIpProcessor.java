package org.ltx.elasticsearch.plugin.ingest.regionip;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

/** @author ltxlouis 8/14/2019 */
public class RegionIpProcessor extends AbstractProcessor {

  public static final String TYPE = "regionip";

  private final String field;
  private final String targetField;
  private final String algorithm;
  private final boolean ignoreMissing;
  private final Ip2RegionSearcher ip2RegionSearcher;

  RegionIpProcessor(
      String tag,
      String field,
      String targetField,
      String algorithm,
      boolean ignoreMissing,
      Ip2RegionSearcher ip2RegionSearcher)
      throws IOException {

    super(tag);
    this.field = field;
    this.targetField = targetField;
    this.algorithm = algorithm;
    this.ignoreMissing = ignoreMissing;
    this.ip2RegionSearcher = ip2RegionSearcher;
  }

  @Override
  public void execute(IngestDocument ingestDocument) throws Exception {
    String ip = ingestDocument.getFieldValue(field, String.class, ignoreMissing);

    if (ip == null && ignoreMissing) {
      return;
    } else if (ip == null) {
      throw new IllegalArgumentException(
          "field [" + field + "] is null, cannot extract regionip information.");
    }

    RegionIp regionIp = ip2RegionSearcher.searchIp(ip, algorithm);
    if (regionIp != null) {
      HashMap<String, Object> regionData = new HashMap<>(8);
      regionData.put("ip", regionIp.getIp());
      regionData.put("country_name", regionIp.getCountryName());
      regionData.put("region_name", regionIp.getRegionName());
      regionData.put("city_name", regionIp.getCityName());
      regionData.put("isp_name", regionIp.getIspName());
      ingestDocument.setFieldValue(targetField, regionData);
    }
  }

  @Override
  public String getType() {
    return TYPE;
  }

  public static final class Factory implements Processor.Factory {

    @Override
    public Processor create(
        Map<String, Processor.Factory> processorFactories, String tag, Map<String, Object> config)
        throws Exception {

      String field = readStringProperty(TYPE, tag, config, "field");
      String targetField = readStringProperty(TYPE, tag, config, "target_field", "regionip");
      Boolean ignoreMissing = readBooleanProperty(TYPE, tag, config, "ignore_missing", false);
      String algorithm = readStringProperty(TYPE, tag, config, "ip2region_algorithm", "MEMORY");

      return new RegionIpProcessor(
          tag, field, targetField, algorithm, ignoreMissing, new Ip2RegionSearcher());
    }
  }

  public String getField() {
    return field;
  }

  public String getTargetField() {
    return targetField;
  }

  public boolean isIgnoreMissing() {
    return ignoreMissing;
  }
}
