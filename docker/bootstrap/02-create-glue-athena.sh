#!/usr/bin/env bash
set -euo pipefail

DB="datalake_db"
TABLE="sales"
BUCKET="datalake"
WORKGROUP="wg_local"
OUTPUT="s3://${BUCKET}/athena-results/"

echo "[BOOT] Criando Glue Database/Tabela e Athena WorkGroup..."

# Database
awslocal glue create-database --database-input "{\"Name\":\"${DB}\"}" || true

# Tabela (CSV simples)
awslocal glue create-table --database-name "${DB}" --table-input "{
  \"Name\": \"${TABLE}\",
  \"TableType\": \"EXTERNAL_TABLE\",
  \"Parameters\": {\"classification\":\"csv\",\"skip.header.line.count\":\"1\"},
  \"StorageDescriptor\": {
    \"Columns\": [
      {\"Name\":\"order_id\",\"Type\":\"string\"},
      {\"Name\":\"sku\",\"Type\":\"string\"},
      {\"Name\":\"amount\",\"Type\":\"double\"},
      {\"Name\":\"ts\",\"Type\":\"string\"}
    ],
    \"Location\":\"s3://${BUCKET}/raw/sales/\",
    \"InputFormat\":\"org.apache.hadoop.mapred.TextInputFormat\",
    \"OutputFormat\":\"org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat\",
    \"SerdeInfo\": {\"SerializationLibrary\":\"org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe\",\"Parameters\":{\"field.delim\":\",\"}}
  },
  \"PartitionKeys\":[]
}" || true

# WorkGroup + pasta de resultados
awslocal athena create-work-group --name "${WORKGROUP}" \
  --configuration "ResultConfiguration={OutputLocation=${OUTPUT}}" \
  --description "Local workgroup" || true

echo "[BOOT] OK"