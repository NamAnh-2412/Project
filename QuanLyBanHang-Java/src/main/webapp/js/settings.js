/**
 * Settings Page JavaScript
 * Xử lý tabs, form validation và logo upload
 */

(function() {
    'use strict';

    // Initialize when DOM is ready
    document.addEventListener('DOMContentLoaded', function() {
        initTabs();
        initForms();
        initLogoUpload();
    });

    /**
     * Initialize tabs functionality
     */
    function initTabs() {
        const tabButtons = document.querySelectorAll('.tab-btn');
        const tabContents = document.querySelectorAll('.tab-content');

        tabButtons.forEach(button => {
            button.addEventListener('click', function() {
                const targetTab = this.getAttribute('data-tab');

                // Remove active class from all buttons and contents
                tabButtons.forEach(btn => btn.classList.remove('active'));
                tabContents.forEach(content => content.classList.remove('active'));

                // Add active class to clicked button and corresponding content
                this.classList.add('active');
                const targetContent = document.getElementById('tab-' + targetTab);
                if (targetContent) {
                    targetContent.classList.add('active');
                }
            });
        });
    }

    /**
     * Initialize form submissions with loading states
     */
    function initForms() {
        const forms = document.querySelectorAll('.settings-form');

        forms.forEach(form => {
            // Auto apply loading state
            if (typeof autoButtonLoadingOnSubmit === 'function') {
                autoButtonLoadingOnSubmit(form);
            }

            // Add form validation
            form.addEventListener('submit', function(e) {
                if (!validateForm(this)) {
                    e.preventDefault();
                    return false;
                }
            });
        });
    }

    /**
     * Validate form
     */
    function validateForm(form) {
        const requiredFields = form.querySelectorAll('[required]');
        let isValid = true;

        requiredFields.forEach(field => {
            if (!field.value.trim()) {
                isValid = false;
                showFieldError(field, 'Trường này là bắt buộc');
            } else {
                clearFieldError(field);
                
                // Validate specific field types
                if (field.type === 'email' && field.value) {
                    if (!isValidEmail(field.value)) {
                        isValid = false;
                        showFieldError(field, 'Email không hợp lệ');
                    }
                }
                
                if (field.getAttribute('data-validate') === 'phone' && field.value) {
                    if (!isValidPhone(field.value)) {
                        isValid = false;
                        showFieldError(field, 'Số điện thoại không hợp lệ');
                    }
                }
            }
        });

        // Validate number fields
        const numberFields = form.querySelectorAll('input[type="number"]');
        numberFields.forEach(field => {
            const min = parseFloat(field.getAttribute('min'));
            const max = parseFloat(field.getAttribute('max'));
            const value = parseFloat(field.value);

            if (field.value && !isNaN(value)) {
                if (min !== null && value < min) {
                    isValid = false;
                    showFieldError(field, `Giá trị tối thiểu là ${min}`);
                } else if (max !== null && value > max) {
                    isValid = false;
                    showFieldError(field, `Giá trị tối đa là ${max}`);
                } else {
                    clearFieldError(field);
                }
            }
        });

        return isValid;
    }

    /**
     * Show field error
     */
    function showFieldError(field, message) {
        clearFieldError(field);
        
        field.classList.add('error');
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error';
        errorDiv.textContent = message;
        errorDiv.style.color = '#ef4444';
        errorDiv.style.fontSize = '12px';
        errorDiv.style.marginTop = '4px';
        
        field.parentNode.appendChild(errorDiv);
    }

    /**
     * Clear field error
     */
    function clearFieldError(field) {
        field.classList.remove('error');
        const existingError = field.parentNode.querySelector('.field-error');
        if (existingError) {
            existingError.remove();
        }
    }

    /**
     * Validate email
     */
    function isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    /**
     * Validate phone (Vietnamese format)
     */
    function isValidPhone(phone) {
        const cleaned = phone.replace(/[^0-9+]/g, '');
        return /^(0|\+84)[0-9]{9,10}$/.test(cleaned);
    }

    /**
     * Initialize logo upload
     */
    function initLogoUpload() {
        const logoFileInput = document.getElementById('logoFile');
        const logoPreview = document.getElementById('logoPreview');
        const deleteLogoBtn = document.getElementById('btnDeleteLogo');

        if (!logoFileInput) return;

        // Handle file selection
        logoFileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (!file) return;

            // Validate file type
            const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
            const fileExtension = file.name.split('.').pop().toLowerCase();
            const allowedExtensions = ['jpg', 'jpeg', 'png', 'gif', 'webp'];
            
            if (!allowedTypes.includes(file.type) && !allowedExtensions.includes(fileExtension)) {
                showToast('Định dạng file không được phép. Chỉ chấp nhận: JPG, JPEG, PNG, GIF, WEBP', 'error');
                this.value = '';
                return;
            }

            // Validate file size (4MB - sẽ được resize xuống)
            if (file.size > 4194304) {
                showToast('File quá lớn. Kích thước tối đa: 4MB (sẽ tự động resize)', 'error');
                this.value = '';
                return;
            }
            
            // Show info if file is large
            if (file.size > 2097152) {
                showToast('File lớn, đang xử lý resize...', 'info');
            }

            // Show preview
            const reader = new FileReader();
            reader.onload = function(e) {
                updateLogoPreview(e.target.result);
            };
            reader.readAsDataURL(file);

            // Upload file
            uploadLogo(file);
        });

        // Handle delete logo
        if (deleteLogoBtn) {
            deleteLogoBtn.addEventListener('click', function() {
                if (confirm('Bạn có chắc chắn muốn xóa logo?')) {
                    deleteLogo();
                }
            });
        }
    }

    /**
     * Update logo preview
     */
    function updateLogoPreview(imageSrc) {
        const logoPreview = document.getElementById('logoPreview');
        if (!logoPreview) return;

        logoPreview.innerHTML = `
            <img src="${imageSrc}" alt="Logo">
            <button type="button" class="btn-delete-logo" id="btnDeleteLogo" title="Xóa logo">
                <i class="fas fa-times"></i>
            </button>
        `;

        // Re-attach delete button event
        const deleteBtn = document.getElementById('btnDeleteLogo');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', function() {
                if (confirm('Bạn có chắc chắn muốn xóa logo?')) {
                    deleteLogo();
                }
            });
        }
    }

    /**
     * Upload logo to server
     */
    function uploadLogo(file) {
        const formData = new FormData();
        formData.append('logo', file);

        // Show loading
        const logoPreview = document.getElementById('logoPreview');
        if (logoPreview) {
            logoPreview.style.opacity = '0.6';
            logoPreview.style.pointerEvents = 'none';
        }

        const baseUrl = window.APP_BASE_PATH || '/';
        fetch(baseUrl + 'api/logo-upload', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            // Check if response is OK
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                showToast('Upload logo thành công!', 'success');
                // Update preview with server path
                if (data.url) {
                    updateLogoPreview(data.url);
                }
            } else {
                const errorMsg = data.error || 'Có lỗi xảy ra khi upload logo';
                console.error('Upload logo error:', errorMsg);
                showToast(errorMsg, 'error');
                // Reset preview
                resetLogoPreview();
            }
        })
        .catch(error => {
            console.error('Error uploading logo:', error);
            let errorMsg = 'Có lỗi xảy ra khi upload logo';
            if (error.message) {
                errorMsg += ': ' + error.message;
            }
            showToast(errorMsg, 'error');
            resetLogoPreview();
        })
        .finally(() => {
            // Remove loading
            if (logoPreview) {
                logoPreview.style.opacity = '1';
                logoPreview.style.pointerEvents = 'auto';
            }
            // Clear file input
            const logoFileInput = document.getElementById('logoFile');
            if (logoFileInput) {
                logoFileInput.value = '';
            }
        });
    }

    /**
     * Delete logo
     */
    function deleteLogo() {
        const baseUrl = window.APP_BASE_PATH || '/';
        fetch(baseUrl + 'api/logo-delete', {
            method: 'POST'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Đã xóa logo thành công!', 'success');
                resetLogoPreview();
            } else {
                showToast(data.error || 'Có lỗi xảy ra khi xóa logo', 'error');
            }
        })
        .catch(error => {
            console.error('Error deleting logo:', error);
            showToast('Có lỗi xảy ra khi xóa logo', 'error');
        });
    }

    /**
     * Reset logo preview to placeholder
     */
    function resetLogoPreview() {
        const logoPreview = document.getElementById('logoPreview');
        if (!logoPreview) return;

        logoPreview.innerHTML = `
            <div class="logo-placeholder">
                <i class="fas fa-image"></i>
                <span>Chưa có logo</span>
            </div>
        `;
    }

    /**
     * Show toast notification
     */
    function showToast(message, type = 'info') {
        if (typeof showToastNotification === 'function') {
            showToastNotification(message, type);
        } else {
            // Fallback: simple alert
            alert(message);
        }
    }

})();

