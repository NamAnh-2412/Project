package vn.quanlybanhang.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static void writeJson(HttpServletResponse response, Object payload) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        OBJECT_MAPPER.writeValue(response.getOutputStream(), payload);
    }
}
