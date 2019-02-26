# kafka-rest-connect
Provides kafka connect features over HTTP/S, can work with any kafka ReST proxy, tested with confluent's ReST Proxy

### Kafka connect 
https://kafka.apache.org/documentation/#connect provides framework to publish and consume data from kafka topic, it has pluggable 
source and sink connectors to variety of data sources

### Why HTTP?
<li>Kafka connect is powerfull framework but tied to kafka and requires kafka cluster access
<li>Scenario, in which we need kafka connect functionality but do not want to expose kafka cluster to internet, then its a challenge.
<li>With HTTP/S we communicate with kafka-ReST Proxy, which in turn runs kafka producers & consumers
<li>From remote client perspective http is proven and simple way to transfer data through robust faremwork like kafka-connect
<li> This compoenent provides at least once data publish gurantee. 


