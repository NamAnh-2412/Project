package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;

import vn.quanlybanhang.service.KitchenService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/bep")
public class KitchenController extends BaseController {

    private static final long serialVersionUID = 1L;
    private final KitchenService kitchenService = new KitchenService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("pageTitle", "Màn bếp");
            applyPageChrome(request, "fas fa-blender", "Đơn đang chờ pha chế. Màn hình tự làm mới.", null);
            request.setAttribute("tickets", kitchenService.kitchen());
            render(request, response, "/WEB-INF/views/bep/index.jsp");
        } catch (SQLException ex) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            kitchenService.kitchenAction(
                    paramOrEmpty(request, "action"),
                    getIntParameter(request, "order_id", 0),
                    getIntParameter(request, "item_id", 0));
        } catch (SQLException ex) {
            setFlash(request, "error", ex.getMessage());
        }
        redirect(request, response, "/bep");
    }
}
