
The mapping files should be copied to ES config dir (Standard dir listed below)



/etc/elasticsearch/mappings/kbplus/

      com.k_int.kbplus.License.json
      com.k_int.kbplus.Org.json
      com.k_int.kbplus.Package.json
      com.k_int.kbplus.Platform.json
      com.k_int.kbplus.Subscription.json
      com.k_int.kbplus.TitleInstance.json

    

You can create the initial KB+ index with

curl -X PUT "http://localhost:9200/kbplus"



curl \-XPUT 'localhost:9200/kbplus?pretty' \-d'
{
"settings": {
        "number_of_shards" :   1,
        "number_of_replicas" : 0
    }
}'