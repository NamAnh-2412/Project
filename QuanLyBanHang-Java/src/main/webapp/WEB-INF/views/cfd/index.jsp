<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Màn hình khách</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/public.css">
</head>
<body class="cfd-page">
<main class="cfd-main">
    <section>
        <h1 id="shop">${shopName}</h1>
        <div id="lines">Đang chờ thu ngân...</div>
        <div class="cfd-total" id="total"></div>
    </section>
    <section id="qr"></section>
</main>
<script>
const token = '${token}';
async function refresh() {
  const response = await fetch('${pageContext.request.contextPath}/api/cfd?token=' + encodeURIComponent(token));
  const data = await response.json();
  const lines = document.getElementById('lines');
  if (!data || !data.lines) { lines.textContent = 'Đang chờ thu ngân...'; return; }
  lines.innerHTML = data.lines.map(function (line) {
    return '<div>' + line.ten + ' × ' + line.sl + ' · ' + Number(line.tien).toLocaleString('vi-VN') + 'đ</div>';
  }).join('');
  document.getElementById('total').textContent = Number(data.tong || 0).toLocaleString('vi-VN') + 'đ';
  const qr = document.getElementById('qr');
  qr.innerHTML = data.qr ? '<div>Quét để chuyển khoản</div><img alt="VietQR" src="' + data.qr + '"><div>' + (data.noiDung || '') + '</div>' : '';
}
refresh();
setInterval(refresh, 2000);
</script>
</body>
</html>
