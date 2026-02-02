package br.com.rezende.cache.hollowcacheagencia.generator;

import br.com.rezende.cache.hollowcacheagencia.model.Agencia;
import com.netflix.hollow.api.codegen.HollowAPIGenerator;
import com.netflix.hollow.core.write.HollowWriteStateEngine;
import com.netflix.hollow.core.write.objectmapper.HollowObjectMapper;

import java.io.IOException;

/**
 * Classe utilitária para gerar a API type-safe do Hollow.
 * Execute este main para gerar as classes na pasta src/main/java
 */
public class GenerateHollowAPI {
    public static void main(String[] args) throws IOException {
        // Criar o schema a partir das classes do modelo
        HollowWriteStateEngine writeEngine = new HollowWriteStateEngine();
        HollowObjectMapper mapper = new HollowObjectMapper(writeEngine);
        
        mapper.initializeTypeState(Agencia.class);
        
        HollowAPIGenerator generator = new HollowAPIGenerator.Builder()
                .withAPIClassname("AgenciaAPI")
                .withPackageName("br.com.rezende.cache.hollowcacheagencia.consumer.api")
                .withDataModel(writeEngine)
                .build();
        
        generator.generateFiles("src/main/java");
        
        System.out.println("API gerada com sucesso em src/main/java/br/com/rezende/cache/hollowcacheagencia/consumer/api/");
    }
}
