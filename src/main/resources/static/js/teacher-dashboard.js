document.addEventListener('DOMContentLoaded', function () {
    const appContainer = document.querySelector('.app-container');
    const toggleBtn = document.getElementById('sidebarToggleBtn');

    // Hàm cập nhật Icon mũi tên thuần: Mở rộng (chevron-right: >), Rút gọn (chevron-left: <)
    function updateToggleIcon(isCollapsed) {
        if (!toggleBtn) return;
        toggleBtn.innerHTML = isCollapsed 
            ? '<i data-lucide="chevron-right" title="Mở rộng Menu"></i>' 
            : '<i data-lucide="chevron-left" title="Thu gọn Menu"></i>';
        if (typeof lucide !== 'undefined') {
            lucide.createIcons();
        }
    }

    // Phục hồi trạng thái Sidebar từ localStorage khi tải trang
    const savedState = localStorage.getItem('aka_sidebar_collapsed');
    if (savedState === 'true') {
        appContainer.classList.add('sidebar-collapsed');
        updateToggleIcon(true);
    } else {
        updateToggleIcon(false);
    }

    if (toggleBtn) {
        toggleBtn.addEventListener('click', function () {
            appContainer.classList.toggle('sidebar-collapsed');
            const isCollapsed = appContainer.classList.contains('sidebar-collapsed');
            localStorage.setItem('aka_sidebar_collapsed', isCollapsed);
            updateToggleIcon(isCollapsed);
        });
    }
});
