package vn.quanlybanhang.listener;

import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import vn.quanlybanhang.config.DatabaseConfig;
import vn.quanlybanhang.config.SchemaBootstrap;
import vn.quanlybanhang.config.UploadConfig;
import vn.quanlybanhang.service.InsightService;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    private ScheduledExecutorService reports;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        UploadConfig.initialize(sce.getServletContext());
        try {
            SchemaBootstrap.ensure();
        } catch (SQLException ex) {
            sce.getServletContext().log("Khong tao duoc bang ca lam viec / ma giam gia", ex);
        }
        InsightService insightService = new InsightService();
        reports = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "qlbh-daily-report");
            thread.setDaemon(true);
            return thread;
        });
        reports.scheduleAtFixedRate(() -> {
            try {
                insightService.sendScheduledReport();
            } catch (Exception ex) {
                sce.getServletContext().log("Khong gui duoc bao cao Telegram", ex);
            }
        }, 5, 15, TimeUnit.MINUTES);
        sce.getServletContext().log("QuanLyBanHang application started. Upload dir: "
                + UploadConfig.getUploadRoot());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (reports != null) {
            reports.shutdownNow();
        }
        DatabaseConfig.shutdown();
        sce.getServletContext().log("QuanLyBanHang application stopped.");
    }
}
