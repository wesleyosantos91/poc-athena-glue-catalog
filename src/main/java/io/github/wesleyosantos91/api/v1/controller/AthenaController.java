package io.github.wesleyosantos91.api.v1.controller;

import io.github.wesleyosantos91.api.v1.response.QueryResponse;
import io.github.wesleyosantos91.domain.service.AthenaQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/athena")
public class AthenaController {

    private static final Logger log = LoggerFactory.getLogger(AthenaQueryService.class);

    private final AthenaQueryService service;

    public AthenaController(AthenaQueryService service) {
        this.service = service;
    }

    @GetMapping("/query")
    public ResponseEntity<QueryResponse> query(@RequestParam("sql") String sql) {
        validate(sql);
        var result = service.execute(sql);
        log.info("Query: {}", sql);
        log.info("Result: {}", result);
        return ResponseEntity.ok(new QueryResponse(result.queryExecutionId(), result.rows()));
    }

    private void validate(String sql) {

        if (!StringUtils.hasText(sql)) {
            throw new IllegalArgumentException("sql é obrigatório");
        }
        // PoC: apenas SELECT
        String s = sql.trim().toLowerCase();
        if (!s.startsWith("select")) {
            throw new IllegalArgumentException("Apenas SELECT é permitido nesta PoC");
        }
        if (s.length() > 10_000) {
            throw new IllegalArgumentException("SQL muito grande");
        }
    }
}
