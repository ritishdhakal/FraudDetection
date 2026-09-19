from kafka import KafkaConsumer
import json
import joblib
from producer import send_prediction_result


# Load trained fraud model
model = joblib.load("fraud_model.pkl")
print("Fraud model loaded")

# Create Kafka consumer
consumer = KafkaConsumer(
    'fraud.prediction.request',
    bootstrap_servers='localhost:9092',
    group_id='fraud-ml-debug-new',
    auto_offset_reset='latest'
)
print("Fraud ML consumer started...")

# Consume messages
for message in consumer:

    transaction = json.loads(message.value.decode('utf-8'))

    print("Received transaction:", transaction)

    # Feature engineering
    
    features = [[
    transaction["step"],
    transaction["amount"],
    transaction["senderPrevBalance"],
    transaction["receiverPrevBalance"],
    transaction["type_CASH_OUT"],
    transaction["type_DEBIT"],
    transaction["type_PAYMENT"],
    transaction["type_TRANSFER"]

    ]]

    print("Model features:", model.n_features_in_)
    print("Feature names:", getattr(model, "feature_names_in_", None))
    # Make prediction

    prediction = model.predict(features)[0]

    probability = model.predict_proba(features)[0][1]



    result  = {
        "fraud" : bool(prediction),
        "probability": float(probability)
    }


    send_prediction_result(result)