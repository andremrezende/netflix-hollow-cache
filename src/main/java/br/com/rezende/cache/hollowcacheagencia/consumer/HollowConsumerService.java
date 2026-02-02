package br.com.rezende.cache.hollowcacheagencia.consumer;

import br.com.rezende.cache.hollowcacheagencia.consumer.api.Agencia;
import br.com.rezende.cache.hollowcacheagencia.consumer.api.AgenciaAPI;
import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.fs.HollowFilesystemAnnouncementWatcher;
import com.netflix.hollow.api.consumer.fs.HollowFilesystemBlobRetriever;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class HollowConsumerService {
    private final HollowConsumer consumer;
    private AgenciaAPI api;

    public HollowConsumerService() {
        String hollowPath = System.getenv("HOLLOW_PATH");
        if (hollowPath == null || hollowPath.isEmpty()) {
            hollowPath = "./hollow-data";
        }

        Path path = Paths.get(hollowPath);

        HollowConsumer.BlobRetriever retriever = new HollowFilesystemBlobRetriever(path);
        HollowConsumer.AnnouncementWatcher watcher = new HollowFilesystemAnnouncementWatcher(path);

        this.consumer = HollowConsumer.withBlobRetriever(retriever)
                .withAnnouncementWatcher(watcher)
                .build();

        try {
            this.consumer.triggerRefreshTo(Long.MAX_VALUE);
            initializeAPI();
            System.out.println("✅ Consumer inicializado com sucesso");
        } catch (Exception e) {
            System.out.println("⚠️ Nenhum snapshot disponível ainda. API será inicializada após a primeira publicação.");
        }
    }

    public List<Map<String, Object>> listarAgencias() {
        // Força refresh para garantir dados mais recentes
        this.consumer.triggerRefresh();
        
        if (api == null) {
            initializeAPI();
        }
        
        if (api == null) {
            System.out.println("⚠️ API ainda está null. Nenhum dado disponível no Hollow.");
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        Collection<Agencia> agencias = api.getAllAgencia();
        System.out.println("📊 Total de agências no consumer: " + agencias.size());
        
        for (Agencia agencia : agencias) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", agencia.getId());
            item.put("nome", agencia.getNome() != null ? agencia.getNome().getValue() : "");
            item.put("codigo", agencia.getCodigo() != null ? agencia.getCodigo().getValue() : "");
            result.add(item);
        }
        
        return result;
    }

    public Map<String, Object> buscarPorId(int id) {
        // Força refresh para pegar dados atualizados
        this.consumer.triggerRefresh();
        
        if (api == null) {
            initializeAPI();
        }
        
        if (api == null) {
            System.out.println("AVISO: API não inicializada. Execute POST /publicar primeiro.");
            return null;
        }

        Collection<Agencia> agencias = api.getAllAgencia();
        for (Agencia agencia : agencias) {
            if (agencia.getId() == id) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", agencia.getId());
                item.put("nome", agencia.getNome() != null ? agencia.getNome().getValue() : "");
                item.put("codigo", agencia.getCodigo() != null ? agencia.getCodigo().getValue() : "");
                return item;
            }
        }
        
        System.out.println("Agência com ID " + id + " não encontrada no cache.");
        return null;
    }

    public Map<String, Object> listarPaginado(int pagina, int tamanho) {
        this.consumer.triggerRefresh();
        
        if (api == null) {
            initializeAPI();
        }
        
        if (api == null) {
            return Map.of(
                    "content", new ArrayList<>(),
                    "totalElements", 0,
                    "totalPages", 0,
                    "currentPage", pagina
            );
        }
        
        Collection<Agencia> todasAgencias = api.getAllAgencia();
        List<Agencia> listaAgencias = new ArrayList<>(todasAgencias);
        
        int totalElements = listaAgencias.size();
        int fromIndex = pagina * tamanho;
        int toIndex = Math.min(fromIndex + tamanho, totalElements);
        
        List<Map<String, Object>> content = new ArrayList<>();
        
        if (fromIndex < totalElements) {
            List<Agencia> paginaAtual = listaAgencias.subList(fromIndex, toIndex);
            
            for (Agencia agencia : paginaAtual) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", agencia.getId());
                item.put("nome", agencia.getNome() != null ? agencia.getNome().getValue() : "");
                item.put("codigo", agencia.getCodigo() != null ? agencia.getCodigo().getValue() : "");
                content.add(item);
            }
        }
        
        return Map.of(
                "content", content,
                "totalElements", totalElements,
                "totalPages", (int) Math.ceil((double) totalElements / tamanho),
                "currentPage", pagina
        );
    }

    public Map<String, Object> calcularOcupacaoMemoria() {
        long totalBytes = 0;
        Map<String, String> detalhesPorTipo = new HashMap<>();

        HollowReadStateEngine stateEngine = consumer.getStateEngine();
        for (String type : stateEngine.getAllTypes()) {
            HollowTypeReadState typeState = stateEngine.getTypeState(type);
            if (typeState != null) {
                long bytes = typeState.getApproximateHeapFootprintInBytes();
                totalBytes += bytes;
                detalhesPorTipo.put(type, String.format("%.2f KB", bytes / 1024.0));
            }
        }
        
        return Map.of(
                "totalGeral", String.format("%.2f MB", totalBytes / (1024.0 * 1024.0)),
                "detalhes", detalhesPorTipo,
                "totalBytes", totalBytes
        );
    }
    
    /**
     * Força uma atualização do consumer para pegar novos snapshots
     */
    public void refresh() {
        System.out.println("🔄 Forçando refresh manual do consumer...");
        consumer.triggerRefresh();
        if (api == null) {
            initializeAPI();
        }
        System.out.println("✅ Refresh manual completo.");
    }

    private void initializeAPI() {
        if (consumer.getStateEngine().getTypeState("Agencia") != null) {
            this.api = new AgenciaAPI(consumer.getStateEngine());
            System.out.println("✅ API inicializada com sucesso.");
        } else {
            this.api = null;
            System.out.println("⚠️ TypeState 'Agencia' não encontrado. API definida como null.");
        }
    }
    
    /**
     * Retorna o consumer para uso no Hollow Explorer UI
     */
    public HollowConsumer getConsumer() {
        return consumer;
    }
}
