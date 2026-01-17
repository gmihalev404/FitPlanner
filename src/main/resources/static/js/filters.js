document.addEventListener("DOMContentLoaded", function () {
    const categoryFilter = document.getElementById("categoryFilter");
    const typeFilter = document.getElementById("typeFilter");
    const equipmentFilter = document.getElementById("equipmentFilter");
    const noResults = document.getElementById("noResults");

    const exercises = document.querySelectorAll(".exercise-item");

    if (!categoryFilter || !typeFilter || !equipmentFilter) return;

    window.applyExerciseFilters = function (query = "") {
        query = query.toLowerCase().trim();
        let hasVisible = false;

        exercises.forEach(ex => {
            const name = ex.dataset.name?.toLowerCase() || "";
            const category = ex.dataset.category || "";
            const type = ex.dataset.type || "";
            const equipment = ex.dataset.equipment || "";

            const matches =
                name.includes(query) &&
                (!categoryFilter.value || category === categoryFilter.value) &&
                (!typeFilter.value || type === typeFilter.value) &&
                (!equipmentFilter.value || equipment === equipmentFilter.value);

            ex.style.display = matches ? "" : "none";
            if (matches) hasVisible = true;
        });

        if (noResults) {
            noResults.style.display = hasVisible ? "none" : "block";
        }
    };

    categoryFilter.addEventListener("change", () => applyExerciseFilters());
    typeFilter.addEventListener("change", () => applyExerciseFilters());
    equipmentFilter.addEventListener("change", () => applyExerciseFilters());
});