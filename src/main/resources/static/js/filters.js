document.addEventListener("DOMContentLoaded", function () {
    const categoryFilter = document.getElementById("categoryFilter");
    const typeFilter = document.getElementById("typeFilter");
    const equipmentFilter = document.getElementById("equipmentFilter");
    const searchInput = document.getElementById("exerciseSearch"); // Ensure this ID matches your search input
    const noResults = document.getElementById("noResults");
    const exercises = document.querySelectorAll(".exercise-item");

    // Exit if filters aren't on this specific page
    if (!categoryFilter || !typeFilter || !equipmentFilter) return;

    /**
     * The Master Filter Function
     * Reads all current filter states and applies them to the grid
     */
    window.applyExerciseFilters = function () {
        // 1. Get all current values
        const query = searchInput ? searchInput.value.toLowerCase().trim() : "";
        const catValue = categoryFilter.value.toLowerCase();
        const typeValue = typeFilter.value.toLowerCase();
        const equipValue = equipmentFilter.value.toLowerCase();

        let hasVisible = false;

        // 2. Loop through every exercise and check ALL conditions
        exercises.forEach(ex => {
            const name = ex.dataset.name || "";
            const category = ex.dataset.category || "";
            const type = ex.dataset.type || "";
            const equipment = ex.dataset.equipment || "";

            // Logical AND: All conditions must be true
            const matchesSearch = name.includes(query);
            const matchesCat = !catValue || category === catValue;
            const matchesType = !typeValue || type === typeValue;
            const matchesEquip = !equipValue || equipment === equipValue;

            const isVisible = matchesSearch && matchesCat && matchesType && matchesEquip;

            // 3. Apply visibility
            ex.style.display = isVisible ? "" : "none";
            if (isVisible) hasVisible = true;
        });

        // 4. Handle "No Results" message
        if (noResults) {
            noResults.style.display = hasVisible ? "none" : "block";
        }
    };

    // Listen for changes on all three dropdowns
    categoryFilter.addEventListener("change", window.applyExerciseFilters);
    typeFilter.addEventListener("change", window.applyExerciseFilters);
    equipmentFilter.addEventListener("change", window.applyExerciseFilters);
});