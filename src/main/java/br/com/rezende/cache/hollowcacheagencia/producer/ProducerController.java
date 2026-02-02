package br.com.rezende.cache.hollowcacheagencia.producer;

import br.com.rezende.cache.hollowcacheagencia.consumer.HollowConsumerService;
import br.com.rezende.cache.hollowcacheagencia.model.Agencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@RestController
public class ProducerController {
    @Autowired
    private HollowProducerService service;
    
    @Autowired
    private HollowConsumerService consumerService;
    
    @Value("${server.port:8080}")
    private int serverPort;

    private static final List<Agencia> agenciasEmMemoria = new ArrayList<>();
    
    /**
     * Endpoint de debug para verificar o estado atual
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        consumerService.refresh();
        List<Map<String, Object>> agenciasNoHollow = consumerService.listarAgencias();
        
        return Map.of(
            "porta", serverPort,
            "agenciasEmMemoriaLocal", agenciasEmMemoria.size(),
            "agenciasNoHollow", agenciasNoHollow.size(),
            "hollowPath", System.getenv("HOLLOW_PATH") != null ? System.getenv("HOLLOW_PATH") : "./hollow-data",
            "mensagem", agenciasNoHollow.isEmpty() ? 
                "⚠ Nenhuma agência no Hollow. Execute POST /publicar na porta 8080 primeiro." :
                "✓ Hollow funcionando normalmente"
        );
    }

    @PostMapping("/publicar")
    public String publicar() {
        if(agenciasEmMemoria.size() < 100) {
            for (int id = 1; id <= 100; id++) {
                agenciasEmMemoria.add(new Agencia(id, "Agência Central " + id, "00" + id));
            }

            service.publicarAgencias(agenciasEmMemoria);
            return "100 Agências publicadas no Hollow! Total em memória: " + agenciasEmMemoria.size();
        }
        return "Nenhum nova agencia publicada. Total em memória: " + agenciasEmMemoria.size();
    }

    @PostMapping("/carga-pesada")
    public String cargaPesada() {
        long start = System.currentTimeMillis();

        agenciasEmMemoria.addAll(IntStream.range(1, 1000001)
                .mapToObj(i -> new Agencia(i, "Agência Central " + i, "00" + i))
                .toList());

        service.publicarAgencias(agenciasEmMemoria);

        long end = System.currentTimeMillis();
        return "100.000 agências publicadas em " + (end - start) + "ms. Total em memória: " + agenciasEmMemoria.size();
    }

    @PostMapping("/alterar-cinco-agencias")
    public String alterarCincoAgencias() {
        // SEMPRE sincroniza do Hollow antes de fazer alterações
        sincronizarComConsumer();
        
        if (agenciasEmMemoria.isEmpty()) {
            return "Erro: Nenhuma agência encontrada no Hollow. Execute POST /publicar primeiro na porta 8080.";
        }
        
        int alteradas = 0;
        for (Agencia agencia : agenciasEmMemoria) {
            if (agencia.getId() <= 5) {
                agencia.setNome("Agência ATUALIZADA " + agencia.getId());
                alteradas++;
            }
        }

        service.publicarAgencias(agenciasEmMemoria);
        return alteradas + " agências foram atualizadas (IDs 1-5) e novo dataset publicado no Hollow! Total em memória: " + agenciasEmMemoria.size();
    }

    @PostMapping("/remover-agencias")
    public String removerAgencias() {
        // SEMPRE sincroniza do Hollow antes de fazer alterações
        sincronizarComConsumer();
        
        int totalRemovidas = agenciasEmMemoria.size();
        agenciasEmMemoria.clear();
        
        service.publicarAgencias(agenciasEmMemoria);
        return "Todas as " + totalRemovidas + " agências foram removidas. Total em memória: 0";
    }
    
    @PostMapping("/remover-99-agencias")
    public String remover99Agencias() {
        // SEMPRE sincroniza do Hollow antes de fazer alterações
        sincronizarComConsumer();
        
        int totalAntes = agenciasEmMemoria.size();
        
        if (totalAntes == 0) {
            return "Erro: Nenhuma agência encontrada no Hollow. Execute POST /publicar primeiro na porta 8080.";
        }
        
        int totalRemover = 99;
        
        if (totalAntes <= 1) {
            return "Não há agências suficientes para remover. Total em memória: " + totalAntes;
        }
        
        if (totalAntes < 100) {
            totalRemover = totalAntes - 1;
        }
        
        agenciasEmMemoria.sort(Comparator.comparingInt(Agencia::getId));
        for (int index = 0; index < totalRemover && !agenciasEmMemoria.isEmpty(); index++) {
            agenciasEmMemoria.remove(index);
        }
        
        int totalDepois = agenciasEmMemoria.size();
        int removidas = totalAntes - totalDepois;
        
        service.publicarAgencias(agenciasEmMemoria);
        return removidas + " agências foram removidas. Total em memória: " + totalDepois;
    }

    /**
     * Sincroniza a lista em memória com os dados do Hollow Consumer
     * SEMPRE busca os dados mais recentes do Hollow
     */
    private void sincronizarComConsumer() {
        System.out.println("Sincronizando com o Hollow...");
        consumerService.refresh(); // Força refresh do consumer
        
        List<Map<String, Object>> agenciasDoConsumer = consumerService.listarAgencias();
        
        agenciasEmMemoria.clear(); // Limpa a lista local
        
        if (!agenciasDoConsumer.isEmpty()) {
            for (Map<String, Object> dados : agenciasDoConsumer) {
                int id = (int) dados.get("id");
                String nome = (String) dados.get("nome");
                String codigo = (String) dados.get("codigo");
                agenciasEmMemoria.add(new Agencia(id, nome, codigo));
            }
            System.out.println("✓ Sincronizado " + agenciasEmMemoria.size() + " agências do Hollow para memória.");
        } else {
            System.out.println("⚠ Nenhuma agência encontrada no Hollow.");
        }
    }

}