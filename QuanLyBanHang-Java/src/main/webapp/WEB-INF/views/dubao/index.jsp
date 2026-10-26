<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/layout/module-head.jspf" %>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
<%@ include file="/WEB-INF/views/layout/page-header.jspf" %>
<%@ include file="/WEB-INF/views/layout/flash-notice.jspf" %>
<div class="content-card section-gap">
    <h3>Ngày mai, cùng thứ trong 8 tuần</h3>
    <c:forEach var="row" items="${forecast}">
        <div>${row.ten}: khoảng ${row.duBao}</div>
    </c:forEach>
    <c:if test="${empty forecast}"><p>Chưa đủ đơn hoàn thành để dự báo.</p></c:if>
</div>
<div class="content-card">
    <h3>Báo cáo trong ngày</h3>
    <pre class="report-text">${reportText}</pre>
    <form method="post" action="${pageContext.request.contextPath}/du-bao">
        <button class="btn btn-primary" type="submit">Gửi Telegram ngay</button>
    </form>
</div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
