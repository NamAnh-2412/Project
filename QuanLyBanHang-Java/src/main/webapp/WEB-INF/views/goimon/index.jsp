<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Gọi món ${shopName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/public.css">
</head>
<body class="guest-page">
<header>
    <div>${shopName}</div>
    <h1>${empty table ? 'Gọi món' : table.tenKhuVuc} ${empty table ? '' : table.tenBan}</h1>
</header>
<c:if test="${not empty error}"><p class="guest-error">${error}</p></c:if>
<div class="guest-panel"><div id="menu" class="guest-grid"></div></div>
<div id="picker" class="guest-panel" hidden></div>
<div class="guest-cart">
    <div id="lines"></div>
    <input id="customer" class="guest-input" placeholder="Tên khách (không bắt buộc)">
    <button id="send" class="guest-send" type="button">Gửi đơn</button>
    <div id="status"></div>
</div>
<script type="application/json" id="menu-data"><c:out value="${menuJson}" /></script>
<script>
const MENU = JSON.parse(document.getElementById('menu-data').textContent || '[]');
const token = new URLSearchParams(location.search).get('token') || '';
const csrf = '${csrfToken}';
let cart = [];
let current = null;
const menuEl = document.getElementById('menu');
MENU.forEach(function (product) {
  const card = document.createElement('button');
  card.type = 'button';
  card.className = 'guest-card';
  card.innerHTML = '<div>' + product.ten + '</div><div class="guest-price">' + Number(product.gia).toLocaleString('vi-VN') + 'đ</div>';
  card.onclick = function () { openPicker(product); };
  menuEl.appendChild(card);
});
function openPicker(product) {
  current = product;
  const picker = document.getElementById('picker');
  picker.hidden = false;
  let html = '<h3>' + product.ten + '</h3>';
  if (product.sizes.length) {
    html += '<div>Size</div>';
    product.sizes.forEach(function (size, index) {
      html += '<label><input type="radio" name="size" value="' + size.id + '"' + (index === 0 ? ' checked' : '') + '> ' + size.ten + ' +' + Number(size.gia).toLocaleString('vi-VN') + '</label><br>';
    });
  }
  product.nhomLuaChon.forEach(function (group) {
    html += '<div>' + group.ten + (group.batBuoc ? ' *' : '') + '</div>';
    group.luaChon.forEach(function (choice) {
      html += '<label><input type="checkbox" data-group="' + group.id + '" data-choice="' + choice.id + '" data-name="' + choice.ten + '" data-group-name="' + group.ten + '" data-price="' + choice.gia + '"> ' + choice.ten + '</label><br>';
    });
  });
  html += '<button type="button" id="add" class="guest-send">Thêm vào đơn</button>';
  picker.innerHTML = html;
  document.getElementById('add').onclick = addCurrent;
}
function addCurrent() {
  const sizeInput = document.querySelector('#picker input[name="size"]:checked');
  const size = sizeInput ? current.sizes.find(function (item) { return String(item.id) === sizeInput.value; }) : null;
  const choices = Array.from(document.querySelectorAll('#picker input[type="checkbox"]:checked')).map(function (input) {
    return { nhom_id: Number(input.dataset.group), lua_chon_id: Number(input.dataset.choice), nhom_ten: input.dataset.groupName, lua_chon_ten: input.dataset.name, gia_tang_them: Number(input.dataset.price) };
  });
  let price = Number(current.gia) + (size ? Number(size.gia) : 0);
  choices.forEach(function (choice) { price += choice.gia_tang_them; });
  cart.push({ id: current.id, ten: current.ten, gia: price, sl: 1, size: size ? size.ten : '', size_id: size ? size.id : null, choices: choices });
  document.getElementById('picker').hidden = true;
  renderCart();
}
function renderCart() {
  document.getElementById('lines').innerHTML = cart.map(function (item) {
    return '<div>' + item.sl + ' × ' + item.ten + (item.size ? ' (' + item.size + ')' : '') + '</div>';
  }).join('');
}
document.getElementById('send').onclick = async function () {
  if (!cart.length) { document.getElementById('status').textContent = 'Chọn món trước.'; return; }
  const status = document.getElementById('status');
  let lat = '';
  let lng = '';
  if (navigator.geolocation) {
    try {
      const position = await new Promise(function (resolve, reject) {
        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 8000 });
      });
      lat = position.coords.latitude;
      lng = position.coords.longitude;
    } catch (error) { /* quán không bật vùng thì server vẫn nhận đơn */ }
  }
  const body = new URLSearchParams();
  body.set('token', token);
  body.set('items', JSON.stringify(cart));
  body.set('ten_khachhang', document.getElementById('customer').value);
  body.set('lat', lat);
  body.set('lng', lng);
  body.set('_csrf', csrf);
  const response = await fetch('${pageContext.request.contextPath}/api/public/orders', { method: 'POST', body: body, credentials: 'same-origin' });
  const data = await response.json();
  if (!data.success) { status.textContent = data.error || 'Không gửi được đơn.'; return; }
  cart = [];
  renderCart();
  status.textContent = data.message || 'Đã gửi đơn tới quầy.';
};
</script>
</body>
</html>
