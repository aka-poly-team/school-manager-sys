/**
 * INLINE TABLE EDITING SYSTEM (SỬA TRỰC TIẾP TRÊN BẢNG KHÔNG CẦN QUA FORM MỚI)
 * -----------------------------------------------------------------------------
 * Mã nguồn JavaScript thuần 100%, KHÔNG CHỨA BẤT KỲ CSS NÀO TRONG FILE JS.
 */
function enableInlineEdit(rowId, updateUrl, fieldNames) {
    const row = document.getElementById(rowId);
    if (!row) return;

    // Lưu lại nội dung HTML ban đầu để phục hồi khi bấm Hủy
    if (!row.dataset.originalHtml) {
        row.dataset.originalHtml = row.innerHTML;
    }

    const cells = row.querySelectorAll("td");

    // Biến các ô dữ liệu thành input trực tiếp
    fieldNames.forEach((fieldConfig, index) => {
        const cell = cells[index + 1]; // Ô index 0 là Mã ID
        if (cell) {
            let fieldName = typeof fieldConfig === 'object' ? fieldConfig.name : fieldConfig;
            let fieldType = typeof fieldConfig === 'object' ? fieldConfig.type : 'text';
            let options = typeof fieldConfig === 'object' ? fieldConfig.options : null;

            let currentVal = cell.innerText.trim();
            if (currentVal.endsWith(' cháu')) currentVal = currentVal.replace(' cháu', '');
            if (currentVal.endsWith(' tiết')) currentVal = currentVal.replace(' tiết', '');

            if (fieldType === 'select' && options) {
                let selectHtml = `<select name="${fieldName}" class="table-search-input-ios inline-edit-input">`;
                options.forEach(opt => {
                    let selected = (opt.text === currentVal || opt.value == currentVal) ? 'selected' : '';
                    selectHtml += `<option value="${opt.value}" ${selected}>${opt.text}</option>`;
                });
                selectHtml += `</select>`;
                cell.innerHTML = selectHtml;
            } else {
                cell.innerHTML = `<input type="${fieldType}" name="${fieldName}" value="${currentVal}" class="table-search-input-ios inline-edit-input">`;
            }
        }
    });

    // Chuyển nút Sửa/Xóa thành nút Lưu/Hủy
    const actionCell = cells[cells.length - 1];
    actionCell.innerHTML = `
        <div class="form-actions form-actions-reset center-actions">
            <button type="button" onclick="submitInlineEdit('${rowId}', '${updateUrl}')" class="action-btn green">Lưu</button>
            <button type="button" onclick="cancelInlineEdit('${rowId}')" class="action-btn gray">Hủy</button>
        </div>
    `;
}

function cancelInlineEdit(rowId) {
    const row = document.getElementById(rowId);
    if (row && row.dataset.originalHtml) {
        row.innerHTML = row.dataset.originalHtml;
        delete row.dataset.originalHtml;
    }
}

function submitInlineEdit(rowId, updateUrl) {
    const row = document.getElementById(rowId);
    if (!row) return;

    const inputs = row.querySelectorAll("input, select");
    const formData = new FormData();
    inputs.forEach(input => {
        formData.append(input.name, input.value);
    });

    row.classList.add('row-loading-state');

    fetch(updateUrl, {
        method: "POST",
        body: formData,
        headers: {
            "X-Requested-With": "XMLHttpRequest"
        }
    })
    .then(response => {
        window.location.reload();
    })
    .catch(err => {
        console.error("Inline edit error:", err);
        window.location.reload();
    });
}
