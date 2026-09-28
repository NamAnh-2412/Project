(function (global) {
    'use strict';

    function initCollapsibleForm(cfg) {
        var panel = document.getElementById(cfg.panelId);
        if (!panel) {
            return;
        }

        var toggleBtn = cfg.toggleBtnId ? document.getElementById(cfg.toggleBtnId) : null;
        var closeBtn = cfg.closeBtnId ? document.getElementById(cfg.closeBtnId) : null;
        var labelEl = cfg.labelId ? document.getElementById(cfg.labelId) : null;
        var iconEl = cfg.iconId ? document.getElementById(cfg.iconId) : null;
        var isEdit = !!cfg.isEdit;
        var addLabel = cfg.addLabel || 'Thêm';
        var editOpenLabel = cfg.editOpenLabel || 'Ẩn form sửa';
        var addOpenLabel = cfg.addOpenLabel || 'Ẩn form thêm';

        function setOpen(open, scroll) {
            panel.classList.toggle('is-open', open);
            panel.style.display = open ? 'block' : 'none';
            if (toggleBtn) {
                toggleBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
            }
            if (labelEl) {
                labelEl.textContent = open ? (isEdit ? editOpenLabel : addOpenLabel) : addLabel;
            }
            if (iconEl) {
                iconEl.className = open ? 'fas fa-chevron-up' : 'fas fa-plus';
            }
            if (open && scroll) {
                panel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        }

        if (toggleBtn) {
            toggleBtn.addEventListener('click', function () {
                setOpen(!panel.classList.contains('is-open'), true);
            });
        }
        if (closeBtn) {
            closeBtn.addEventListener('click', function () {
                setOpen(false, false);
            });
        }

        if (cfg.startOpen) {
            setOpen(true, !!cfg.scrollOnOpen);
        }
    }

    global.initCollapsibleForm = initCollapsibleForm;
})(window);
