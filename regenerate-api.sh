#!/bin/bash
# Script para regenerar a API do Hollow quando o modelo Agencia for modificado

echo "=== Regenerando API do Hollow ==="

# 1. Remove a API antiga
echo "1. Removendo API antiga..."
rm -rf src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/api

# 2. Move temporariamente os arquivos que dependem da API
echo "2. Movendo arquivos temporariamente..."
mv src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/HollowConsumerService.java HollowConsumerService.bak 2>/dev/null
mv src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/AgenciaController.java AgenciaController.bak 2>/dev/null
mv src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/MetricaController.java MetricaController.bak 2>/dev/null

# 3. Compila e gera a API
echo "3. Compilando e gerando API..."
mvn clean compile exec:java -Dexec.mainClass="br.com.rezende.cache.hollowcacheagencia.generator.GenerateHollowAPI" -q

# 4. Restaura os arquivos
echo "4. Restaurando arquivos..."
mv HollowConsumerService.bak src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/HollowConsumerService.java 2>/dev/null
mv AgenciaController.bak src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/AgenciaController.java 2>/dev/null
mv MetricaController.bak src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/MetricaController.java 2>/dev/null

# 5. Compila tudo junto
echo "5. Compilando projeto completo..."
mvn compile -q

echo "=== API regenerada com sucesso! ==="
