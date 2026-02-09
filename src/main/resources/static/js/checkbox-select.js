document.addEventListener("DOMContentLoaded", function() {
    // 1. Вземаме параметрите от URL-а
    const urlParams = new URLSearchParams(window.location.search);
    const programIdFromUrl = urlParams.get('programId');

    if (programIdFromUrl) {
        // 2. Търсим чекбокса, който има data-id равен на нашето ID
        const targetCheckbox = document.querySelector(`.program-checkbox[data-id="${programIdFromUrl}"]`);

        if (targetCheckbox) {
            // 3. Отбелязваме го
            targetCheckbox.checked = true;

            // 4. Извикваме updateCalendar(), за да се оцвети календара веднага
            if (typeof updateCalendar === "function") {
                updateCalendar();
            }

            // Опционално: Скролване до програмата, ако списъкът е дълъг
            targetCheckbox.closest('.program-row').scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
    }
});