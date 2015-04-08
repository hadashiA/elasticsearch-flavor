# elasticsearch-flavor

**WORK IN PROGRESS**

Apache Mahout base Recommendation engine.
Use elasticsearch to storage and REST interface.

## Version

| Flavor | Elasticsearch |
|:-------|:-------------|
| 0.0.1  | 1.4.0        |

## Installation


## Getting Start

### Setup Preference index

```bash
curl -XPOST localhost:9200/my_index -d '{
    "mappings" : {
        "preference" : {
            "preference": {
                "properties": {
                    "user_id": {
                        "type": "long"
                    },
                    "item_id": {
                        "type": "long"
                    },
                    "value": {
                        "type": "float"
                    }
                }
            }
        }
    }
}'
```

|Name     | Type  | Description                    |
|:------- |:------|:--------------------------------|
| user_id | long  | Unique User ID for this plugin. |
| item_id | long  | Unique Item ID for this plugin. |
| value   | float | Value rated by user_id for item_id. |

### Preprocessing

Before you run the recommendation, preload the preference.
(Read into memory using the FastIDSet of Mahout.)

```bash
curl -XPOST localhost:9200/_flavor/reload -d '{
    "preference" : {
        "index" : "my_index",
        "type" : "preference"
    }
}'
```

Response
```
{"dataModel":"ElasticsearchPreloadDataModel[index:suzuri_preference_development type:preference]","total_users":6907,"total_items":40695}
```

## Recommendation

### Similar Items

```bash
$ curl 'localhost:9200/_flavor/similar_items/5803?size=3'
HTTP/1.1 200 OK
Content-Length: 126
Content-Type: application/json; charset=UTF-8

{
    "hits": {
        "hits": [
            {
                "item_id": 40891,
                "value": 1.0
            },
            {
                "item_id": 48541,
                "value": 1.0
            },
            {
                "item_id": 151,
                "value": 1.0
            }
        ],
        "total": 3
    },
    "took": 4
}
```


