#!/bin/bash
BASE=http://192.168.101.13:8080/api/v1

comprobar () {
   if [ "$2" = "$3" ]; then
      echo "OK  - $1"
   else 
      echo "FALLO   - $1 (esperaba $2, recibio $3)"
      cat /tmp/body.txt
   fi
}

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" $BASE/tickets/mine)
comprobar "Sin token no entra" 401 $CODIGO

TOKEN=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d "{\"email\":\"ana@test.com\",\"password\":\"$PASS_ANA\"}" | jq -r '.token')

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets/mine)
comprobar "Con token ve sus tickets" 200 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets)
comprobar "Con token pero no puede listar todos los ticket, no tiene permisos" 403 $CODIGO

ID=$(curl -sS -X POST $BASE/tickets -H "Authorization: Bearer $TOKEN" \
 -H "Content-Type: application/json" \
 -d '{"title":"Ticket de Ana","description":"Descripcion de prueba larga","type":"INCIDENTE","priority":"ALTA","categoryId":1}' | jq -r '.id')

TOKEN_LUIS=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d "{\"email\":\"luis@test.com\",\"password\":\"$PASS_LUIS\"}" | jq -r '.token')

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -H "Authorization: Bearer $TOKEN" $BASE/tickets/$ID)
comprobar "Ana ve su propio ticket" 200 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -H "Authorization: Bearer $TOKEN_LUIS" $BASE/tickets/$ID)
comprobar "Luis no ve el ticket de Ana" 404 $CODIGO

TOKEN_CRISTIAN=$(curl -sS -X POST $BASE/auth/login -H "Content-Type: application/json" -d "{\"email\":\"cristian@test.com\",\"password\":\"$PASS_CRISTIAN\"}" | jq -r '.token')
ID_CRISTIAN=$(curl -s -H "Authorization: Bearer $TOKEN_CRISTIAN" $BASE/users/me | jq -r '.id')

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -H "Authorization: Bearer $TOKEN_CRISTIAN" $BASE/tickets/$ID)
comprobar "Cristian como agente puede listar todos los tickets" 200 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID/assign \
  -H "Authorization: Bearer $TOKEN_CRISTIAN" \
  -H "Content-Type: application/json" \
  -d "{\"agentId\": $ID_CRISTIAN}") 
comprobar "Cristian como agente puede asignar casos" 200 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID/cancel \
  -H "Authorization: Bearer $TOKEN_LUIS")
comprobar "Luis no puede cancelar ticket de Ana" 403 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID/cancel \
 -H "Authorization: Bearer $TOKEN_CRISTIAN")
comprobar "Cristian como agente no puede cancelar ticket de Ana" 403 $CODIGO

ID_2=$(curl -sS -X POST $BASE/tickets -H "Authorization: Bearer $TOKEN" \
 -H "Content-Type: application/json" \
 -d '{"title":"Ticket 2 de Ana","description":"Descripcion de nuevo ticket de Ana","type":"INCIDENTE","priority":"ALTA","categoryId":1}' | jq -r '.id')

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID/cancel \
 -H "Authorization: Bearer $TOKEN")
comprobar "Ana no puede cancelar ticket en estado EN_PROGRESO" 400 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID_2/cancel \
 -H "Authorization: Bearer $TOKEN")
comprobar "Ana cancela su ticket abierto" 200 $CODIGO

CODIGO=$(curl -s -o /tmp/body.txt -w "%{http_code}" -X PATCH $BASE/tickets/$ID_2/cancel \
 -H "Authorization: Bearer $TOKEN")
comprobar "Ana no puede cancelar un ticket ya cancelado" 400 $CODIGO

