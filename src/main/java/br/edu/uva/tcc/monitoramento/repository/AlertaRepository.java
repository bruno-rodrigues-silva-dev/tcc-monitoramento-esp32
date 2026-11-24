package br.edu.uva.tcc.monitoramento.repository;

import br.edu.uva.tcc.monitoramento.domain.Alerta;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByDispositivoIdOrderByInstanteDesc(String dispositivoId, Pageable pageable);

    List<Alerta> findAllByOrderByInstanteDesc(Pageable pageable);
}
