(function () {
    var khoa = 'chu-de';
    function apDung(che) {
        document.documentElement.setAttribute('data-chu-de', che);
        var nut = document.getElementById('nut-chu-de');
        if (!nut) return;
        var toi = che === 'toi';
        nut.textContent = toi ? 'Sáng' : 'Tối';
        nut.setAttribute('aria-pressed', toi ? 'true' : 'false');
        nut.setAttribute('title', toi ? 'Chuyển sang chế độ sáng' : 'Chuyển sang chế độ tối');
    }
    var nut = document.getElementById('nut-chu-de');
    if (nut) {
        nut.addEventListener('click', function () {
            var moi = document.documentElement.getAttribute('data-chu-de') === 'toi' ? 'sang' : 'toi';
            try { localStorage.setItem(khoa, moi); } catch (e) { /* trình duyệt chặn lưu */ }
            apDung(moi);
        });
    }
    apDung(document.documentElement.getAttribute('data-chu-de') || 'sang');
})();
