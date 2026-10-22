(function () {
    var root = document.querySelector('[data-model-form]');
    var form = root ? root.querySelector('form') : null;
    if (!root || !form) {
        return;
    }

    var materials = JSON.parse(root.getAttribute('data-materials') || '{}');
    var layers = JSON.parse(root.getAttribute('data-layers') || '{}');
    var startup = Number(root.getAttribute('data-startup') || 0);
    var minimum = Number(root.getAttribute('data-minimum') || 0);
    var shell = Number(root.getAttribute('data-shell') || 0.35);
    var volumeNode = root.querySelector('[data-model-volume]');
    var estimateNode = root.querySelector('[data-model-estimate]');
    var errorNode = root.querySelector('[data-model-error]');
    var box = root.querySelector('[data-step-box]');
    var fileInput = form.querySelector('[name="model"]');
    var volume = null;
    var timer = null;

    function money(value) {
        return new Intl.NumberFormat('vi-VN').format(Math.round(value)) + 'đ';
    }

    function isStep() {
        var name = (fileInput.files[0] && fileInput.files[0].name || '').toLowerCase();
        return name.endsWith('.step') || name.endsWith('.stp');
    }

    function render() {
        if (box) {
            box.classList.toggle('d-none', !isStep());
        }
        if (volume === null) {
            volumeNode.textContent = '—';
            estimateNode.textContent = '—';
            return;
        }
        var material = form.querySelector('[name="material"]').value;
        var layer = form.querySelector('[name="layer_height"]').value;
        var infill = Number(form.querySelector('[name="infill"]').value || 0);
        var quantity = Math.max(1, Number(form.querySelector('[name="quantity"]').value || 1));
        var fill = shell + (1 - shell) * (infill / 100);
        var perPiece = volume * fill * Number(materials[material] || 0) * Number(layers[layer] || 1);
        var total = Math.max(minimum, Math.round(startup + perPiece * quantity));
        volumeNode.textContent = volume.toLocaleString('vi-VN') + ' cm³';
        estimateNode.textContent = money(total);
    }

    function preview() {
        if (!fileInput.files[0]) {
            volume = null;
            errorNode.textContent = '';
            render();
            return;
        }
        var data = new FormData(form);
        var token = document.querySelector('meta[name="csrf-token"]');
        fetch(root.getAttribute('data-preview-url'), {
            method: 'POST',
            headers: {
                'X-CSRF-TOKEN': token ? token.getAttribute('content') : '',
                Accept: 'application/json',
            },
            body: data,
        }).then(function (response) {
            return response.json().then(function (body) {
                return { ok: response.ok, body: body };
            });
        }).then(function (result) {
            if (!result.ok) {
                volume = null;
                var errors = result.body.errors || {};
                var first = errors.model || errors.length_mm || ['Không tính được thể tích.'];
                errorNode.textContent = first[0];
                render();
                return;
            }
            volume = Number(result.body.volume_cm3);
            errorNode.textContent = '';
            render();
        }).catch(function () {
            errorNode.textContent = 'Không tính được tạm tính. Bạn vẫn có thể gửi file.';
        });
    }

    form.addEventListener('change', function () {
        clearTimeout(timer);
        timer = setTimeout(preview, 250);
    });
    form.addEventListener('input', render);
    render();
})();
