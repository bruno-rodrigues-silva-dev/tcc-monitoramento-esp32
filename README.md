# Monitoramento de temperatura e corrente com ESP32 — Backend (TCC)

API em Spring Boot 3 (Java 21) + PostgreSQL. Recebe as leituras do ESP32 (DS18B20 + ACS712),
grava no banco e gera alertas por limites fixos, anomalia estatística e falha de comunicação.

## Como rodar (IntelliJ)

1. Abra o **Docker Desktop** e espere iniciar.
2. No IntelliJ: **Open** > escolha esta pasta (ou o `pom.xml`) > *Trust Project*. Aguarde o Maven baixar as dependências.
3. Clique no botão verde da configuração **Monitoramento (API)** (ou ao lado do `main` em `MonitoramentoApplication`).
   O PostgreSQL sobe sozinho pelo `docker-compose.yml`.
4. Teste: `http://localhost:8080/api/alertas` deve mostrar `[]`.
5. Para o Wokwi: dê dois cliques em `iniciar-tunel.bat` e copie a URL `https://....trycloudflare.com`.

JDK recomendado: **21** (File > Project Structure > SDK > Download JDK). Testes: botão direito em `DetectorAnomaliaTest` > Run.

Sem IntelliJ: `docker compose up -d` e `mvn spring-boot:run`.

Variáveis opcionais: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `PORT`.
Os limites ficam em `src/main/resources/application.yml` (bloco `monitoramento`).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/leituras` | ESP32 envia uma leitura (retorna 201 + alertas gerados) |
| GET | `/api/leituras?dispositivoId=&limite=50` | Últimas leituras (máx. 500) |
| GET | `/api/alertas?dispositivoId=&limite=50` | Últimos alertas |
| GET | `/api/dispositivos/{id}/status` | Estado atual do dispositivo |

Corpo do POST (contrato que o ESP32 deve enviar a cada ~5 s):

```json
{ "dispositivoId": "esp32-01", "temperaturaC": 31.5, "correnteA": 1.02 }
```

Faixas aceitas: temperatura de -55 a 125 °C (DS18B20), corrente de 0 a 5 A RMS (ACS712 5 A).
Fora disso a API responde 400.

## Regras de detecção (`DetectorAnomalia`)

- **Temperatura:** alerta ≥ 50 °C, crítico ≥ 60 °C, histerese de 2 °C (só volta ao nível anterior abaixo de limite − 2).
- **Corrente:** alerta ≥ 1,6 A, crítico ≥ 2,0 A.
- **Estatística:** corrente > média + 3 desvios-padrão + 0,05 A por 3 leituras seguidas, calculada sobre uma janela de até 120 leituras normais (mínimo de 30 para valer). Leituras anômalas não entram na linha de base.
- **Comunicação:** se um dispositivo ficar 15 s sem enviar, gera `FALHA_COMUNICACAO` (verificação a cada 5 s).
- Cada alerta é gerado uma vez por subida de nível, sem repetir a cada leitura.

## Testes rápidos com curl

```bash
URL=http://localhost:8080/api/leituras
# normal
curl -s -X POST $URL -H 'Content-Type: application/json' -d '{"dispositivoId":"esp32-01","temperaturaC":30,"correnteA":1.0}'
# temperatura em alerta (>= 50) e crítica (>= 60)
curl -s -X POST $URL -H 'Content-Type: application/json' -d '{"dispositivoId":"esp32-01","temperaturaC":51,"correnteA":1.0}'
curl -s -X POST $URL -H 'Content-Type: application/json' -d '{"dispositivoId":"esp32-01","temperaturaC":61,"correnteA":1.0}'
# corrente em alerta (>= 1,6) e crítica (>= 2,0)
curl -s -X POST $URL -H 'Content-Type: application/json' -d '{"dispositivoId":"esp32-01","temperaturaC":30,"correnteA":1.7}'
curl -s -X POST $URL -H 'Content-Type: application/json' -d '{"dispositivoId":"esp32-01","temperaturaC":30,"correnteA":2.1}'
# falha de comunicação: pare de enviar e espere mais de 15 s
curl -s http://localhost:8080/api/alertas
curl -s http://localhost:8080/api/dispositivos/esp32-01/status
```

Para a anomalia estatística, envie ~30 leituras com corrente estável (ex.: ~1,0 A) e depois 3 leituras
acima do padrão porém abaixo de 1,6 A (ex.: 1,5 A).

## Limitações conhecidas (citar no TCC)

- O estado do detector (linha de base, níveis) fica em memória: ao reiniciar a API, a linha de base é refeita do zero.
- Sem autenticação: adequado para protótipo acadêmico, não para produção.
- O tempo da leitura é o do servidor (o ESP32 não envia timestamp).
