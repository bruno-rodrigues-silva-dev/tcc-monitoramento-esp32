# Monitoramento de Temperatura e Corrente com ESP32

## Backend — Trabalho de Conclusão de Curso (TCC)

API desenvolvida em Spring Boot 3 (Java 21) e PostgreSQL para monitoramento de temperatura e corrente elétrica utilizando ESP32, sensor DS18B20 e sensor ACS712.

O sistema recebe as leituras dos sensores, armazena os dados no banco de dados e gera alertas por limites fixos, anomalias estatísticas e falhas de comunicação.

## Tecnologias utilizadas

- Java 21
- Spring Boot 3
- PostgreSQL
- Docker Compose
- ESP32
- Sensor de temperatura DS18B20
- Sensor de corrente ACS712
- Maven

## Como executar pelo IntelliJ

1. Abra o Docker Desktop e aguarde a inicialização.
2. Abra o projeto no IntelliJ, selecionando a pasta do projeto ou o arquivo `pom.xml`.
3. Aguarde o Maven baixar as dependências.
4. Execute a classe `MonitoramentoApplication` pelo botão verde.
5. O PostgreSQL será iniciado automaticamente pelo Docker Compose.
6. A API ficará disponível em `http://localhost:8080`.

Para testar, acesse:

`http://localhost:8080/api/alertas`

## Integração com o ESP32 pelo Wokwi

1. Execute a API pelo IntelliJ.
2. Dê dois cliques no arquivo `iniciar-tunel.bat`.
3. Copie a URL gerada pelo Cloudflare Tunnel.
4. Configure essa URL no código do ESP32 no Wokwi.
5. Execute a simulação e acompanhe as respostas HTTP no Serial Monitor.

O retorno HTTP 201 indica que a leitura foi recebida e registrada com sucesso.

## Configuração

As variáveis de ambiente opcionais são:

- `DB_URL`
- `DB_USER`
- `DB_PASSWORD`
- `PORT`

Os parâmetros de monitoramento estão definidos no arquivo `application.yml`, dentro do bloco `monitoramento`.

## Endpoints da API

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/leituras` | Recebe uma leitura do ESP32 |
| GET | `/api/leituras?dispositivoId=&limite=50` | Consulta as últimas leituras |
| GET | `/api/alertas?dispositivoId=&limite=50` | Consulta os alertas gerados |
| GET | `/api/dispositivos/{id}/status` | Consulta o estado do dispositivo |

### Exemplo de envio de leitura

O ESP32 envia os dados em formato JSON:

```json
{
  "dispositivoId": "esp32-01",
  "temperaturaC": 31.5,
  "correnteA": 1.02
}
```

O envio ocorre aproximadamente a cada 5 segundos.

## Regras de detecção de alertas

### Temperatura

- Alerta: temperatura igual ou superior a 50 °C.
- Crítico: temperatura igual ou superior a 60 °C.
- Histerese: 2 °C para evitar oscilações na classificação dos alertas.

### Corrente elétrica

- Alerta: corrente igual ou superior a 1,6 A.
- Crítico: corrente igual ou superior a 2,0 A.

### Detecção estatística de anomalias

O sistema identifica anomalias quando a corrente ultrapassa a média acrescida de três desvios-padrão e uma margem de 0,05 A, durante três leituras consecutivas.

O cálculo utiliza uma janela de até 120 leituras normais, com mínimo de 30 amostras. Leituras consideradas anômalas não são incorporadas à linha de base.

### Falha de comunicação

Caso o dispositivo fique 15 segundos sem enviar leituras, o sistema gera um alerta de `FALHA_COMUNICACAO`.

A verificação ocorre a cada 5 segundos.

Cada alerta é gerado uma vez por subida de nível, evitando repetições desnecessárias a cada leitura.

## Execução sem IntelliJ

Com Docker e Maven instalados, execute:

```bash
docker compose up -d
mvn spring-boot:run
```

## Testes

Os testes automatizados podem ser executados pelo IntelliJ, clicando com o botão direito na classe `DetectorAnomaliaTest` e selecionando Run.

## Limitações conhecidas

- O estado do detector, incluindo a linha de base estatística e os níveis de alerta, fica armazenado em memória. Ao reiniciar a API, a linha de base é refeita.
- O sistema não possui autenticação, pois foi desenvolvido como protótipo acadêmico.
- O horário das leituras é registrado pelo servidor, não pelo ESP32.

## Objetivo do projeto

Desenvolver um sistema de monitoramento de temperatura e corrente elétrica utilizando ESP32, com comunicação entre hardware e backend, armazenamento em banco de dados e identificação automática de condições anormais por meio de regras de detecção e análise estatística.