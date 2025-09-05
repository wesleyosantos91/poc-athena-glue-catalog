# listagem simples
curl --get 'http://localhost:8080/athena/query' \
  --data-urlencode 'sql=SELECT * FROM "datalake_db"."sales" LIMIT 5'

# agregação de exemplo
curl --get 'http://localhost:8080/athena/query' \
  --data-urlencode 'sql=SELECT sku, SUM(amount) AS total FROM "datalake_db"."sales" GROUP BY sku ORDER BY total DESC LIMIT 10'
