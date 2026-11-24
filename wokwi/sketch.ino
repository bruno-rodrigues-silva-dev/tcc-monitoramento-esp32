#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <HTTPClient.h>
#include <OneWire.h>
#include <DallasTemperature.h>

// ---------- CONFIGURAÇÃO ----------
const char* WIFI_SSID = "Wokwi-GUEST";
const char* WIFI_PASS = "";
const char* URL_API   = "https://marshall-volunteer-among-here.trycloudflare.com/api/leituras";
const char* DISPOSITIVO_ID = "esp32-01";

const float CORRENTE_MAX_A   = 5.0;   // ACS712 5 A, igual à faixa da API
const float LIMITE_TEMP_C    = 50.0;  // alerta da API
const float LIMITE_CORRENTE  = 1.6;   // alerta da API
const unsigned long INTERVALO_MS = 5000;
// ----------------------------------

#define PINO_TEMPERATURA 4
#define PINO_POTENCIOMETRO 34

OneWire oneWire(PINO_TEMPERATURA);
DallasTemperature sensorTemperatura(&oneWire);

void conectarWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASS, 6);
  Serial.print("Conectando ao WiFi");
  while (WiFi.status() != WL_CONNECTED) {
    delay(250);
    Serial.print(".");
  }
  Serial.println(" conectado!");
}

void enviarLeitura(float temperatura, float corrente) {
  if (WiFi.status() != WL_CONNECTED) {
    conectarWiFi();
  }

  WiFiClientSecure cliente;
  cliente.setInsecure();  // simulação: sem validar certificado

  HTTPClient http;
  http.begin(cliente, URL_API);
  http.addHeader("Content-Type", "application/json");

  String corpo = "{\"dispositivoId\":\"" + String(DISPOSITIVO_ID) + "\"" +
                 ",\"temperaturaC\":" + String(temperatura, 2) +
                 ",\"correnteA\":" + String(corrente, 3) + "}";

  int codigo = http.POST(corpo);
  Serial.print("HTTP: ");
  Serial.println(codigo);
  http.end();
}

void setup() {
  Serial.begin(115200);
  sensorTemperatura.begin();
  analogReadResolution(12);

  Serial.println("================================");
  Serial.println(" SISTEMA DE MONITORAMENTO ESP32");
  Serial.println("================================");
  conectarWiFi();
  Serial.println("Sistema iniciado!");
}

void loop() {
  sensorTemperatura.requestTemperatures();
  float temperatura = sensorTemperatura.getTempCByIndex(0);

  int adc = analogRead(PINO_POTENCIOMETRO);
  float corrente = (adc / 4095.0) * CORRENTE_MAX_A;

  Serial.println("-------------------------------");

  if (temperatura == DEVICE_DISCONNECTED_C) {
    Serial.println("ERRO: Sensor de temperatura!");
  } else {
    Serial.print("T=");
    Serial.print(temperatura, 2);
    Serial.print(" C  I=");
    Serial.print(corrente, 3);
    Serial.println(" A");

    if (temperatura >= LIMITE_TEMP_C || corrente >= LIMITE_CORRENTE) {
      Serial.println("ALERTA: ANORMALIDADE DETECTADA!");
    } else {
      Serial.println("STATUS: FUNCIONAMENTO NORMAL");
    }

    enviarLeitura(temperatura, corrente);
  }

  delay(INTERVALO_MS);
}