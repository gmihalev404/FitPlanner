document.addEventListener("DOMContentLoaded", function () {

    const searchInput = document.getElementById("exerciseSearch");

    if (!searchInput || !window.applyExerciseFilters) return;

    searchInput.addEventListener("input", function () {
        const query = this.value.toLowerCase().trim();
        window.applyExerciseFilters(query);
    });
});