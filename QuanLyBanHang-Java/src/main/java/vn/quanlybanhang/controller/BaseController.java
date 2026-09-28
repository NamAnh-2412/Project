package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import vn.quanlybanhang.service.SettingsService;
import vn.quanlybanhang.util.RequestUtil;
import vn.quanlybanhang.util.ViewUtil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public abstract class BaseController extends HttpServlet {

    private final SettingsService settingsService = new SettingsService();

    protected void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        applyCommonAttributes(request);
        RequestDispatcher dispatcher = request.getRequestDispatcher(view);
        dispatcher.forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * Gan icon + mo ta + stylesheet theo trang (giong layout PHP / page-header.jspf).
     */
    protected void applyPageChrome(HttpServletRequest request, String pageHeaderIcon, String headerSubtitle,
            List<String> extraCss) {
        if (pageHeaderIcon != null && !pageHeaderIcon.isBlank()) {
            request.setAttribute("pageHeaderIcon", pageHeaderIcon);
        }
        if (extraCss != null && !extraCss.isEmpty()) {
            request.setAttribute("extraCss", extraCss);
        }
    }

    protected int getIntParameter(HttpServletRequest request, String name, int defaultValue) {
        return RequestUtil.getInt(request, name, defaultValue);
    }

    protected Integer getNullableIntParameter(HttpServletRequest request, String name) {
        return RequestUtil.getNullableInt(request, name);
    }

    protected String paramOrEmpty(HttpServletRequest request, String name) {
        return RequestUtil.paramOrEmpty(request, name);
    }

    protected String trimOrEmpty(String value) {
        return RequestUtil.trimOrEmpty(value);
    }

    protected int getPasswordMinLength() {
        try {
            return settingsService.getPasswordMinLength();
        } catch (SQLException ex) {
            return 6;
        }
    }

    protected void setFlash(HttpServletRequest request, String type, String message) {
        request.getSession(true).setAttribute("flashType", type);
        request.getSession(true).setAttribute("flashMessage", message);
    }

    protected void exposeFlash(HttpServletRequest request) {
        request.setAttribute("flashType", request.getSession().getAttribute("flashType"));
        request.setAttribute("flashMessage", request.getSession().getAttribute("flashMessage"));
        request.getSession().removeAttribute("flashType");
        request.getSession().removeAttribute("flashMessage");
    }

    private void applyCommonAttributes(HttpServletRequest request) {
        ViewUtil.applySharedAttributes(request);
        exposeFlash(request);
    }
}
