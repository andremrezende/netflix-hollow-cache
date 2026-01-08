# Roteiro de Teste do Projeto
## Siga estes passos para validar o funcionamento:

- Build: Gere os jars com mvn clean package.
- Subir: Execute docker-compose up --build.
- Publicar: No seu navegador ou Postman - (Isso gera as 100 agências), chame:

```shell
curl -v -X POST http://localhost:8080/publicar
```

- Consumir (Instância 1) - Deve retornar a agência de ID 50 instantaneamente:
```shell
curl -s http://localhost:8080/agencias/agencia/50
```

- Consumir -> Deve listar todas as agências:
```shell
curl -X GET http://localhost:8080/agencias
```

- Consumir agencias paginadas
```shell
curl -X GET "http://localhost:8080/agencias/paginado?page=2&size=5"
```