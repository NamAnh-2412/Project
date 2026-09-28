<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/home/home-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
            <%@ include file="/WEB-INF/views/layout/page-header.jspf" %>

            <%@ include file="/WEB-INF/views/home/home-stats.jspf" %>
            <%@ include file="/WEB-INF/views/home/home-chart.jspf" %>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
