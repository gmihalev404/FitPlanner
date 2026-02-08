document.addEventListener("DOMContentLoaded", function () {
    const searchInputs = document.querySelectorAll(".search-input");

    searchInputs.forEach(input => {
        input.addEventListener("input", function () {
            const query = this.value.toLowerCase().trim();
            const targetSelector = this.dataset.targetSelector;

            // SPECIAL CASE: If we are searching exercises, use the unified filter
            if (targetSelector === '.exercise-item' && typeof window.applyExerciseFilters === 'function') {
                window.applyExerciseFilters(query);
                return; // Let filters.js handle the rest
            }

            // DEFAULT CASE: For trainers or simple lists
            if (!targetSelector) return;
            const items = document.querySelectorAll(targetSelector);
            items.forEach(item => {
                const text = item.textContent.toLowerCase().trim();
                item.style.display = text.includes(query) ? "" : "none";
            });
        });
    });
});