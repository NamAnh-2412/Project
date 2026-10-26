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
<div class="content-card">
    <p>Màn hình khách: <a href="${pageContext.request.contextPath}/man-hinh-phu?token=${cfdToken}">${pageContext.request.contextPath}/man-hinh-phu?token=${cfdToken}</a></p>
    <p>Webhook sàn: <code>${pageContext.request.contextPath}/api/integrations/delivery-webhook</code> với header <code>X-Api-Key</code>. Nguồn nhận: shopeefood, grabfood, befood. Quán chưa gửi lệnh tạm ngừng món ngược ra sàn vì chưa có API của từng hãng.</p>
    <form method="post" action="${pageContext.request.contextPath}/mo-rong" class="field-grid">
        <label>Telegram bot token<input class="form-control" type="password" name="telegram_bot_token" placeholder="Để trống nếu không đổi"></label>
        <label>Telegram chat id<input class="form-control" name="telegram_chat_id" value="${settings.telegram_chat_id}"></label>
        <label>Khóa nhận đơn sàn<input class="form-control" type="password" name="delivery_api_key" placeholder="Để trống nếu không đổi"></label>
        <label>Hóa đơn điện tử
            <select class="form-control" name="einvoice_provider">
                <option value="">Chưa dùng</option>
                <option value="viettel" ${settings.einvoice_provider == 'viettel' ? 'selected' : ''}>Viettel S-Invoice</option>
                <option value="vnpt" ${settings.einvoice_provider == 'vnpt' ? 'selected' : ''}>VNPT Invoice</option>
                <option value="misa" ${settings.einvoice_provider == 'misa' ? 'selected' : ''}>MISA meInvoice</option>
            </select>
        </label>
        <label>Tài khoản hóa đơn<input class="form-control" name="einvoice_username" value="${settings.einvoice_username}"></label>
        <label>Mật khẩu hóa đơn<input class="form-control" type="password" name="einvoice_password" placeholder="Để trống nếu không đổi"></label>
        <label>Vĩ độ quán<input class="form-control" name="geofence_lat" value="${settings.geofence_lat}"></label>
        <label>Kinh độ quán<input class="form-control" name="geofence_lng" value="${settings.geofence_lng}"></label>
        <label>Bán kính mét<input class="form-control" name="geofence_met" value="${empty settings.geofence_met ? '50' : settings.geofence_met}"></label>
        <button class="btn btn-primary" type="submit">Lưu</button>
    </form>
</div>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
