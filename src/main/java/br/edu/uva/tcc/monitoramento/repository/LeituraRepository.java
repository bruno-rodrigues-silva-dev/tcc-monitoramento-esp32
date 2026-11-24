package br.edu.uva.tcc.monitoramento.repository;

import br.edu.uva.tcc.monitoramento.domain.Leitura;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeituraRepository extends JpaRepository<Leitura, Long> {

    List<Leitura> findByDispositivoIdOrderByInstanteDesc(String dispositivoId, Pageable pageable);

    List<Leitura> findAllByOrderByInstanteDesc(Pageable pageable);
}
