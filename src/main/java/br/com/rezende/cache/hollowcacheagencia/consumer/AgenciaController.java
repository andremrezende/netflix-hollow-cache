package br.com.rezende.cache.hollowcacheagencia.consumer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agencias")
public class AgenciaController {

    private final HollowConsumerService service;

    public AgenciaController(HollowConsumerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Map<String, Object>> listar() {
        return service.listarAgencias();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscar(@PathVariable int id) {
        Map<String, Object> agencia = service.buscarPorId(id);
        if (agencia != null) {
            return ResponseEntity.ok(agencia);
        }
        return ResponseEntity.status(404)
                .body(Map.of(
                    "erro", "Agência não encontrada",
                    "id", id,
                    "mensagem", "Verifique se os dados foram publicados com POST /publicar"
                ));
    }

    /**
     * Retorna agências de forma paginada
     * Ex: /consumer/listar/paginado?page=0&size=10
     */
    @GetMapping("/paginado")
    public Map<String, Object> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.listarPaginado(page, size);
    }
}