from kafka import KafkaProducer

import json

producer  = KafkaProducer(
    bootstrap_servers="localhost:9092",
    value_serializer=lambda value: json.dumps(value).encode("utf-8")
)

def send_prediction_result(result):
        producer.send(
                "fraud.detection.result",
                value=result
                
        )
    
        
        
producer.flush()
print("Message sent")