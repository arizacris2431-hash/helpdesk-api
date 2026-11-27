#!/bin/bash

set -euo pipefail
SERVER=${SERVER:?falta SERVER (usuario@IP)}

JAR=target/helpDesk-0.0.1-SNAPSHOT.jar
NOMBRE=$(basename "$JAR")

# 1. construir
./mvnw clean package

# 2. calcular la huella local
HUELLA=$(sha256sum "$JAR" | cut -d' ' -f1)

# 3. copiar con scp
scp "$JAR" "$SERVER:~/"

# 4. en el servidor: verificar huella, parar, respaldar, copiar, restorecon, arrancar, status
ssh -t "$SERVER" "echo '$HUELLA  $NOMBRE' | sha256sum -c - && \
sudo systemctl stop helpdesk && \
sudo cp /opt/helpdesk/helpdesk.jar /opt/helpdesk/helpdesk.jar.bak && \
sudo cp ~/$NOMBRE /opt/helpdesk/helpdesk.jar && \
sudo restorecon -v /opt/helpdesk/helpdesk.jar && \
sudo  systemctl start helpdesk && \
systemctl status helpdesk --no-pager"
