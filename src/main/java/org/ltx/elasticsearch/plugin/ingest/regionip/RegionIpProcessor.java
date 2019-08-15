package org.ltx.elasticsearch.plugin.ingest.regionip;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readOptionalList;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
  private final Set<Property> properties;
  private final Ip2RegionSearcher ip2RegionSearcher;

  RegionIpProcessor(
      String tag,
      String field,
      String targetField,
      String algorithm,
      boolean ignoreMissing,
      Set<Property> properties,
      Ip2RegionSearcher ip2RegionSearcher)
      throws IOException {

    super(tag);
    this.field = field;
    this.targetField = targetField;
    this.algorithm = algorithm;
    this.ignoreMissing = ignoreMissing;
    this.properties = properties;
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

      for (Property property : properties) {
        switch (property) {
          case IP:
            regionData.put("ip", regionIp.getIp());
            break;
          case COUNTRY_NAME:
            regionData.put("country_name", regionIp.getCountryName());
            break;
          case REGION_NAME:
            regionData.put("region_name", regionIp.getRegionName());
            break;
          case CITY_NAME:
            regionData.put("city_name", regionIp.getCityName());
            break;
          case ISP_NAME:
            regionData.put("isp_name", regionIp.getIspName());
            break;
          default:
            // do nothing
        }
      }

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
      List<String> propertyNames = readOptionalList(TYPE, tag, config, "properties");

      final Set<Property> properties;
      if (propertyNames != null) {
        properties = EnumSet.noneOf(Property.class);
        for (String fieldName : propertyNames) {
          try {
            properties.add(Property.parseProperty(fieldName));
          } catch (IllegalArgumentException e) {
            throw new RegionIpException(e.getMessage());
          }
        }
      } else {
        properties = Property.ALL_PROPERTIES;
      }

      return new RegionIpProcessor(
          tag, field, targetField, algorithm, ignoreMissing, properties, new Ip2RegionSearcher());
    }
  }

  enum Property {
    /** output property */
    IP,
    COUNTRY_NAME,
    REGION_NAME,
    CITY_NAME,
    ISP_NAME;

    static final EnumSet<Property> ALL_PROPERTIES = EnumSet.allOf(Property.class);

    public static Property parseProperty(String value) {
      try {
        Property property = valueOf(value.toUpperCase(Locale.ROOT));
        if (!ALL_PROPERTIES.contains(property)) {
          throw new IllegalArgumentException("invalid");
        }

        return property;
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(
            "illegal property value ["
                + value
                + "]. valid values are "
                + Arrays.toString(ALL_PROPERTIES.toArray()));
      }
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
