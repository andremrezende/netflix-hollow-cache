# 🏗️ Arquitetura do Sistema - Hollow Cache Agência

## Visão Geral

```
┌────────────────────────────────────────────────────────────────┐
│                         Docker Host                             │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ Container 1  │  │ Container 2  │  │ Container 3  │         │
│  │  :8080       │  │  :8081       │  │  :8082       │         │
│  │              │  │              │  │              │         │
│  │ Spring Boot  │  │ Spring Boot  │  │ Spring Boot  │         │
│  │   App        │  │   App        │  │   App        │         │
│  │              │  │              │  │              │         │
│  │ ┌──────────┐ │  │ ┌──────────┐ │  │ ┌──────────┐ │         │
│  │ │ Producer │ │  │ │ Producer │ │  │ │ Producer │ │         │
│  │ │    +     │ │  │ │    +     │ │  │ │    +     │ │         │
│  │ │ Consumer │ │  │ │ Consumer │ │  │ │ Consumer │ │         │
│  │ └────┬─────┘ │  │ └────┬─────┘ │  │ └────┬─────┘ │         │
│  └──────┼───────┘  └──────┼───────┘  └──────┼───────┘         │
│         │                 │                 │                  │
│         └─────────────────┼─────────────────┘                  │
│                           │                                    │
│         ┌─────────────────▼─────────────────┐                  │
│         │      Volume: hollow_storage       │                  │
│         │      Path: /hollow-data           │                  │
│         │                                   │                  │
│         │  - snapshots                      │                  │
│         │  - deltas                         │                  │
│         │  - reverse deltas                 │                  │
│         │  - headers                        │                  │
│         │  - announced.version              │                  │
│         └───────────────────────────────────┘                  │
│                                                                 │
└────────────────────────────────────────────────────────────────┘
```

## Fluxo de Dados

### 1. Publicação (Write)

```
┌─────────────┐
│   Cliente   │
│             │
└──────┬──────┘
       │ POST /publicar
       ▼
┌─────────────────────────┐
│  Container (qualquer)   │
│                         │
│  ┌──────────────────┐   │
│  │ProducerController│   │
│  │  - Sincroniza    │───┼──┐
│  │  - Modifica      │   │  │ 1. consumerService.refresh()
│  │  - Publica       │   │  │ 2. Lê dados do /hollow-data
│  └────────┬─────────┘   │  │
│           │             │  │
│           ▼             │  │
│  ┌──────────────────┐   │  │
│  │ HollowProducer   │   │  │
│  │   Service        │   │  │
│  └────────┬─────────┘   │  │
│           │             │  │
└───────────┼─────────────┘  │
            │                │
            ▼                │
    ┌───────────────┐        │
    │ /hollow-data  │◄───────┘
    │               │
    │ - Escreve     │
    │   snapshot    │
    │ - Gera delta  │
    │ - Atualiza    │
    │   announced   │
    │   .version    │
    └───────────────┘
```

### 2. Leitura (Read)

```
┌─────────────┐
│   Cliente   │
│             │
└──────┬──────┘
       │ GET /agencias
       ▼
┌─────────────────────────┐
│  Container (qualquer)   │
│                         │
│  ┌──────────────────┐   │
│  │AgenciaController │   │
│  └────────┬─────────┘   │
│           │             │
│           ▼             │
│  ┌──────────────────┐   │
│  │ HollowConsumer   │   │
│  │   Service        │   │
│  │                  │   │
│  │ 1. refresh()     │───┼──┐
│  │ 2. listar()      │   │  │
│  └──────────────────┘   │  │
│                         │  │
└─────────────────────────┘  │
                             │
            ┌────────────────┘
            │
            ▼
    ┌───────────────┐
    │ /hollow-data  │
    │               │
    │ - Lê snapshot │
    │ - Aplica      │
    │   deltas      │
    │ - Reconstrói  │
    │   estado      │
    └───────────────┘
```

## Componentes Principais

### 1. ProducerController
**Responsabilidades:**
- Receber requisições de publicação
- **Sincronizar com Hollow antes de modificar** ✨ (CORREÇÃO)
- Gerenciar lista temporária em memória
- Delegar publicação ao HollowProducerService

**Endpoints:**
- `POST /publicar`
- `POST /alterar-cinco-agencias`
- `POST /remover-agencias`
- `POST /remover-99-agencias`
- `GET /status`

### 2. HollowProducerService
**Responsabilidades:**
- Escrever snapshots no filesystem
- Gerar deltas incrementais
- Anunciar novas versões
- Manter histórico de versões

**Arquivos gerados:**
- `snapshot-{version}` - Estado completo
- `delta-{from}-{to}` - Mudanças incrementais
- `reversedelta-{to}-{from}` - Rollback
- `header-{version}` - Metadados
- `announced.version` - Versão atual

### 3. HollowConsumerService
**Responsabilidades:**
- Ler snapshots do filesystem
- Aplicar deltas incrementalmente
- Manter estado em memória
- **Forçar refresh quando solicitado**

**Métodos principais:**
- `refresh()` - Atualiza do Hollow
- `listarAgencias()` - Retorna dados atuais
- `buscarPorId()` - Busca específica

### 4. AgenciaController
**Responsabilidades:**
- Expor API REST para consultas
- Delegar para HollowConsumerService
- Tratar paginação

**Endpoints:**
- `GET /agencias`
- `GET /agencias/{id}`
- `GET /agencias/paginado`

## Sincronização Cross-Container

### Antes da Correção ❌

```
Container 8080:              Container 8081:
┌──────────────────┐         ┌──────────────────┐
│ Lista memória: A │         │ Lista memória: ∅ │
│ [1,2,3,...,100]  │         │ []               │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         │ Publica                    │ Não sincroniza
         ▼                            ▼
    ┌────────────┐              ┌────────────┐
    │  Hollow    │              │  Hollow    │
    │  [100]     │              │  NÃO LÊ   │
    └────────────┘              └────────────┘
                                      ❌
```

### Depois da Correção ✅

```
Container 8080:              Container 8081:
┌──────────────────┐         ┌──────────────────┐
│ Lista memória: A │         │ Lista memória: ∅ │
│ [1,2,3,...,100]  │         │ []               │
└────────┬─────────┘         └────────┬─────────┘
         │                            │
         │ Publica                    │ 1. Limpa lista
         ▼                            │ 2. refresh()
    ┌────────────┐                    │ 3. Lê do Hollow
    │  Hollow    │◄───────────────────┘
    │  [100]     │                    
    └────────────┘                    
         │
         └──────────────────┐
                            ▼
                    ┌──────────────────┐
                    │ Lista memória: B │
                    │ [1,2,3,...,100]  │
                    └──────────────────┘
                            ✅
```

## Estados Possíveis

### Estado 1: Inicial (Vazio)
```
┌─────────┬─────────┬─────────┐
│  8080   │  8081   │  8082   │
├─────────┼─────────┼─────────┤
│ Mem: 0  │ Mem: 0  │ Mem: 0  │
│ H: 0    │ H: 0    │ H: 0    │
└─────────┴─────────┴─────────┘
         Hollow: vazio
```

### Estado 2: Após Publicação na 8080
```
┌─────────┬─────────┬─────────┐
│  8080   │  8081   │  8082   │
├─────────┼─────────┼─────────┤
│ Mem:100 │ Mem: 0  │ Mem: 0  │
│ H: 100  │ H: 100  │ H: 100  │
└─────────┴─────────┴─────────┘
      Hollow: 100 agências
```

### Estado 3: Após Sync em 8081
```
┌─────────┬─────────┬─────────┐
│  8080   │  8081   │  8082   │
├─────────┼─────────┼─────────┤
│ Mem:100 │ Mem:100 │ Mem: 0  │
│ H: 100  │ H: 100  │ H: 100  │
└─────────┴─────────┴─────────┘
      Hollow: 100 agências
      
✅ 8081 sincronizada!
```

### Estado 4: Todos Sincronizados
```
┌─────────┬─────────┬─────────┐
│  8080   │  8081   │  8082   │
├─────────┼─────────┼─────────┤
│ Mem:100 │ Mem:100 │ Mem:100 │
│ H: 100  │ H: 100  │ H: 100  │
└─────────┴─────────┴─────────┘
      Hollow: 100 agências
      
✅✅✅ TODOS sincronizados!
```

## Garantias do Sistema

### 1. Consistência Eventual
- Todos os consumidores eventualmente veem os mesmos dados
- Sincronização forçada antes de cada operação de escrita

### 2. Ordem de Operações
```
1. Cliente → POST /alterar-cinco-agencias
2. Container → sincronizarComConsumer()
3. Container → consumerService.refresh()
4. Container → Lê do /hollow-data
5. Container → Modifica lista local
6. Container → service.publicarAgencias()
7. Container → Escreve no /hollow-data
8. Outros containers → Leem na próxima operação
```

### 3. Atomicidade
- Cada operação de publicação gera uma nova versão
- Versões são atômicas (tudo ou nada)
- Deltas permitem rollback se necessário

## Monitoramento

### Logs Importantes

```bash
# Sincronização bem-sucedida
✓ Sincronizado 100 agências do Hollow para memória.

# Publicação bem-sucedida
Snapshot publicado com sucesso. Versão: 20260130200704001

# Refresh do consumer
Forçando refresh do consumer...
API reinicializada com sucesso.
```

### Métricas via /status

```json
{
  "porta": 8081,
  "agenciasEmMemoriaLocal": 100,  // Lista local da JVM
  "agenciasNoHollow": 100,         // Dados compartilhados
  "hollowPath": "/hollow-data",
  "mensagem": "✓ Hollow funcionando normalmente"
}
```

### Quando está OK ✅
- `agenciasNoHollow` é igual em todas as portas
- `mensagem` mostra "✓ Hollow funcionando normalmente"
- Logs mostram "Sincronizado X agências"

### Quando há problema ❌
- `agenciasNoHollow` é 0 em todas as portas → Execute `/publicar` na 8080
- `agenciasNoHollow` diferente entre portas → Aguarde alguns segundos ou force refresh via `/status`
- Logs mostram exceções → Verifique permissões do volume Docker

## Performance

### Snapshot vs Delta

- **Snapshot**: Estado completo (~7KB para 100 agências)
- **Delta**: Apenas mudanças (~500B para alterar 5 agências)
- **Sistema usa deltas automaticamente** para economizar espaço

### Consumo de Memória

- Heap por agência: ~100 bytes
- 100 agências: ~10KB
- 100.000 agências: ~10MB
- 1.000.000 agências: ~100MB

### Escalabilidade

O sistema atual suporta:
- ✅ Múltiplos consumidores (N containers)
- ✅ Leituras concorrentes (sem lock)
- ✅ Milhões de registros
- ⚠️ Escritas concorrentes (1 producer recomendado)

## Resumo das Correções

| Aspecto | Antes | Depois |
|---------|-------|--------|
| Sincronização | Apenas se lista vazia | **Sempre** |
| Refresh | Manual | **Automático** |
| Validação | Nenhuma | **Verifica se Hollow tem dados** |
| Debug | Difícil | **Endpoint /status** |
| Documentação | Mínima | **Completa** |
