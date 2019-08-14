# Elasticsearch regionip Ingest Processor

#### supported Elasticsearch verison: 6.2.4
Uses the [ip2region](https://github.com/lionsoul2014/ip2region) lib to add Chinese region info based on provided ip address

## Installation
1. download .zip file
2. use Elasticsearch elasticsearch-plugin command to install locally
[[reference]](https://www.elastic.co/guide/en/elasticsearch/plugins/6.2/plugin-management-custom-url.html)

## Usage

```
PUT _ingest/pipeline/custom-pipeline-name
{
  "description": "custom description",
  "processors": [
    {
      "regionip": {
        "field": "field_containing_ip_address",
        "target_field": "target_field_name"
      }
    }
  ]
}
```

```
target_field result example

"regionip": {
  "countryName": "中国",
  "ispName": "电信",
  "cityName": "上海市",
  "regionName": "上海"
}
```

##### for more usage please refer to official [doc](https://www.elastic.co/guide/en/elasticsearch/reference/6.2/ingest.html)

## Configuration

| conf | required | note |
| --- | --- | --- |
| field          | yes | Field name containing ip address |
| target_field   | no | Field name to write region info to, defaults to `regionip` |
| ignore_missing | no | If set to true, doc missing specified field will not throw a exception, defaults to `false`. |
| ip2region_algorithm | no |`BTREE`/`BINARY`/`MEMORY`, defaults to `MEMORY` [[link]](https://github.com/lionsoul2014/ip2region) |

## Build

```bash
gradle clean assemble
```