package br.edu.uva.tcc.monitoramento.web;

import br.edu.uva.tcc.monitoramento.dto.AlertaResponse;
import br.edu.uva.tcc.monitoramento.dto.DispositivoStatus;
import br.edu.uva.tcc.monitoramento.dto.LeituraRequest;
import br.edu.uva.tcc.monitoramento.dto.LeituraResponse;
import br.edu.uva.tcc.monitoramento.dto.RegistroResponse;
import br.edu.uva.tcc.monitoramento.service.MonitoramentoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class MonitoramentoController {

    private static final int LIMITE_MAXIMO = 500;

    private final MonitoramentoService service;

    public MonitoramentoController(MonitoramentoService service) {
        this.service = service;
    }

    /** Endpoint que o ESP32 chama a cada ~5 s. */
    @PostMapping("/leituras")
    @ResponseStatus(HttpStatus.CREATED)
    public RegistroResponse registrar(@Valid @RequestBody LeituraRequest req) {
        return service.registrar(req);
    }

    @GetMapping("/leituras")
    public List<LeituraResponse> leituras(
            @RequestParam(required = false) String dispositivoId,
            @RequestParam(defaultValue = "50") int limite) {
        return service.listarLeituras(dispositivoId, limitar(limite));
    }

    @GetMapping("/alertas")
    public List<AlertaResponse> alertas(
            @RequestParam(required = false) String dispositivoId,
            @RequestParam(defaultValue = "50") int limite) {
        return service.listarAlertas(dispositivoId, limitar(limite));
    }

    @GetMapping("/dispositivos/{id}/status")
    public DispositivoStatus status(@PathVariable String id) {
        return service.status(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Dispositivo sem leituras: " + id));
    }

    private static int limitar(int limite) {
        return Math.max(1, Math.min(limite, LIMITE_MAXIMO));
    }
}
