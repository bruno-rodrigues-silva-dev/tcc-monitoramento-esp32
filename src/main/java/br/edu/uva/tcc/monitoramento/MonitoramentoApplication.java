package br.edu.uva.tcc.monitoramento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class MonitoramentoApplication {

    public static void main(String[] args) {
        // Permite rodar em JDKs mais novos que o suportado oficialmente pelo Byte Buddy (usado pelo Hibernate).
        // O projeto foi feito para o JDK 21; esta linha evita erro caso o IntelliJ use o JDK 24.
        System.setProperty("net.bytebuddy.experimental", "true");
        SpringApplication.run(MonitoramentoApplication.class, args);
    }
}
