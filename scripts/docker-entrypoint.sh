#!/bin/sh
set -eu

mask_value() {
  value="${1:-}"
  if [ -z "$value" ]; then
    printf '<empty>'
  else
    printf '<set:%s chars>' "${#value}"
  fi
}

print_env_value() {
  name="$1"
  eval "value=\${$name:-}"
  printf '%s=%s\n' "$name" "$value"
}

print_masked_env_value() {
  name="$1"
  eval "value=\${$name:-}"
  printf '%s=%s\n' "$name" "$(mask_value "$value")"
}

echo "=== NewsBack runtime environment ==="
print_env_value SPRING_PROFILES_ACTIVE
print_env_value DB_URL
print_env_value DB_USERNAME
print_masked_env_value DB_PASSWORD
print_env_value AI_SERVER_URL
print_env_value BACKEND_URL
print_env_value FIREBASE_CREDENTIAL_PATH
print_masked_env_value JWT_SECRET
print_env_value JWT_ACCESS_TOKEN_EXPIRATION_MINUTES
print_env_value JWT_REFRESH_TOKEN_EXPIRATION_DAYS
print_env_value SPRINGDOC_API_DOCS_ENABLED
print_env_value SPRINGDOC_SWAGGER_UI_ENABLED
echo "===================================="

exec "$@"
