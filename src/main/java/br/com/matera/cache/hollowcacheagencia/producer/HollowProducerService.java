package br.com.matera.cache.hollowcacheagencia.producer;

import br.com.matera.cache.hollowcacheagencia.model.Agencia;
import com.netflix.hollow.api.producer.HollowProducer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemAnnouncer;
import com.netflix.hollow.api.producer.fs.HollowFilesystemPublisher;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HollowProducerService {
    private HollowProducer producer;

    public HollowProducerService() {
        // Em produção, use um S3Publisher. Aqui usaremos o sistema de arquivos local.
        String hollowPath = System.getenv("HOLLOW_PATH");
        if (hollowPath == null || hollowPath.isEmpty()) {
            hollowPath = "./hollow-repo";
        }

        Path path = Paths.get(hollowPath);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar o diretório do Hollow: " + hollowPath);
        }
        HollowProducer.Publisher publisher = new HollowFilesystemPublisher(path);
        HollowProducer.Announcer announcer = new HollowFilesystemAnnouncer(path);

        this.producer = HollowProducer.withPublisher(publisher)
                .withAnnouncer(announcer)
                .build();
    }

    public void publicarAgencias(List<Agencia> agencias) {
        producer.runCycle(state -> {
            for (Agencia a : agencias) {
                state.add(a);
            }
        });
    }
}
