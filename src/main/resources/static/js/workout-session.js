document.addEventListener('DOMContentLoaded', function() {
    const isViewOnly = window.workoutConfig.viewOnly; // Fixed reference

    const allCheckboxes = document.querySelectorAll('.set-checkbox');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');

    function updateUIForCheckbox(ch) {
        const label = ch.nextElementSibling;
        // Find the status span (it might be the second next element depending on whitespace)
        const statusSpan = ch.parentElement.querySelector('.status-text');

        if (ch.checked) {
            label.classList.add('completed-label');
            if(statusSpan) statusSpan.style.display = 'inline';
        } else {
            label.classList.remove('completed-label');
            if(statusSpan) statusSpan.style.display = 'none';
        }
    }

    function updateProgress() {
        const total = allCheckboxes.length;
        const checked = Array.from(allCheckboxes).filter(c => c.checked).length;
        const percent = total > 0 ? Math.round((checked / total) * 100) : 0;

        if(progressBar) {
            progressBar.style.width = percent + '%';
            progressBar.setAttribute('aria-valuenow', percent);
        }
        if(progressText) progressText.innerText = percent + '%';
    }

    // 1. Initial UI Setup: Loop through checkboxes to set correct styles for already-checked sets
    allCheckboxes.forEach(ch => {
        updateUIForCheckbox(ch);

        // 2. Add Event Listeners only if NOT view-only
        if (!isViewOnly) {
            ch.addEventListener('change', () => {
                const card = ch.closest('.exercise-card');
                const cardCheckboxes = card.querySelectorAll('.set-checkbox');
                const setsCompletedInput = card.querySelector('.sets-completed-input');
                const finishedInput = card.querySelector('.finished-input');

                const completedCount = Array.from(cardCheckboxes).filter(c => c.checked).length;
                setsCompletedInput.value = completedCount;
                finishedInput.value = (completedCount === cardCheckboxes.length);

                updateUIForCheckbox(ch);
                updateProgress();
            });
        }
    });

    // Run once on load to ensure progress bar matches the saved state
    updateProgress();
});