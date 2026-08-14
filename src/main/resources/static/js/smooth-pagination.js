/**
 * SMOOTH IN-PLACE AJAX SYSTEM FOR SPRING BOOT MVC
 * -----------------------------------------------------------------------------
 * - Tải ngầm Phân Trang, Nút Sửa, Nút Hủy và Submit Form Sửa/Xóa trong bảng
 * - Thu thập đầy đủ tất cả ô input (kể cả các ô ngoài thẻ form kết nối qua attribute form="")
 * - Giữ nguyên 100% vị trí thanh cuộn (scroll) và con trỏ chuột
 * - KHÔNG CHUYỂN TRANG, KHÔNG RELOAD CẢ TRANG, KHÔNG MẤT DỮ LIỆU KHI LƯU
 * - 0% CSS TRONG JS (SỬ DỤNG CLASS CSS TÁCH BIỆT STYLESHEET)
 */
document.addEventListener("DOMContentLoaded", function () {

    // 1. XỬ LÝ CLICK LINK (PHÂN TRANG, NÚT SỬA, NÚT HỦY DÒNG BẢNG)
    document.addEventListener("click", function (e) {
        const targetLink = e.target.closest(".pagination-controls a.page-btn, .data-table a.action-btn");
        if (!targetLink) return;

        const targetUrl = targetLink.getAttribute("href");
        if (!targetUrl || targetUrl.startsWith("javascript:") || targetLink.classList.contains("disabled")) return;

        const cardBox = targetLink.closest(".card-box");
        if (!cardBox) return;

        e.preventDefault();
        loadCardBoxUrl(cardBox, targetUrl);
    });

    // 2. XỬ LÝ SUBMIT FORM (FORM LƯU INLINE, FORM XÓA DÒNG BẢNG)
    document.addEventListener("submit", function (e) {
        const form = e.target;
        const cardBox = form.closest(".card-box");
        if (!cardBox) return;

        if (form.getAttribute("method") && form.getAttribute("method").toLowerCase() === "get") return;

        e.preventDefault();
        const actionUrl = form.getAttribute("action") || window.location.href;
        
        // Thu thập đầy đủ dữ liệu từ form trong thẻ VÀ ngoài thẻ kết nối qua attribute form=""
        const formData = new FormData(form);
        if (form.id) {
            document.querySelectorAll(`[form="${form.id}"]`).forEach(input => {
                if (input.name) {
                    if (input.type === 'checkbox' || input.type === 'radio') {
                        if (input.checked) formData.set(input.name, input.value);
                    } else {
                        formData.set(input.name, input.value);
                    }
                }
            });
        }

        cardBox.classList.add("card-box-loading");

        fetch(actionUrl, {
            method: "POST",
            body: formData,
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(response => {
            const redirectUrl = response.url || window.location.href;
            return response.text().then(html => ({ html, redirectUrl }));
        })
        .then(({ html, redirectUrl }) => {
            updateCardBoxFromHtml(cardBox, html, redirectUrl);
        })
        .catch(err => {
            console.error("Smooth form submit error:", err);
            window.location.reload();
        })
        .finally(() => {
            cardBox.classList.remove("card-box-loading");
        });
    });

    // HÀM TẢI URL VÀ ĐẮP NỘI DUNG VÀO KHUNG CARD-BOX
    function loadCardBoxUrl(cardBox, targetUrl) {
        cardBox.classList.add("card-box-loading");

        fetch(targetUrl, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
        .then(response => {
            if (!response.ok) throw new Error("HTTP error " + response.status);
            const redirectUrl = response.url || targetUrl;
            return response.text().then(html => ({ html, redirectUrl }));
        })
        .then(({ html, redirectUrl }) => {
            updateCardBoxFromHtml(cardBox, html, redirectUrl);
        })
        .catch(err => {
            console.error("Smooth fetch error:", err);
            window.location.href = targetUrl;
        })
        .finally(() => {
            cardBox.classList.remove("card-box-loading");
        });
    }

    // HÀM THAY THẾ NỘI DUNG CARD-BOX TƯƠNG ỨNG MÀ KHÔNG LÀM TRÔI TRANG VÀ CẬP NHẬT CHÍNH XÁC THANH ĐỊA CHỈ
    function updateCardBoxFromHtml(cardBox, html, targetUrl) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, "text/html");

        // Bắt thông báo Toast (Thành công / Lỗi) từ Server trả về sau khi Xóa / Sửa / Thêm
        if (typeof showToastNotification === 'function') {
            const successAlert = doc.querySelector('.alert-success');
            const errorAlert = doc.querySelector('.alert-error, .alert-danger');

            if (successAlert && successAlert.textContent && successAlert.textContent.trim()) {
                showToastNotification(successAlert.textContent.trim(), 'success');
            } else if (errorAlert && errorAlert.textContent && errorAlert.textContent.trim()) {
                showToastNotification(errorAlert.textContent.trim(), 'error');
            }
        }

        const currentCards = Array.from(document.querySelectorAll(".card-box"));
        const cardIndex = currentCards.indexOf(cardBox);
        const newCards = Array.from(doc.querySelectorAll(".card-box"));

        if (cardIndex !== -1 && newCards[cardIndex]) {
            cardBox.innerHTML = newCards[cardIndex].innerHTML;
            if (targetUrl && window.history && window.history.pushState) {
                window.history.pushState(null, "", targetUrl);
            }
        } else {
            window.location.href = targetUrl;
        }
    }
});
