package br.com.matera.cache.hollowcacheagencia.producer;

import br.com.matera.cache.hollowcacheagencia.model.Agencia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.IntStream;

@RestController
public class ProducerController {
    @Autowired
    private HollowProducerService service;

    @PostMapping("/publicar")
    public String publicar() {
        List<Agencia> lista = IntStream.range(0, 100)
                .mapToObj(i -> new Agencia(i, "Agência Central " + i, "00" + i))
                .toList();

        service.publicarAgencias(lista);
        return "100 Agências publicadas no Hollow!";
    }
}