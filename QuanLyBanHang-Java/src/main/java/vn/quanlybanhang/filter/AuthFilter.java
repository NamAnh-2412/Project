package vn.quanlybanhang.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import vn.quanlybanhang.service.SettingsService;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.SessionUtil;
import vn.quanlybanhang.util.ViewUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter("/*")
public class AuthFilter implements Filter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/login",
            "/logout",
            "/register"
    );

    private final SettingsService settingsService = new SettingsService();

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());

        if (isPublicPath(httpRequest, path)) {
            ViewUtil.applySharedAttributes(httpRequest);
            chain.doFilter(request, response);
            return;
        }

        if (!SessionUtil.isLoggedIn(httpRequest)) {
            rejectUnauthenticated(httpRequest, httpResponse, false);
            return;
        }

        try {
            int timeoutMinutes = settingsService.getSessionTimeoutMinutes();
            if (SessionUtil.isSessionTimedOut(httpRequest, timeoutMinutes)) {
                SessionUtil.logout(httpRequest);
                rejectUnauthenticated(httpRequest, httpResponse, true);
                return;
            }
        } catch (SQLException ex) {
            SessionUtil.touchSession(httpRequest);
        }

        SessionUtil.touchSession(httpRequest);
        ViewUtil.applySharedAttributes(httpRequest);
        chain.doFilter(request, response);
    }

    private void rejectUnauthenticated(HttpServletRequest request, HttpServletResponse response, boolean timeout)
            throws IOException {
        if (RequestUtil.isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"error\":\"Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.\",\"code\":\"UNAUTHORIZED\"}");
            return;
        }
        String target = timeout ? "/login?timeout=1" : "/login";
        response.sendRedirect(request.getContextPath() + target);
    }

    private boolean isPublicPath(HttpServletRequest request, String path) {
        if (path.startsWith("/assets/") || path.startsWith("/js/") || path.startsWith("/uploads/")) {
            return true;
        }
        if (path.startsWith("/goi-mon") || path.startsWith("/api/public/") || path.startsWith("/man-hinh-phu")) {
            return true;
        }
        if ("/api/integrations/delivery-webhook".equals(path)) {
            return true;
        }
        if ("/api/cfd".equals(path) && "GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return PUBLIC_PATHS.contains(path);
    }

    @Override
    public void destroy() {
    }
}
