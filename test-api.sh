#!/bin/bash
BASE=http://192.168.101.15:8080/api/v1

comprobar () {
   if [ "$2" = "$3" ]; then
      echo "OK  - $1"
   else 
      echo "FALLO   - $1 (esperaba $2, recibio $3)"
   fi
}

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" $BASE/tickets/mine)
comprobar "Sin token no entra" 401 $CODIGO

TOKEN=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d '{"email":"ana@test.com","password":"Clave123!"}' | jq -r '.token')

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets/mine)
comprobar "Con token ve sus tickets" 200 $CODIGO

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets)
comprobar "Con token pero no puede listar todos los ticket, no tiene permisos" 403 $CODIGO

ID=$(curl -sS -X POST $BASE/tickets -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"title":"Ticket de Ana","description":"Descripcion de prueba larga","type":"INCIDENTE","priority":"ALTA","categoryId":1}' | jq -r '.id')

TOKEN_LUIS=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d '{"email":"luis@test.com","password":"Clave123!"}' | jq -r '.token')

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets/$ID)
comprobar "Ana ve su propio ticket" 200 $CODIGO

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN_LUIS" $BASE/tickets/$ID)
comprobar "Luis no ve el ticket de Ana" 404 $CODIGO

TOKEN_CRISTIAN=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d '{"email":"cristian@test.com","password":"Clave1234!"}' | jq -r '.token')

CODIGO=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer $TOKEN_CRISTIAN" $BASE/tickets/$ID)
comprobar "Cristian como agente puede listar todos los tickets" 200 $CODIGO
