### Elastic Search Mapping

The mapping files should be copied to ES config dir (standard dir listed below)

/etc/elasticsearch/mappings/\<indexname\>/

      com.k_int.kbplus.License.json
      com.k_int.kbplus.Org.json
      com.k_int.kbplus.Package.json
      com.k_int.kbplus.Platform.json
      com.k_int.kbplus.Subscription.json
      com.k_int.kbplus.TitleInstance.json

You can create the initial index with

    curl \-XPUT 'localhost:9200/<indexname>?pretty' \-d'
    {
    "settings": {
            "number_of_shards" :   1,
            "number_of_replicas" : 0
        }
    }'