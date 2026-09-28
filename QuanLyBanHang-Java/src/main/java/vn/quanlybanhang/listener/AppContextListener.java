package vn.quanlybanhang.listener;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.config.UploadConfig;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        UploadConfig.initialize(sce.getServletContext());
        sce.getServletContext().log("QuanLyBanHang application started. Upload dir: "
                + UploadConfig.getUploadRoot());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseConfig.shutdown();
        sce.getServletContext().log("QuanLyBanHang application stopped.");
    }
}
