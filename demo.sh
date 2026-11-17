#!/usr/bin/env bash
# End-to-end demo of the GlobalTrust Secure Bank security controls.
# Usage:  ./demo.sh  [base-url]      (default http://localhost:8080)
BASE="${1:-http://localhost:8080}"
PW="password"

line() { printf '\n\033[1;34m== %s ==\033[0m\n' "$1"; }
call() { # method user path [json]
  local method="$1" user="$2" path="$3" data="$4"
  printf '\033[0;36m%s %s  (as %s)\033[0m\n' "$method" "$path" "$user"
  if [ -n "$data" ]; then
    curl -s -u "$user:$PW" -X "$method" "$BASE$path" \
         -H 'Content-Type: application/json' -d "$data" -w '\nHTTP %{http_code}\n'
  else
    curl -s -u "$user:$PW" -X "$method" "$BASE$path" -w '\nHTTP %{http_code}\n'
  fi
}
extract_id() { sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1; }

line "A. Authentication + RBAC"
call GET alice /api/me
call GET alice /api/audit            # expect 403
call GET aria  /api/audit            # expect 200

line "B. Clark-Wilson: small transfer auto-executes (<= 1000)"
call POST alice /api/transfers '{"fromAccount":"ACC-1001","toAccount":"ACC-1002","amount":500,"reason":"rent"}'

line "C. Clark-Wilson: maker-checker + separation of duty"
PENDING=$(curl -s -u "victor:$PW" -X POST "$BASE/api/transfers" \
  -H 'Content-Type: application/json' \
  -d '{"fromAccount":"ACC-9000","toAccount":"ACC-1001","amount":2500,"reason":"ops top-up"}')
echo "created: $PENDING"
ID=$(echo "$PENDING" | extract_id)
echo "pending transfer id = $ID"
call POST victor "/api/transfers/$ID/approve"   # expect 403 SoD (maker == checker)
call POST wendy  "/api/transfers/$ID/approve"   # expect 200 EXECUTED

line "D. Chinese Wall (Brewer-Nash)"
call GET diana /api/advisory/companies/1   # Acme  (TECH)   -> granted
call GET diana /api/advisory/companies/2   # Globex(TECH)   -> 403 wall
call GET diana /api/advisory/companies/3   # Initech(PHARMA)-> granted

line "E. Admin separation of duty"
call POST adam /api/admin/users '{"username":"carol","password":"password","fullName":"Carol Customer","role":"CUSTOMER","department":"RETAIL"}'
call POST adam /api/transfers '{"fromAccount":"ACC-9000","toAccount":"ACC-1001","amount":100,"reason":"should fail"}'  # expect 403

line "F. Audit trail (newest first)"
call GET aria /api/audit
