<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <%@ include file="/WEB-INF/views/donhang/orders-head.jspf" %>
</head>
<body class="create-order-page">
<%@ include file="/WEB-INF/views/layout/app-shell-start.jspf" %>
<%
    java.net.URL reactBundle = null;
    try {
        reactBundle = application.getResource("/assets/react/order-create/order-create.js");
    } catch (java.net.MalformedURLException ex) {
        reactBundle = null;
    }
    pageContext.setAttribute("reactBundle", reactBundle);
%>
<c:set var="useLegacy" value="${param.legacy == '1' or reactBundle == null}" />
<c:choose>
    <c:when test="${useLegacy}">
        <%@ include file="/WEB-INF/views/donhang/create-legacy.jspf" %>
    </c:when>
    <c:otherwise>
            <div id="order-create-root"></div>
            <script>
              window.ORDER_CREATE_CONFIG = {
                basePath: '${pageContext.request.contextPath}/',
                defaultPaymentMethod: '${defaultPaymentMethod}',
                shopName: '${shopName}',
                initialCat: ${activeCat != null ? activeCat : 'null'},
                orderListUrl: '${pageContext.request.contextPath}/don-hang'
              };
              <c:if test="${not empty flashMessage}">
              window.ORDER_CREATE_FLASH = {
                type: '${flashType}',
                message: '${flashMessage}'
              };
              </c:if>
            </script>
            <script type="module" src="${pageContext.request.contextPath}/assets/react/order-create/order-create.js"></script>
    </c:otherwise>
</c:choose>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
