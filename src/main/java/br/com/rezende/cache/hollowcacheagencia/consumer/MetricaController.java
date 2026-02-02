package br.com.rezende.cache.hollowcacheagencia.consumer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/metricas")
public class MetricaController {
    private final HollowConsumerService service;

    public MetricaController(HollowConsumerService service) {
        this.service = service;
    }

    @GetMapping("/memoria")
    public Map<String, Object> getMemoria() {
        return service.calcularOcupacaoMemoria();
    }
}
