package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import vn.quanlybanhang.util.JsonUtil;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet API JSON dung chung: tra loi success/error thong nhat.
 */
public abstract class BaseApiController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void writeSuccess(HttpServletResponse response) throws IOException {
        writeSuccess(response, Map.of("success", true));
    }

    protected void writeSuccess(HttpServletResponse response, Map<String, Object> data) throws IOException {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("success", true);
        payload.putAll(data);
        JsonUtil.writeJson(response, payload);
    }

    protected void writeError(HttpServletResponse response, String message) throws IOException {
        writeError(response, HttpServletResponse.SC_BAD_REQUEST, message);
    }

    protected void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonUtil.writeJson(response, Map.of("success", false, "error", message));
    }

    protected void handleSqlError(HttpServletResponse response, SQLException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        JsonUtil.writeJson(response, Map.of(
                "success", false,
                "error", ex.getMessage() == null ? "Loi he thong" : ex.getMessage()));
    }
}
