// --- Calendar JS --- //
let currentDate = new Date();

function getSelectedWorkoutDates() {
    const selectedCheckboxes = document.querySelectorAll(".program-checkbox:checked");
    const dates = new Set();
    selectedCheckboxes.forEach(cb => {
        const programDates = cb.dataset.dates ? cb.dataset.dates.split(',') : [];
        programDates.forEach(d => dates.add(d));
    });
    return [...dates];
}

function isToday(year, month, day) {
    const today = new Date();
    return today.getFullYear() === year &&
        today.getMonth() === month &&
        today.getDate() === day;
}

function loadCalendar() {
    const workoutDates = getSelectedWorkoutDates();
    const calendarDays = document.getElementById("calendarDays");
    calendarDays.innerHTML = "";

    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();

    const firstDay = new Date(year, month, 1).getDay();
    const firstDayIndex = (firstDay === 0) ? 6 : firstDay - 1;

    const lastDate = new Date(year, month + 1, 0).getDate();

    document.getElementById("monthDisplay").innerText =
        currentDate.toLocaleDateString("en-US", { month: "long", year: "numeric" });

    for (let i = 0; i < firstDayIndex; i++) {
        const emptyDiv = document.createElement('div');
        emptyDiv.className = 'day empty';
        calendarDays.appendChild(emptyDiv);
    }

    for (let d = 1; d <= lastDate; d++) {
        const dateStr = `${year}-${String(month + 1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
        const hasWorkout = workoutDates.includes(dateStr);

        const dayDiv = document.createElement('div');
        dayDiv.className = `day ${isToday(year, month, d) ? 'today' : ''} ${hasWorkout ? 'workout-day' : ''}`;
        dayDiv.innerText = d;
        calendarDays.appendChild(dayDiv);
    }
}

function updateCalendar() {
    loadCalendar();
}

document.getElementById("selectAllPrograms").addEventListener("change", function() {
    const checkboxes = document.querySelectorAll(".program-checkbox");
    checkboxes.forEach(cb => cb.checked = this.checked);
    updateCalendar();
});

document.getElementById("prevMonth").onclick = () => {
    currentDate.setMonth(currentDate.getMonth() - 1);
    loadCalendar();
};

document.getElementById("nextMonth").onclick = () => {
    currentDate.setMonth(currentDate.getMonth() + 1);
    loadCalendar();
};

loadCalendar();