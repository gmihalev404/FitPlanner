const searchInput = document.getElementById("exerciseSearch");
const exercises = document.querySelectorAll(".exercise-item");
searchInput.addEventListener("input", function () {
    const query = this.value.toLowerCase().trim();
    exercises.forEach(ex => {
        const name = ex.dataset.name; ex.style.display = name.includes(query) ? "" : "none"; });
});