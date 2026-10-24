function baoNho(thanhCong, tieuDe) {
    if (!window.Swal) return;
    Swal.fire({
        toast: true,
        position: 'top-end',
        icon: thanhCong ? 'success' : 'error',
        title: tieuDe,
        showConfirmButton: false,
        timer: 2200,
    });
}

document.addEventListener('click', function (suKien) {
    var nut = suKien.target.closest('.khung-truot .nut-truot');
    if (!nut) return;
    suKien.preventDefault();
    var khung = nut.closest('.khung-truot');
    var anh = Array.prototype.slice.call(khung.querySelectorAll('.anh-truot'));
    if (anh.length < 2) return;
    var dang = anh.findIndex(function (el) { return el.classList.contains('hien'); });
    if (dang < 0) dang = 0;
    anh[dang].classList.remove('hien');
    var buoc = nut.classList.contains('sau') ? 1 : -1;
    anh[(dang + buoc + anh.length) % anh.length].classList.add('hien');
});

document.addEventListener('click', function (suKien) {
    var mo = suKien.target.closest('[data-mo-anh]');
    var hop = document.querySelector('.hop-anh');
    if (!hop) return;
    if (mo) {
        var anh = Array.prototype.slice.call(document.querySelectorAll('.bo-anh img'));
        hop._anh = anh.map(function (el) { return el.src; });
        hop._viTri = Number(mo.getAttribute('data-mo-anh')) || 0;
        hop.querySelector('img').src = hop._anh[hop._viTri] || '';
        hop.hidden = false;
        return;
    }
    if (suKien.target.closest('.hop-anh .dong-anh') || suKien.target === hop) {
        hop.hidden = true;
        return;
    }
    var truot = suKien.target.closest('.hop-anh .nut-truot');
    if (!truot || !hop._anh || !hop._anh.length) return;
    var buoc = truot.classList.contains('sau') ? 1 : -1;
    hop._viTri = (hop._viTri + buoc + hop._anh.length) % hop._anh.length;
    hop.querySelector('img').src = hop._anh[hop._viTri];
});

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.thong-bao-flash').forEach(function (el) {
        var loi = el.classList.contains('alert-error');
        baoNho(!loi, el.textContent.trim());
        el.remove();
    });
});
