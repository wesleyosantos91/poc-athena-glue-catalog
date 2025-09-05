package io.github.wesleyosantos91.domain.service;

import io.github.wesleyosantos91.infrastructure.envs.AppProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.athena.AthenaClient;
import software.amazon.awssdk.services.athena.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AthenaQueryService {

    private static final Logger log = LoggerFactory.getLogger(AthenaQueryService.class);

    private final AthenaClient athena;
    private final AppProps props;

    public AthenaQueryService(AthenaClient athena, AppProps props) {
        this.athena = athena;
        this.props = props;
    }

    public String startQuery(String sql, String workgroup, String outputS3, String database) {
        StartQueryExecutionRequest req = StartQueryExecutionRequest.builder()
                .queryString(sql)
                .queryExecutionContext(QueryExecutionContext.builder().database(database).build())
                .workGroup(workgroup)
                .resultConfiguration(ResultConfiguration.builder().outputLocation(outputS3).build())
                .build();

        StartQueryExecutionResponse res = athena.startQueryExecution(req);
        String id = res.queryExecutionId();
        log.info("Athena StartQueryExecution id={}", id);
        return id;
    }

    public void waitForSucceeded(String queryExecutionId) {
        Instant start = Instant.now();
        while (true) {
            GetQueryExecutionResponse r = athena.getQueryExecution(GetQueryExecutionRequest.builder()
                    .queryExecutionId(queryExecutionId)
                    .build());
            QueryExecutionState state = r.queryExecution().status().state();

            if (state == QueryExecutionState.SUCCEEDED) {
                return;
            }
            if (state == QueryExecutionState.FAILED || state == QueryExecutionState.CANCELLED) {
                String reason = Optional.ofNullable(r.queryExecution().status().stateChangeReason()).orElse("Unknown");
                throw new IllegalStateException("Athena query " + state + ": " + reason);
            }
            if (Duration.between(start, Instant.now()).getSeconds() > props.getTimeoutSeconds()) {
                throw new IllegalStateException("Timeout aguardando execução Athena (id=" + queryExecutionId + ")");
            }
            try {
                Thread.sleep(props.getPollIntervalMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Polling interrompido", e);
            }
        }
    }

    public List<Map<String, String>> getResults(String queryExecutionId, int maxRows) {
        List<Map<String, String>> out = new ArrayList<>();
        boolean isFirstPage = true;

        GetQueryResultsRequest request = GetQueryResultsRequest.builder()
                .queryExecutionId(queryExecutionId)
                .maxResults(Math.min(maxRows, 1000)) // limite por página
                .build();

        var paginator = athena.getQueryResultsPaginator(request);

        List<String> columnNames = null;

        for (GetQueryResultsResponse page : paginator) {
            if (columnNames == null) {
                columnNames = page.resultSet()
                        .resultSetMetadata()
                        .columnInfo()
                        .stream()
                        .map(ColumnInfo::name)
                        .collect(Collectors.toList());
            }

            List<Row> rows = page.resultSet().rows();

            // A primeira linha da primeira página costuma ser header
            int startIndex = isFirstPage ? 1 : 0;
            for (int i = startIndex; i < rows.size(); i++) {
                Row row = rows.get(i);
                List<Datum> data = row.data();
                Map<String, String> map = new LinkedHashMap<>();
                for (int c = 0; c < columnNames.size(); c++) {
                    String key = columnNames.get(c);
                    String val = (c < data.size() && data.get(c) != null) ? data.get(c).varCharValue() : null;
                    map.put(key, val);
                }
                out.add(map);
                if (out.size() >= maxRows) return out;
            }
            isFirstPage = false;
        }
        return out;
    }

    public Result execute(String sql) {
        String id = startQuery(sql, props.getWorkgroup(), props.getOutputS3(), props.getDatabase());
        waitForSucceeded(id);
        List<Map<String, String>> rows = getResults(id, props.getMaxRows());
        return new Result(id, rows);
    }

    public record Result(String queryExecutionId, List<Map<String, String>> rows) {}
}