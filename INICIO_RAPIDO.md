# 🚀 Guia Rápido - Iniciar do Zero

## Passo 1: Limpar ambiente anterior (opcional mas recomendado)

```bash
# Parar containers se estiverem rodando
docker-compose down -v

# Limpar dados antigos do Hollow (opcional)
rm -rf hollow-data/*

# Limpar build anterior
mvn clean
```

## Passo 2: Recompilar a aplicação

```bash
mvn clean package -DskipTests
```

## Passo 3: Subir os containers

```bash
docker-compose up --build
```

Você verá 3 serviços iniciando:
- `hollow-producer` na porta 8080
- `hollow-consumer-1` na porta 8081
- `hollow-consumer-2` na porta 8082

## Passo 4: Verificar se todos estão prontos

Em outro terminal:

```bash
# Verificar status de cada porta
curl http://localhost:8080/status | jq
curl http://localhost:8081/status | jq
curl http://localhost:8082/status | jq
```

**Resultado esperado**:
```json
{
  "porta": 8080,
  "agenciasEmMemoriaLocal": 0,
  "agenciasNoHollow": 0,
  "hollowPath": "/hollow-data",
  "mensagem": "⚠ Nenhuma agência no Hollow. Execute POST /publicar na porta 8080 primeiro."
}
```

## Passo 5: Publicar dados iniciais

```bash
curl -X POST http://localhost:8080/publicar
```

**Resultado**: `"100 Agências publicadas no Hollow! Total em memória: 100"`

⚠️ **IMPORTANTE**: Aguarde 3-5 segundos após a publicação para que o Hollow propague os dados para os consumers.

## Passo 6: Validar sincronização

```bash
# AGUARDE 3-5 segundos após publicar, então verifique:
# Verificar se TODAS as portas veem os dados
curl http://localhost:8080/status | jq '.agenciasNoHollow'  # deve retornar: 100
curl http://localhost:8081/status | jq '.agenciasNoHollow'  # deve retornar: 100
curl http://localhost:8082/status | jq '.agenciasNoHollow'  # deve retornar: 100
```

💡 **Dica**: Se retornar 0, aguarde mais alguns segundos. O HollowFilesystemAnnouncementWatcher verifica mudanças periodicamente.

## Passo 7: Testar leitura em todas as portas

```bash
# Listar todas as agências
curl http://localhost:8080/agencias | jq 'length'  # 100
curl http://localhost:8081/agencias | jq 'length'  # 100
curl http://localhost:8082/agencias | jq 'length'  # 100

# Buscar agência específica
curl http://localhost:8081/agencias/42 | jq
```

## Passo 8: Testar modificação cross-porta

```bash
# Modificar pela porta 8082
curl -X POST http://localhost:8082/alterar-cinco-agencias

# Verificar mudança na porta 8080
curl http://localhost:8080/agencias/1 | jq '.nome'
# Resultado: "Agência ATUALIZADA 1"
```

## ✅ Teste Automatizado

Execute o script de teste completo:

```bash
./teste-sincronizacao.sh
```

## 🔍 Monitoramento em Tempo Real

Para acompanhar os logs:

```bash
# Logs de todos os containers
docker-compose logs -f

# Logs de um container específico
docker-compose logs -f hollow-consumer-1
```

## 🛑 Parar tudo

```bash
docker-compose down
```

## 🗑️ Limpar tudo (incluindo volumes)

```bash
docker-compose down -v
rm -rf hollow-data/*
```

## 📊 Endpoints Disponíveis

| Endpoint | Método | Descrição |
|----------|--------|-----------|
| `/status` | GET | Status do container e quantidade de agências |
| `/agencias` | GET | Lista todas as agências |
| `/agencias/{id}` | GET | Busca agência por ID |
| `/agencias/paginado` | GET | Lista paginada (params: page, size) |
| `/publicar` | POST | Publica 100 agências |
| `/carga-pesada` | POST | Publica 1.000.000 de agências |
| `/alterar-cinco-agencias` | POST | Modifica agências 1-5 |
| `/remover-agencias` | POST | Remove todas as agências |
| `/remover-99-agencias` | POST | Remove 99 agências |

## 🐛 Troubleshooting

### Problema: "Connection refused" ao acessar endpoints

**Solução**: Aguarde alguns segundos. Spring Boot leva tempo para iniciar.

```bash
# Verificar se o container está saudável
docker ps
docker logs hollow-cache-agencia-hollow-producer-1
```

### Problema: Portas já em uso

**Solução**: Libere as portas ou modifique o docker-compose.yml

```bash
# Verificar o que está usando a porta
sudo lsof -i :8080
sudo lsof -i :8081
sudo lsof -i :8082

# Matar processo na porta
kill -9 <PID>
```

### Problema: Dados dessincronizados

**Solução**: Force um refresh ou reinicie os containers

```bash
# Opção 1: Force refresh via endpoint
curl http://localhost:8081/status

# Opção 2: Reinicie os containers
docker-compose restart
```

### Problema: Build falhou

**Solução**: Verifique se tem Java 21 e Maven instalados

```bash
java -version  # deve ser 21
mvn -version   # deve estar instalado

# Se necessário, instale:
# Ubuntu/Debian: sudo apt install openjdk-21-jdk maven
# MacOS: brew install openjdk@21 maven
```

## 📚 Documentação Adicional

- [README.md](README.md) - Documentação original do projeto

## 🎯 Próximos Passos

Após validar que tudo funciona:

1. Ler a [SOLUCAO_PROBLEMA.md](SOLUCAO_PROBLEMA.md) para entender a arquitetura
2. Experimentar com diferentes volumes de dados (carga-pesada)
3. Testar paginação e busca por ID
4. Monitorar consumo de memória no endpoint `/metricas`
