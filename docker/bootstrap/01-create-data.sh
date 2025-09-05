#!/usr/bin/env bash
set -euo pipefail

echo "[BOOT] Criando bucket e carregando dados de exemplo..."
awslocal s3 mb s3://datalake || true
awslocal s3 cp /data/sales.csv s3://datalake/raw/sales/sales.csv
echo "[BOOT] OK"