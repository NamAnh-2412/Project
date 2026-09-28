(function() {
    'use strict';

    const page = window.PRODUCT_PAGE || {};
    const contextPath = page.contextPath || (window.APP_BASE_PATH || '/').replace(/\/$/, '');
    const currentProductId = Number(page.currentProductId || 0);

    document.addEventListener('DOMContentLoaded', function() {
        const fileInput = document.getElementById('productImageFile');
        if (fileInput) {
            fileInput.addEventListener('change', handleProductImageUpload);
        }

        const urlInput = document.getElementById('imageUrlInput');
        if (urlInput) {
            urlInput.addEventListener('input', function() {
                renderImagePreview(this.value);
            });
        }

        if (currentProductId > 0) {
            reloadSizes(currentProductId, false);
        }
    });

    async function handleProductImageUpload() {
        const fileInput = document.getElementById('productImageFile');
        if (!fileInput || !fileInput.files.length) {
            return;
        }
        const status = document.getElementById('imageUploadStatus');
        if (status) {
            status.textContent = 'Dang upload...';
        }
        const formData = new FormData();
        formData.append('product_image', fileInput.files[0]);
        if (currentProductId > 0) {
            formData.append('product_id', String(currentProductId));
        }
        try {
            const response = await fetch(contextPath + '/api/product-image-upload', { method: 'POST', body: formData });
            const data = await response.json();
            if (!data.success) {
                throw new Error(data.error || 'Upload that bai');
            }
            document.getElementById('imageUrlInput').value = data.path;
            renderImagePreview(data.url || (contextPath + '/' + data.path));
            if (status) {
                status.textContent = 'Upload thanh cong';
            }
        } catch (error) {
            if (status) {
                status.textContent = '';
            }
            alert(error.message || 'Upload that bai');
        } finally {
            fileInput.value = '';
        }
    }

    function renderImagePreview(value) {
        const box = document.getElementById('imagePreviewBox');
        if (!box) {
            return;
        }
        const trimmed = (value || '').trim();
        if (!trimmed) {
            box.innerHTML = '<span style="color:#94a3b8;">Chua co anh</span>';
            return;
        }
        const src = trimmed.startsWith('http://') || trimmed.startsWith('https://')
            ? trimmed
            : contextPath + '/' + trimmed.replace(/^\//, '');
        box.innerHTML = '<img src="' + escapeHtml(src) + '" alt="Preview">';
    }

    async function callSizeApi(payload) {
        const formData = new FormData();
        Object.entries(payload).forEach(function(entry) {
            formData.append(entry[0], entry[1]);
        });
        const response = await fetch(contextPath + '/api/manage-size', {
            method: 'POST',
            body: formData,
            headers: { 'Accept': 'application/json' }
        });
        const data = await response.json();
        if (!data.success) {
            throw new Error(data.error || 'Không thể xử lý size');
        }
        return data;
    }

    async function reloadSizes(productId, showAlertOnError) {
        if (showAlertOnError === undefined) {
            showAlertOnError = true;
        }
        try {
            const data = await callSizeApi({ action: 'list', product_id: productId });
            const rows = Array.isArray(data.data) ? data.data : [];
            const tbody = document.getElementById('sizeTableBody');
            if (!tbody) {
                return;
            }
            if (rows.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4">Chua co size nao.</td></tr>';
                return;
            }
            tbody.innerHTML = rows.map(function(size) {
                return '<tr>'
                    + '<td>' + escapeHtml(size.tenSize) + '</td>'
                    + '<td>' + formatMoney(size.gia) + '</td>'
                    + '<td>' + (size.dangHoatDong ? 'Hoat dong' : 'Da an') + '</td>'
                    + '<td><div class="size-actions">'
                    + '<button type="button" class="btn btn-secondary" onclick="promptEditSize('
                    + productId + ', ' + size.id + ', \'' + escapeJs(size.tenSize) + '\', \'' + size.gia + '\')">Sua</button> '
                    + '<button type="button" class="btn btn-danger" onclick="deleteSize('
                    + productId + ', ' + size.id + ')">Xoa</button>'
                    + '</div></td>'
                    + '</tr>';
            }).join('');
        } catch (error) {
            const tbody = document.getElementById('sizeTableBody');
            if (tbody) {
                tbody.innerHTML = '<tr><td colspan="4" style="color:#ef4444;">Không tải được danh sách size.</td></tr>';
            }
            if (showAlertOnError) {
                alert(error.message);
            }
        }
    }

    window.promptAddSize = async function(productId) {
        const tenSize = prompt('Nhap ten size moi:');
        if (!tenSize || !tenSize.trim()) {
            return;
        }
        const gia = prompt('Nhap gia tang them:', '0');
        if (gia == null) {
            return;
        }
        try {
            await callSizeApi({ action: 'add', product_id: productId, ten_size: tenSize.trim(), gia: gia });
            await reloadSizes(productId);
        } catch (error) {
            alert(error.message);
        }
    };

    window.reloadSizes = reloadSizes;

    window.promptEditSize = async function(productId, sizeId, currentName, currentPrice) {
        const tenSize = prompt('Sua ten size:', currentName);
        if (!tenSize || !tenSize.trim()) {
            return;
        }
        const gia = prompt('Sua gia tang them:', currentPrice);
        if (gia == null) {
            return;
        }
        try {
            await callSizeApi({ action: 'update', product_id: productId, id: sizeId, ten_size: tenSize.trim(), gia: gia });
            await reloadSizes(productId);
        } catch (error) {
            alert(error.message);
        }
    };

    window.deleteSize = async function(productId, sizeId) {
        if (!confirm('Ban co chac muon xoa size nay?')) {
            return;
        }
        try {
            await callSizeApi({ action: 'delete', product_id: productId, id: sizeId });
            await reloadSizes(productId);
        } catch (error) {
            alert(error.message);
        }
    };

    function formatMoney(value) {
        return new Intl.NumberFormat('vi-VN').format(Number(value || 0));
    }

    function escapeHtml(value) {
        const div = document.createElement('div');
        div.textContent = value == null ? '' : value;
        return div.innerHTML;
    }

    function escapeJs(value) {
        return String(value == null ? '' : value).replace(/\\/g, '\\\\').replace(/'/g, "\\'");
    }
})();
