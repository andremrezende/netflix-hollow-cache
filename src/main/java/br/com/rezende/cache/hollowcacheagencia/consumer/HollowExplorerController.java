package br.com.rezende.cache.hollowcacheagencia.consumer;

import com.netflix.hollow.explorer.ui.HollowExplorerUI;
import com.netflix.hollow.explorer.ui.jetty.HollowExplorerUIServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Component
public class HollowExplorerController {

    public static final int PORT = 7777;

    private final HollowConsumerService consumerService;
    
    private HollowExplorerUIServer server;

    public HollowExplorerController(HollowConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @PostConstruct
    public void startExplorerUI() {
        try {
            HollowExplorerUI ui = new HollowExplorerUI("", consumerService.getConsumer());
            
            this.server = new HollowExplorerUIServer(ui, PORT);
            server.start();
            
            System.out.println("=================================================");
            System.out.println("Hollow Explorer UI iniciado em: http://localhost:" + PORT);
            System.out.println("=================================================");
        } catch (Exception e) {
            System.err.println("Erro ao iniciar Hollow Explorer UI: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @PreDestroy
    public void stopExplorerUI() {
        if (server != null) {
            try {
                server.stop();
                System.out.println("Hollow Explorer UI encerrado.");
            } catch (Exception e) {
                System.err.println("Erro ao parar Hollow Explorer UI: " + e.getMessage());
            }
        }
    }
}
