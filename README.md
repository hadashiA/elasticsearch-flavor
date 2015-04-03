# elasticsearch-flavor

**WORK IN PROGRESS**

Apache Mahout base Recommendation engine.
Use elasticsearch to storage and REST interface.

Client Example

```bash
$ http get 'localhost:9200/my_index/_flavor/similar_items/58151'
HTTP/1.1 200 OK
Content-Length: 559
Content-Type: application/json; charset=UTF-8

{
    "hits": {
        "hits": [
            {
                "item_id": "58151",
                "similarity": 0.9893830318926521
            },
            {
                "item_id": "57378",
                "similarity": 0.968011093361618
            },
            {
                "item_id": "34458",
                "similarity": 0.9607427173492923
            },
            {
                "item_id": "58139",
                "similarity": 0.9520546210873847
            },
            {
                "item_id": "58935",
                "similarity": 0.9413491414871521
            },
            {
                "item_id": "36824",
                "similarity": 0.9293944193683797
            },
            {
                "item_id": "53499",
                "similarity": 0.9293944193683797
            },
            {
                "item_id": "58064",
                "similarity": 0.9246263500463889
            },
            {
                "item_id": "53384",
                "similarity": 0.9202662907049427
            },
            {
                "item_id": "58148",
                "similarity": 0.9161937633276578
            }
        ],
        "total": 10
    },
    "took": 24
}
```

