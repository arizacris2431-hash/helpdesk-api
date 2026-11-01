#!/bin/bash
BASE=http://localhost:8080/api/v1

echo "1) Login"
TOKEN=$(curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cristian@test.com","password":"Password123!"}' | jq -r '.token')

echo "2) Mis tickets (debe dar 200)"
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" $BASE/tickets/mine

echo "3) Todos los tickets como CLIENTE (¿debería dar 403?)"
curl -s -o /dev/null -w "%{http_code}\n" \
  -H "Authorization: Bearer $TOKEN" $BASE/tickets

# Registrar (solo la primera vez; si ya existen, dará 409 y no pasa nada)
curl -s -o /dev/null -X POST $BASE/users -H "Content-Type: application/json" \
  -d '{"name":"Ana","email":"ana@test.com","password":"Clave123!"}'
curl -s -o /dev/null -X POST $BASE/users -H "Content-Type: application/json" \
  -d '{"name":"Luis","email":"luis@test.com","password":"Clave123!"}'

login() {
  curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
    -d "{\"email\":\"$1\",\"password\":\"$2\"}" | jq -r '.token'
}

TOKEN_ANA=$(login ana@test.com Clave123!)
TOKEN_LUIS=$(login luis@test.com Clave123!)

# Ana crea un ticket y guardamos el ID
ID=$(curl -s -X POST $BASE/tickets \
  -H "Authorization: Bearer $TOKEN_ANA" -H "Content-Type: application/json" \
  -d '{"title":"Ticket de Ana","description":"Descripcion de prueba larga","type":"INCIDENTE","priority":"ALTA","categoryId":1}' | jq -r '.id')

echo "Ana pide su ticket (debe dar 200)"
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN_ANA" $BASE/tickets/$ID

echo "Luis pide el ticket de Ana (NO debe dar 200)"
curl -s -o /dev/null -w "%{http_code}\n" -H "Authorization: Bearer $TOKEN_LUIS" $BASE/tickets/$ID
