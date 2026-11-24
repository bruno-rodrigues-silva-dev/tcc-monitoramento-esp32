@echo off
REM Abre um tunel publico para a API (porta 8080), para o Wokwi conseguir enviar as leituras.
where cloudflared >nul 2>nul
if errorlevel 1 (
  echo Instalando o cloudflared...
  winget install --id Cloudflare.cloudflared -e
  echo.
  echo Instalado. FECHE esta janela e de dois cliques em iniciar-tunel.bat de novo.
  pause
  exit /b
)
echo Copie a URL https://....trycloudflare.com que aparecer abaixo e cole no API_URL do Wokwi.
echo (Mantenha esta janela aberta durante os testes.)
echo.
cloudflared tunnel --url http://localhost:8080
pause
