from kafka import KafkaProducer

import json

producer  = KafkaProducer(
    bootstrap_servers="localhost:9092",
    value_serializer=lambda x: json.dumps(x).encode("utf-8")
)
message = {
    "transactionId": "TX123",
    "amount": 5000,
    "type": "TRANSFER"
}
producer.send(
    "fraud.prediction.request",
    message
)
producer.flush()
print("Message sent")