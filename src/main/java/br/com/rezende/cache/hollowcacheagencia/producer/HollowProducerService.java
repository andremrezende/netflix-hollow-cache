package br.com.rezende.cache.hollowcacheagencia.producer;

import br.com.rezende.cache.hollowcacheagencia.model.Agencia;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemAnnouncer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemPublisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class HollowProducerService {
    private HollowProducer producer;
    private Path hollowPath;

    public HollowProducerService() {
        inicializarProducer();
    }
    
    private void inicializarProducer() {
        String hollowPathStr = System.getenv("HOLLOW_PATH");
        if (hollowPathStr == null || hollowPathStr.isEmpty()) {
            hollowPathStr = "./hollow-data";
        }

        this.hollowPath = Paths.get(hollowPathStr);
        this.hollowPath = Paths.get(hollowPathStr);
        try {
            if (!Files.exists(hollowPath)) {
                Files.createDirectories(hollowPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório do Hollow: " + hollowPathStr);
        }
        HollowProducer.Publisher publisher = new HollowFilesystemPublisher(hollowPath);
        HollowProducer.Announcer announcer = new HollowFilesystemAnnouncer(hollowPath);

        this.producer = HollowProducer.withPublisher(publisher)
                .withAnnouncer(announcer)
                .build();
        
        try {
            this.producer.restore(Long.MAX_VALUE, null);
            System.out.println("Producer restaurado do snapshot anterior.");
        } catch (Exception e) {
            System.out.println("Nenhum snapshot anterior encontrado. Iniciando do zero.");
        }
    }

    public void publicarAgencias(List<Agencia> agencias) {
        System.out.println("Publicando " + agencias.size() + " agências no Hollow...");
        
        if (producer.getWriteEngine() == null) {
            producer.initializeDataModel(Agencia.class);
        }
        
        long version = producer.runCycle(state -> {
            for (Agencia a : agencias) {
                state.add(a);
            }
        });
        
        System.out.println("Snapshot publicado com sucesso. Versão: " + version);
    }
}
