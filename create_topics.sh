#!/bin/bash

# Defina a lista de tópicos que você deseja criar
TOPICOS=("SockPurchasesTopic" "SockSalesTopic" "ResultsTopicSale" "ResultsTopicPurchase" "DBInfoTopic")

# Loop para criar os tópicos
for topico in "${TOPICOS[@]}"; do
  kafka_2.13-3.6.0/bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic "$topico"
  echo "Tópico '$topico' criado com sucesso."
done
