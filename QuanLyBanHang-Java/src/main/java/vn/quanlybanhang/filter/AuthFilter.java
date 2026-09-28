package vn.quanlybanhang.filter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import vn.quanlybanhang.service.SettingsService;
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

        if (isPublicPath(path)) {
            ViewUtil.applySharedAttributes(httpRequest);
            chain.doFilter(request, response);
            return;
        }

        if (!SessionUtil.isLoggedIn(httpRequest)) {
            httpResponse.sendRedirect(contextPath + "/login");
            return;
        }

        try {
            int timeoutMinutes = settingsService.getSessionTimeoutMinutes();
            if (SessionUtil.isSessionTimedOut(httpRequest, timeoutMinutes)) {
                SessionUtil.logout(httpRequest);
                httpResponse.sendRedirect(contextPath + "/login?timeout=1");
                return;
            }
        } catch (SQLException ex) {
            SessionUtil.touchSession(httpRequest);
        }

        SessionUtil.touchSession(httpRequest);
        ViewUtil.applySharedAttributes(httpRequest);
        chain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        if (path.startsWith("/assets/")) {
            return true;
        }
        if (path.startsWith("/js/")) {
            return true;
        }
        if (path.startsWith("/uploads/")) {
            return true;
        }
        return PUBLIC_PATHS.contains(path);
    }

    @Override
    public void destroy() {
    }
}
