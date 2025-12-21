document.addEventListener("DOMContentLoaded", function () {
    const categoryFilter = document.getElementById("categoryFilter");
    const difficultyFilter = document.getElementById("difficultyFilter");
    const equipmentFilter = document.getElementById("equipmentFilter");
    const noResults = document.getElementById("noResults");
    const exercises = document.querySelectorAll(".exercise-item");

    if (!categoryFilter || !difficultyFilter || !equipmentFilter) return;

    window.applyExerciseFilters = function (query = "") {
        let hasVisible = false;
        exercises.forEach(ex => {
            const name = ex.dataset.name;
            const category = ex.dataset.category;
            const difficulty = ex.dataset.difficulty;
            const equipment = ex.dataset.equipment;
            const matches =
                name.includes(query) &&
                (!categoryFilter.value || category === categoryFilter.value) &&
                (!difficultyFilter.value || difficulty === difficultyFilter.value) &&
                (!equipmentFilter.value || equipment === equipmentFilter.value);
            ex.style.display = matches ? "" : "none";
            if (matches) hasVisible = true;
        });

        if (noResults) noResults.style.display = hasVisible ? "none" : "block";
    };

    categoryFilter.addEventListener("change", () => applyExerciseFilters());
    difficultyFilter.addEventListener("change", () => applyExerciseFilters());
    equipmentFilter.addEventListener("change", () => applyExerciseFilters());
});