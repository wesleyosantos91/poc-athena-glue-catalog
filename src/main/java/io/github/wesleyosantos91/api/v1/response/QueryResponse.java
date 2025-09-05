package io.github.wesleyosantos91.api.v1.response;

import java.util.List;
import java.util.Map;

public class QueryResponse {

    private String queryExecutionId;
    private int count;
    private List<Map<String, String>> rows;

    public QueryResponse() {
    }

    public QueryResponse(String queryExecutionId, List<Map<String, String>> rows) {
        this.queryExecutionId = queryExecutionId;
        this.rows = rows;
        this.count = rows != null ? rows.size() : 0;
    }

    public String getQueryExecutionId() {
        return queryExecutionId;
    }

    public void setQueryExecutionId(String queryExecutionId) {
        this.queryExecutionId = queryExecutionId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Map<String, String>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, String>> rows) {
        this.rows = rows;
    }
}
