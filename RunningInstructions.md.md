# Adicionar as 2 libs
# Atualizar o path do connect-standalone.properties

# Instrucoes para correr o projeto

# Iniciar o Zookeeper
```kafka_2.13-3.6.0/bin/zookeeper-server-start.sh /kafka_2.13-3.6.0/config/zookeeper.properties```

# Iniciar o Kafka
```kafka_2.13-3.6.0/bin/kafka-server-start.sh /kafka_2.13-3.6.0/config/server.properties```

# Conectar o kafka a base de dados
``` kafka_2.13-3.6.0/bin/connect-standalone.sh kafka_2.13-3.6.0/config/connect-standalone.properties ```

# Correr o TopicsCreator

# Correr o Streams

# Correr o PurchaseGenerator


# Comandos relevantes
docker ps
sudo docker exec -it database cat /etc/hosts




## Create Results topic
bin/kafka-topics.sh --create --bootstrap-server broker1:9092 --replication-factor 1 --partitions 1 --topic Results

# listen to Results topic
kafka_2.13-3.6.0/bin/kafka-console-consumer.sh --bootstrap-server broker1:9092 --topic Results --from-beginning

# delete Results topic
kafka_2.13-3.6.0/bin/kafka-topics.sh --delete --bootstrap-server broker1:9092 --topic Results


curl -X POST -H "Content-Type: application/json" --data @config/config.jsonhttp: //localhost:8083/connectors

# Escrever para dentro de um topico
kafka_2.13-3.6.0/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic project3fromDBcountries


