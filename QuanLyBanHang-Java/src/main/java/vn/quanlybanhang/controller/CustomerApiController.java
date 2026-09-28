package vn.quanlybanhang.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

import vn.quanlybanhang.service.CustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/api/search-customer")
public class CustomerApiController extends BaseApiController {

    private static final long serialVersionUID = 1L;

    private final CustomerService customerService = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            var customers = customerService.searchCustomersForApi(
                    request.getParameter("q"), request.getContextPath());
            writeSuccess(response, Map.of("customers", customers));
        } catch (SQLException ex) {
            handleSqlError(response, ex);
        }
    }
}
