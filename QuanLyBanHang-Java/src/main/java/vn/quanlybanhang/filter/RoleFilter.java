package vn.quanlybanhang.filter;

import java.io.IOException;
import java.util.Set;

import vn.quanlybanhang.constants.AppConstants;
import vn.quanlybanhang.util.SessionUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter({
        "/nhan-vien", "/nhan-vien/create", "/nhan-vien/edit", "/nhan-vien/delete", "/nhan-vien/change-role",
        "/kho/approve", "/kho/kiem-kho/approve", "/caidat", "/api/logo-upload", "/api/logo-delete",
        "/ma-giam-gia", "/ban", "/du-bao", "/chi-nhanh", "/mo-rong"
})
public class RoleFilter implements Filter {

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/caidat",
            "/api/logo-upload",
            "/api/logo-delete");

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String contextPath = httpRequest.getContextPath();
        String path = httpRequest.getRequestURI().substring(contextPath.length());
        String role = SessionUtil.getCurrentRole(httpRequest);

        boolean allowed;
        if (ADMIN_PATHS.contains(path)) {
            allowed = AppConstants.ROLE_ADMIN.equals(role);
        } else {
            allowed = AppConstants.ROLE_MANAGER.equals(role);
        }

        if (!allowed) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Ban khong co quyen truy cap.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
