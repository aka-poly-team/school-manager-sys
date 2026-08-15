/**
 * AKA SCHOOL MANAGER SYSTEM — UNIVERSAL FORM, TABLE, SEARCH & TOAST ENGINE (VANILLA JS THUẦN)
 * ---------------------------------------------------------------------------------------------
 * Mã nguồn JavaScript thuần 100%, HOÀN TOÀN KHÔNG CHỨA CSS TRONG FILE JS (TÁCH BIỆT CSS VÀ JS).
 * TÍNH NĂNG REAL-TIME CHỈ VÀ DUY NHẤT DÙNG CHO Ô TÌM KIẾM BẢNG (BỎ QUA HOÀN TOÀN Ô SỬA INLINE).
 */

// 1. HÀM PHÁT THÔNG BÁO NỔI GÓC PHẢI MÀN HÌNH (TOAST NOTIFICATION)
function showToastNotification(message, type) {
    if (!message || !message.trim()) return;

    let toastContainer = document.getElementById('aka-toast-container');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'aka-toast-container';
        toastContainer.className = 'aka-toast-container';
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    const isSuccess = (type === 'success');
    toast.className = isSuccess ? 'aka-toast aka-toast-success' : 'aka-toast aka-toast-error';
    
    // Icon iPhone Monochrome Vector SVG (Trắng/Đen tối giản)
    const iconSvg = isSuccess 
        ? `<span class="aka-toast-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg></span>`
        : `<span class="aka-toast-icon"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffffff" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg></span>`;

    toast.innerHTML = `${iconSvg}<span class="aka-toast-text">${message.trim()}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(function () {
        toast.classList.add('aka-toast-hiding');
        setTimeout(function () { 
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 400);
    }, 4500);
}

// 2. HÀM TỰ ĐỘNG VIẾT HOA CHỮ CÁI ĐẦU TIÊN CỦA MỖI TỪ (TITLE CASE HỌ VÀ TÊN)
function autoCapitalizeName(text) {
    if (!text) return '';
    return text.toLowerCase().replace(/(?:^|\s)\S/g, function (char) {
        return char.toUpperCase();
    });
}

// 3. HÀM HIỂN THỊ THÔNG BÁO LỖI INLINE NẰM BÊN DƯỚI Ô INPUT HOẶC CELL TRONG BẢNG
function showFieldError(inputElement, errorMessage) {
    inputElement.classList.add('field-has-error');
    inputElement.classList.remove('field-is-valid');

    let parentContainer = inputElement.parentElement;
    if (parentContainer && parentContainer.classList.contains('input-with-button')) {
        parentContainer = parentContainer.parentElement;
    }
    if (!parentContainer) return;

    let errorSpan = parentContainer.querySelector('.validation-error-msg');
    if (!errorSpan) {
        errorSpan = document.createElement('span');
        errorSpan.className = 'validation-error-msg';
        parentContainer.appendChild(errorSpan);
    }
    errorSpan.textContent = errorMessage;
}

// 4. HÀM XÓA THÔNG BÁO LỖI KHI DỮ LIỆU ĐÃ HỢP LỆ
function clearFieldError(inputElement) {
    inputElement.classList.remove('field-has-error');
    inputElement.classList.add('field-is-valid');

    let parentContainer = inputElement.parentElement;
    if (parentContainer && parentContainer.classList.contains('input-with-button')) {
        parentContainer = parentContainer.parentElement;
    }
    if (!parentContainer) return;

    const errorSpan = parentContainer.querySelector('.validation-error-msg');
    if (errorSpan) {
        errorSpan.remove();
    }
}

// 5. HÀM KIỂM TRA DỮ LIỆU CỦA MỘT Ô INPUT CỤ THỂ
function validateSingleField(inputElement) {
    if (!inputElement || inputElement.type === 'hidden' || inputElement.disabled || inputElement.readOnly) {
        return true;
    }

    const value = (inputElement.value || '').trim();
    const name = inputElement.name || '';
    const type = (inputElement.type || '').toLowerCase();
    const akaType = inputElement.dataset.akaType || '';
    const isRequired = inputElement.hasAttribute('required') || inputElement.dataset.akaRequired === 'true';

    // Rule 1: Kiểm tra bắt buộc nhập (Required Check)
    if (isRequired && value === '') {
        showFieldError(inputElement, 'Trường này không được để trống.');
        return false;
    }

    // Nếu không bắt buộc và đang trống -> Bỏ qua hợp lệ
    if (!isRequired && value === '') {
        clearFieldError(inputElement);
        return true;
    }

    // Rule 2: Kiểm tra Họ và Tên (Tự động viết hoa từ đầu, không chứa số, tối thiểu 2 ký tự)
    if (name === 'name' || name === 'contactPerson' || akaType === 'name') {
        if (/\d/.test(value)) {
            showFieldError(inputElement, 'Họ và tên không được chứa chữ số.');
            return false;
        }
        if (value.length < 2) {
            showFieldError(inputElement, 'Họ và tên phải có ít nhất 2 ký tự.');
            return false;
        }
    }

    // Rule 3: Kiểm tra Tên đăng nhập (Từ 5 ký tự trở lên và chữ cái đầu tiên)
    if (name === 'username' || akaType === 'username') {
        const usernameRegex = /^[a-zA-Z][a-zA-Z0-9._@\-]{4,}$/;
        if (!usernameRegex.test(value)) {
            showFieldError(inputElement, 'Tên đăng nhập phải từ 5 ký tự trở lên và bắt đầu bằng chữ cái.');
            return false;
        }
    }

    // Rule 4: Kiểm tra Email (Bắt buộc đuôi @gmail.com)
    if (type === 'email' || name === 'email' || akaType === 'email') {
        const gmailRegex = /^[a-zA-Z0-9._%+-]+@gmail\.com$/i;
        if (!gmailRegex.test(value)) {
            showFieldError(inputElement, 'Email phải đúng định dạng và có đuôi @gmail.com.');
            return false;
        }
    }

    // Rule 5: Kiểm tra Mật khẩu (Từ 6 ký tự trở lên)
    if (type === 'password' || name === 'password' || name === 'newPassword') {
        if (value.length < 6) {
            showFieldError(inputElement, 'Mật khẩu phải từ 6 ký tự trở lên.');
            return false;
        }
    }

    // Rule 6: Kiểm tra Số điện thoại (Chỉ số, 10 - 11 số, bắt đầu bằng 03, 05, 07, 08, 09)
    if (name === 'phone' || type === 'tel' || akaType === 'phone') {
        const phoneClean = value.replace(/\D/g, '');
        if (/\D/.test(value)) {
            showFieldError(inputElement, 'Số điện thoại không được chứa chữ cái hay ký tự đặc biệt.');
            return false;
        }
        const phoneRegex = /^(03|05|07|08|09)\d{8,9}$/;
        if (!phoneRegex.test(phoneClean)) {
            showFieldError(inputElement, 'Số điện thoại phải từ 10 đến 11 chữ số và bắt đầu bằng các đầu số 03, 05, 07, 08, 09.');
            return false;
        }
    }

    // Rule 7: Kiểm tra Ngày sinh (Bắt buộc chọn và phải nhỏ hơn ngày hiện tại)
    if (name === 'dob' || (type === 'date' && (isRequired || akaType === 'dob'))) {
        if (!value) {
            showFieldError(inputElement, 'Ngày sinh là bắt buộc, vui lòng chọn ngày sinh.');
            return false;
        }
        const selectedDate = new Date(value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        if (selectedDate >= today) {
            showFieldError(inputElement, 'Ngày sinh không được bằng hoặc lớn hơn ngày hiện tại.');
            return false;
        }
    }

    clearFieldError(inputElement);
    return true;
}

// 6. HÀM KIỂM TRA TOÀN BỘ FORM
function validateFullForm(formElement) {
    let allInputs = [];

    const internalInputs = Array.from(formElement.querySelectorAll('input, select, textarea'));
    allInputs = allInputs.concat(internalInputs);

    if (formElement.id) {
        const linkedInputs = Array.from(document.querySelectorAll(`input[form="${formElement.id}"], select[form="${formElement.id}"], textarea[form="${formElement.id}"]`));
        allInputs = allInputs.concat(linkedInputs);
    }

    const uniqueInputs = Array.from(new Set(allInputs));
    let isAllValid = true;
    let firstErrorInput = null;

    uniqueInputs.forEach(function (input) {
        if (!validateSingleField(input)) {
            isAllValid = false;
            if (!firstErrorInput) firstErrorInput = input;
        }
    });

    const passwordInput = formElement.querySelector('input[name="password"]') || formElement.querySelector('input[name="newPassword"]');
    const confirmPasswordInput = formElement.querySelector('input[name="confirmPassword"]');
    if (passwordInput && confirmPasswordInput && passwordInput.value && confirmPasswordInput.value) {
        if (passwordInput.value !== confirmPasswordInput.value) {
            showFieldError(confirmPasswordInput, 'Mật khẩu xác nhận không trùng khớp.');
            isAllValid = false;
            if (!firstErrorInput) firstErrorInput = confirmPasswordInput;
        }
    }

    if (firstErrorInput) {
        firstErrorInput.scrollIntoView({ behavior: 'smooth', block: 'center' });
        try { firstErrorInput.focus(); } catch (e) {}
    }

    return isAllValid;
}

// 7. TÍNH NĂNG TÌM KIẾM THỜI GIAN THỰC — CHỈ DÙNG CHO THANH TÌM KIẾM BẢNG (BỎ QUA TẤT CẢ Ô SỬA DỮ LIỆU)
function initRealtimeSearch() {
    // A. Ẩn toàn bộ nút "Tìm" dạng submit bằng class CSS
    document.querySelectorAll('form button[type="submit"]').forEach(function (btn) {
        if (btn.textContent && btn.textContent.trim() === 'Tìm') {
            btn.classList.add('search-submit-btn-hidden');
        }
    });

    // B. Đảm bảo CHỈ CÁC Ô TÌM KIẾM (table-search-bar / data-aka-search="true") mới được bọc Icon kính lúp
    document.querySelectorAll('input.table-search-bar, input[data-aka-search="true"]').forEach(function (input) {
        if (input.classList.contains('inline-edit-input') || input.closest('.inline-edit-form') || input.closest('.inline-edit-row')) {
            return;
        }

        let wrapper = input.closest('.search-input-wrapper');
        if (!wrapper) {
            wrapper = document.createElement('div');
            wrapper.className = 'search-input-wrapper';
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);
        }

        let svg = wrapper.querySelector('svg.iphone-search-svg');
        if (!svg) {
            wrapper.insertAdjacentHTML('afterbegin', `
                <svg class="iphone-search-svg" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#8e8e93" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="11" cy="11" r="8"></circle>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
            `);
        }

        if (input.placeholder && input.placeholder.includes('🔍')) {
            input.placeholder = input.placeholder.replace('🔍', '').trim();
        }
    });

    // C. LẮNG NGHE SỰ KIỆN INPUT THỜI GIAN THỰC — BỎ QUA HOÀN TOÀN TẤT CẢ CÁC Ô NHẬP LIỆU CHỈNH SỬA
    document.addEventListener('input', function (e) {
        const input = e.target;
        if (!input || !input.classList) return;

        // TUYỆT ĐỐI BỎ QUA NẾU LÀ Ô CHỈNH SỬA DỮ LIỆU HOẶC NẰM TRONG DÒNG INLINE EDIT
        if (input.classList.contains('inline-edit-input') || 
            input.closest('.inline-edit-form') || 
            input.closest('.inline-edit-row')) {
            return;
        }

        const isSearchInput = input.classList.contains('table-search-bar') ||
                              input.dataset.akaSearch === 'true' ||
                              (input.name && input.name.toLowerCase().includes('keyword'));

        if (!isSearchInput) return;

        const query = (input.value || '').toLowerCase().trim();
        const container = input.closest('.card-box') || input.closest('main') || document;
        const table = container.querySelector('table.data-table') || container.querySelector('table');

        if (!table) return;

        const rows = Array.from(table.querySelectorAll('tbody tr:not(.spacer-row)'));
        rows.forEach(function (row) {
            if (row.querySelector('.empty-table-cell') || row.classList.contains('inline-edit-row')) return;

            const text = (row.textContent || '').toLowerCase();
            if (query === '' || text.indexOf(query) !== -1) {
                row.classList.remove('search-row-hidden');
                row.style.display = '';
            } else {
                row.classList.add('search-row-hidden');
                row.style.display = 'none';
            }
        });
    }, true);
}

// 8. KHỞI TẠO TOAST VÀ BẮT THÔNG BÁO KHI TẢI TRANG
document.addEventListener('DOMContentLoaded', function () {
    initRealtimeSearch();

    // Không bắn Toast góc phải trên các trang Đăng nhập / Auth (vì form đã có alert box trực tiếp bên trong)
    const isAuthPage = document.querySelector('.login-container') || 
                       document.querySelector('.auth-form') || 
                       document.querySelector('.auth-card') || 
                       document.querySelector('.auth-wrapper');

    if (!isAuthPage) {
        document.querySelectorAll('.alert-success').forEach(function (alertEl) {
            if (alertEl.id === 'otpSuccessBadge' || alertEl.offsetParent === null || (alertEl.style && alertEl.style.display === 'none')) return;
            const msg = alertEl.textContent ? alertEl.textContent.trim() : '';
            if (msg) {
                showToastNotification(msg, 'success');
            }
        });

        document.querySelectorAll('.alert-error, .alert-danger').forEach(function (alertEl) {
            if (alertEl.offsetParent === null || (alertEl.style && alertEl.style.display === 'none')) return;
            const msg = alertEl.textContent ? alertEl.textContent.trim() : '';
            if (msg) {
                showToastNotification(msg, 'error');
            }
        });
    }

    document.querySelectorAll('input[name="phone"], input[type="tel"], input[data-aka-type="phone"]').forEach(function (phoneInput) {
        phoneInput.setAttribute('maxlength', '11');
        phoneInput.setAttribute('inputmode', 'numeric');
    });
});

// A. Chặn gõ chữ trên bàn phím cho các ô Số điện thoại
document.addEventListener('keydown', function (e) {
    const target = e.target;
    if (target && (target.name === 'phone' || target.type === 'tel' || (target.dataset && target.dataset.akaType === 'phone'))) {
        const allowedKeys = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'Enter'];
        if (allowedKeys.includes(e.key) || e.ctrlKey || e.metaKey) return;
        if (!/^[0-9]$/.test(e.key)) {
            e.preventDefault();
        }
    }
}, true);

// B. Tự động viết hoa các từ đầu của Họ tên & làm sạch số điện thoại KHÔNG REALTIME (chỉ khi rời ô - blur)
// B. Tự động viết hoa các từ đầu của Họ tên & làm sạch số điện thoại KHÔNG REALTIME (chỉ khi rời ô - blur)
document.addEventListener('blur', function (e) {
    const target = e.target;
    if (!target) return;

    // BỎ QUA HOÀN TOÀN VALIDATE KHI BLUR CHO TRANG ĐĂNG NHẬP / AUTH (CHỈ VALIDATE KHI BẤM SUBMIT)
    const form = target.closest('form');
    if (form && (form.classList.contains('auth-form') || 
                 form.dataset.akaValidateOnSubmitOnly === 'true' || 
                 target.closest('.login-container') ||
                 target.closest('.auth-card') ||
                 target.closest('.auth-wrapper'))) {
        return;
    }

    if (target.name === 'phone' || target.type === 'tel' || (target.dataset && target.dataset.akaType === 'phone')) {
        target.value = target.value.replace(/\D/g, '').slice(0, 11);
    }

    if (target.name === 'name' || target.name === 'contactPerson' || (target.dataset && target.dataset.akaType === 'name')) {
        target.value = autoCapitalizeName(target.value);
    }

    if (target.tagName === 'INPUT' || target.tagName === 'SELECT' || target.tagName === 'TEXTAREA') {
        validateSingleField(target);
    }
}, true);

// C. Bắt sự kiện SUBMIT cho TẤT CẢ CÁC FORM (Bỏ qua hoàn toàn form Đăng nhập để Backend xử lý 100%)
document.addEventListener('submit', function (e) {
    const form = e.target;
    if (form && form.tagName === 'FORM') {
        // Bỏ qua hoàn toàn validation JS cho form đăng nhập / auth-form
        if (form.classList.contains('auth-form') || (form.action && form.action.includes('/auth/login'))) {
            return;
        }

        form.setAttribute('novalidate', 'true');
        if (!validateFullForm(form)) {
            e.preventDefault();
            e.stopPropagation();
        }
    }
}, true);

// D. TỰ ĐỘNG MỞ LINK URL ẢNH TRONG TAB MỚI KHI BẤM VÀO BẤT KỲ ẢNH NÀO
document.addEventListener('click', function(e) {
    const target = e.target.closest('img.avatar-thumb-sm, img.avatar-img, img.table-img, img.attendance-img, img[data-aka-preview="true"]');
    if (!target || !target.src) return;

    // Bỏ qua nếu là khu vực bấm để tải lên ảnh đại diện trong Hồ sơ
    if (target.closest('.avatar-upload-wrapper')) return;

    // Mở trực tiếp liên kết URL của ảnh trong tab mới
    window.open(target.src, '_blank');
});
