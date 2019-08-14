package org.ltx.elasticsearch.plugin.ingest.regionip;

import java.util.Map;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.Processor.Factory;
import org.elasticsearch.ingest.Processor.Parameters;
import org.elasticsearch.plugins.IngestPlugin;
import org.elasticsearch.plugins.Plugin;

/** @author ltxlouis 8/14/2019 */
public class IngestRegionIpPlugin extends Plugin implements IngestPlugin {

  @Override
  public Map<String, Factory> getProcessors(Parameters parameters) {
    return MapBuilder.<String, Processor.Factory>newMapBuilder()
        .put(RegionIpProcessor.TYPE, new RegionIpProcessor.Factory())
        .immutableMap();
  }
}
