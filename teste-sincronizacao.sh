#!/bin/bash

echo "======================================"
echo "Script de Teste - Hollow Cache Agência"
echo "======================================"
echo ""

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Função para verificar se o serviço está disponível
wait_for_service() {
    local port=$1
    local max_attempts=30
    local attempt=1
    
    echo -n "Aguardando serviço na porta $port estar disponível"
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$port/status > /dev/null 2>&1; then
            echo -e " ${GREEN}✓${NC}"
            return 0
        fi
        echo -n "."
        sleep 2
        ((attempt++))
    done
    echo -e " ${RED}✗${NC}"
    return 1
}

echo "1️⃣  Verificando se os containers estão rodando..."
if ! docker ps | grep -q "hollow-producer"; then
    echo -e "${YELLOW}Containers não encontrados. Iniciando...${NC}"
    docker-compose up -d --build
    sleep 10
fi

echo ""
echo "2️⃣  Aguardando serviços ficarem prontos..."
wait_for_service 8080 || { echo -e "${RED}Falha ao iniciar porta 8080${NC}"; exit 1; }
wait_for_service 8081 || { echo -e "${RED}Falha ao iniciar porta 8081${NC}"; exit 1; }
wait_for_service 8082 || { echo -e "${RED}Falha ao iniciar porta 8082${NC}"; exit 1; }

echo ""
echo "3️⃣  Verificando status inicial (devem estar vazios)..."
echo "   Porta 8080:"
curl -s http://localhost:8080/status | jq '.agenciasNoHollow' || echo "Erro"
echo "   Porta 8081:"
curl -s http://localhost:8081/status | jq '.agenciasNoHollow' || echo "Erro"
echo "   Porta 8082:"
curl -s http://localhost:8082/status | jq '.agenciasNoHollow' || echo "Erro"

echo ""
echo "4️⃣  Publicando 100 agências na porta 8080..."
RESPONSE=$(curl -s -X POST http://localhost:8080/publicar)
echo "   Resposta: $RESPONSE"

sleep 3

echo ""
echo "5️⃣  Verificando se TODAS as portas veem os mesmos dados..."
PORTA_8080=$(curl -s http://localhost:8080/status | jq '.agenciasNoHollow')
PORTA_8081=$(curl -s http://localhost:8081/status | jq '.agenciasNoHollow')
PORTA_8082=$(curl -s http://localhost:8082/status | jq '.agenciasNoHollow')

echo "   Porta 8080: $PORTA_8080 agências"
echo "   Porta 8081: $PORTA_8081 agências"
echo "   Porta 8082: $PORTA_8082 agências"

if [ "$PORTA_8080" == "100" ] && [ "$PORTA_8081" == "100" ] && [ "$PORTA_8082" == "100" ]; then
    echo -e "   ${GREEN}✓ SUCESSO! Todas as portas veem 100 agências${NC}"
else
    echo -e "   ${RED}✗ FALHA! As portas não estão sincronizadas${NC}"
    exit 1
fi

echo ""
echo "6️⃣  Testando leitura de dados em cada porta..."
echo "   8080 - Total de agências:"
curl -s http://localhost:8080/agencias | jq 'length'
echo "   8081 - Total de agências:"
curl -s http://localhost:8081/agencias | jq 'length'
echo "   8082 - Total de agências:"
curl -s http://localhost:8082/agencias | jq 'length'

echo ""
echo "7️⃣  Alterando 5 agências pela porta 8081..."
RESPONSE=$(curl -s -X POST http://localhost:8081/alterar-cinco-agencias)
echo "   Resposta: $RESPONSE"

sleep 3

echo ""
echo "8️⃣  Verificando se a alteração é visível na porta 8082..."
AGENCIA_1=$(curl -s http://localhost:8082/agencias/1 | jq -r '.nome')
echo "   Nome da agência 1 na porta 8082: $AGENCIA_1"

if [[ "$AGENCIA_1" == *"ATUALIZADA"* ]]; then
    echo -e "   ${GREEN}✓ SUCESSO! Alteração propagada para porta 8082${NC}"
else
    echo -e "   ${RED}✗ FALHA! Alteração não foi propagada${NC}"
    exit 1
fi

echo ""
echo "9️⃣  Verificando agência específica em todas as portas..."
echo "   Porta 8080 - Agência 3:"
curl -s http://localhost:8080/agencias/3 | jq -r '.nome'
echo "   Porta 8081 - Agência 3:"
curl -s http://localhost:8081/agencias/3 | jq -r '.nome'
echo "   Porta 8082 - Agência 3:"
curl -s http://localhost:8082/agencias/3 | jq -r '.nome'

echo ""
echo "🔟  Teste de paginação na porta 8081..."
echo "   Primeira página (10 itens):"
curl -s "http://localhost:8081/agencias/paginado?page=0&size=10" | jq '.totalElements, .totalPages, .content | length'

echo ""
echo "======================================"
echo -e "${GREEN}✅ TODOS OS TESTES PASSARAM!${NC}"
echo "======================================"
echo ""
echo "As portas 8080, 8081 e 8082 estão compartilhando"
echo "os mesmos dados através do volume Docker /hollow-data"
echo ""
echo "Você pode testar manualmente com:"
echo "  curl http://localhost:8080/agencias"
echo "  curl http://localhost:8081/agencias"
echo "  curl http://localhost:8082/agencias"
echo ""
