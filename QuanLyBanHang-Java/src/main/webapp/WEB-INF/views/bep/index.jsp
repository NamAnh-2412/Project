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
<div class="ticket-grid">
    <c:forEach var="ticket" items="${tickets}">
        <article class="ticket content-card" data-minutes="${ticket.phut}">
            <h3>${ticket.maDonHang} · ${empty ticket.tenBan ? 'Mang về' : ticket.tenBan}</h3>
            <div>${ticket.tenKhachHang} · ${ticket.phut} phút · ${ticket.trangThaiBep}</div>
            <c:forEach var="item" items="${ticket.items}">
                <div class="item-block ${item.trangThaiBep == 'xong' ? 'item-done' : ''}">
                    ${item.soLuong} × ${item.ten}<c:if test="${not empty item.size}"> (${item.size})</c:if>
                    <c:if test="${not empty item.choices}"><div class="choice-note">${item.choices}</div></c:if>
                    <c:if test="${item.trangThaiBep != 'xong'}">
                        <form method="post" action="${pageContext.request.contextPath}/bep">
                            <input type="hidden" name="action" value="item">
                            <input type="hidden" name="item_id" value="${item.id}">
                            <button class="btn btn-secondary" type="submit">Xong món</button>
                        </form>
                    </c:if>
                </div>
            </c:forEach>
            <form method="post" action="${pageContext.request.contextPath}/bep" class="button-row">
                <input type="hidden" name="order_id" value="${ticket.id}">
                <button class="btn btn-secondary" name="action" value="start" type="submit">Bắt đầu làm</button>
                <button class="btn btn-primary" name="action" value="finish" type="submit">Hoàn thành hết</button>
            </form>
        </article>
    </c:forEach>
</div>
<c:if test="${empty tickets}"><div class="content-card">Chưa có món chờ bếp.</div></c:if>
<script>
(function () {
  var count = document.querySelectorAll('.ticket').length;
  var previous = Number(sessionStorage.getItem('kds-count') || count);
  if (count > previous) {
    try {
      var context = new AudioContext();
      var oscillator = context.createOscillator();
      oscillator.frequency.value = 880;
      oscillator.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + 0.18);
    } catch (error) {}
  }
  sessionStorage.setItem('kds-count', String(count));
  setTimeout(function () { location.reload(); }, 6000);
})();
</script>
<%@ include file="/WEB-INF/views/layout/app-shell-end.jspf" %>
</body>
</html>
