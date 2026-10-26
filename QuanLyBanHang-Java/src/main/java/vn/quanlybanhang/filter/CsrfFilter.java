package vn.quanlybanhang.filter;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;

import vn.quanlybanhang.util.RequestUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class CsrfFilter implements Filter {

    static final String TOKEN_KEY = "csrf_token";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> UNSAFE = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (isStatic(httpRequest) || isMachineCall(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }
        String token = ensureToken(httpRequest);
        if (UNSAFE.contains(httpRequest.getMethod().toUpperCase()) && !matches(httpRequest, token)) {
            reject(httpRequest, httpResponse);
            return;
        }
        chain.doFilter(request, response);
    }

    private static boolean isStatic(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        return path.startsWith("/assets/") || path.startsWith("/js/") || path.startsWith("/uploads/");
    }

    private static boolean isMachineCall(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;
        return "/api/integrations/delivery-webhook".equals(path);
    }

    private static String ensureToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(TOKEN_KEY);
        if (token == null || token.isBlank()) {
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(TOKEN_KEY, token);
        }
        request.setAttribute("csrfToken", token);
        return token;
    }

    private static boolean matches(HttpServletRequest request, String expected) {
        String actual = request.getHeader("X-CSRF-Token");
        if (actual == null || actual.isBlank()) {
            actual = request.getParameter("_csrf");
        }
        if (actual == null || expected == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                actual.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (RequestUtil.isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Phiên làm việc không hợp lệ. Hãy tải lại trang.\",\"code\":\"CSRF\"}");
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write("Phiên làm việc không hợp lệ. Hãy tải lại trang rồi thử lại.");
    }

    @Override
    public void destroy() {
    }
}
