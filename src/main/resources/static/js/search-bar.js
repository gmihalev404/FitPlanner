document.addEventListener("DOMContentLoaded", function () {
    const searchInputs = document.querySelectorAll(".search-input");

    searchInputs.forEach(input => {
        input.addEventListener("input", function () {
            const query = this.value.toLowerCase().trim();
            const targetSelector = this.dataset.targetSelector;

            if (!targetSelector) return;

            const items = document.querySelectorAll(targetSelector);
            items.forEach(item => {
                const text = item.textContent.toLowerCase().trim();
                item.style.display = text.includes(query) ? "" : "none";
            });
        });
    });
});
