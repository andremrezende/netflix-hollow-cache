package br.com.matera.cache.hollowcacheagencia.consumer;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.fs.HollowFilesystemAnnouncementWatcher;
import com.netflix.hollow.api.consumer.fs.HollowFilesystemBlobRetriever;
import com.netflix.hollow.api.objects.generic.GenericHollowObject;
import com.netflix.hollow.core.index.HollowPrimaryKeyIndex;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
@Service
public class HollowConsumerService {
    private final HollowConsumer consumer;
    private HollowPrimaryKeyIndex idIndex;

    public HollowConsumerService() {
        String hollowPath = System.getenv("HOLLOW_PATH");
        if (hollowPath == null || hollowPath.isEmpty()) {
            hollowPath = "./hollow-repo";
        }

        Path path = Paths.get(hollowPath);

        HollowConsumer.BlobRetriever retriever = new HollowFilesystemBlobRetriever(path);
        HollowConsumer.AnnouncementWatcher watcher = new HollowFilesystemAnnouncementWatcher(path);

        this.consumer = HollowConsumer.withBlobRetriever(retriever)
                .withAnnouncementWatcher(watcher)
                .build();

        this.consumer.triggerRefresh();
        this.idIndex = new HollowPrimaryKeyIndex(consumer.getStateEngine(), "Agencia", "id");
    }

    public List<Map<String, Object>> listarAgencias() {
        List<Map<String, Object>> result = new ArrayList<>();
        HollowReadStateEngine stateEngine = consumer.getStateEngine();

        HollowTypeReadState typeState = stateEngine.getTypeState("Agencia");
        if (typeState == null) return result;

        // Forma recomendada para evitar NPE e problemas de unboxing
        BitSet ordinals = typeState.getPopulatedOrdinals();
        int ordinal = ordinals.nextSetBit(0);

        while (ordinal != -1) {
            try {
                GenericHollowObject obj = new GenericHollowObject(stateEngine, "Agencia", ordinal);

                // Lendo campos com segurança
                int id = obj.getInt("id");
                String nome = obj.getString("nome");
                String codigo = obj.getString("codigo");

                result.add(Map.of(
                        "id", id,
                        "nome", nome != null ? nome : "",
                        "codigo", codigo != null ? codigo : ""
                ));
            } catch (Exception e) {
                System.err.println("Erro ao ler dados no ordinal " + ordinal + ": " + e.getMessage());
            }

            ordinal = ordinals.nextSetBit(ordinal + 1);
        }
        return result;
    }
    public Map<String, Object> buscarPorId(int id) {
        // O índice retorna o 'ordinal' (endereço interno na memória)
        int ordinal = idIndex.getMatchingOrdinal(id);

        // Se o ordinal for -1, significa que o ID não existe no snapshot atual
        if (ordinal == -1) {
            return null;
        }

        try {
            // Criamos o objeto genérico para ler os dados do ordinal encontrado
            GenericHollowObject obj = new GenericHollowObject(consumer.getStateEngine(), "Agencia", ordinal);

            return Map.of(
                    "id", obj.getInt("id"),
                    "nome", obj.getString("nome") != null ? obj.getString("nome") : "",
                    "codigo", obj.getString("codigo") != null ? obj.getString("codigo") : ""
            );
        } catch (Exception e) {
            System.err.println("Erro ao acessar dados do ID " + id + ": " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> listarPaginado(int pagina, int tamanho) {
        HollowReadStateEngine stateEngine = consumer.getStateEngine();
        HollowTypeReadState typeState = stateEngine.getTypeState("Agencia");

        if (typeState == null) {
            return Map.of("content", List.of(), "totalElements", 0);
        }

        BitSet ordinals = typeState.getPopulatedOrdinals();
        int totalElements = ordinals.cardinality();

        int fromIndex = pagina * tamanho;
        List<Map<String, Object>> content = new ArrayList<>();

        if (fromIndex < totalElements) {
            int ordinal = ordinals.nextSetBit(0);
            int currentCount = 0;
            int itemsCollected = 0;

            while (ordinal != -1 && itemsCollected < tamanho) {
                if (currentCount >= fromIndex) {
                    try {
                        GenericHollowObject obj = new GenericHollowObject(stateEngine, "Agencia", ordinal);

                        // FORÇAR A LEITURA: O erro "arr is null" acontece no momento do get
                        Map<String, Object> item = Map.of(
                                "id", obj.getInt("id"),
                                "nome", obj.getString("nome") != null ? obj.getString("nome") : "",
                                "codigo", obj.getString("codigo") != null ? obj.getString("codigo") : ""
                        );
                        content.add(item);
                        itemsCollected++;
                    } catch (Exception e) {
                        // Se falhar (arr is null), o registro é ignorado mas o loop continua
                        System.err.println("Erro ao ler ordinal " + ordinal + " na paginação: " + e.getMessage());
                    }
                }
                currentCount++;
                ordinal = ordinals.nextSetBit(ordinal + 1);
            }
        }

        return Map.of(
                "content", content,
                "totalElements", totalElements,
                "totalPages", (int) Math.ceil((double) totalElements / tamanho),
                "currentPage", pagina
        );
    }
}