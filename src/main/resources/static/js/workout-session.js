document.addEventListener('DOMContentLoaded', function() {
    const isViewOnly = [[${viewOnly}]];
    if (isViewOnly) return;

    const allCheckboxes = document.querySelectorAll('.set-checkbox');
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');

    function updateProgress() {
        const total = allCheckboxes.length;
        const checked = Array.from(allCheckboxes).filter(c => c.checked).length;
        const percent = total > 0 ? Math.round((checked / total) * 100) : 0;

        progressBar.style.width = percent + '%';
        progressBar.setAttribute('aria-valuenow', percent);
        progressText.innerText = percent + '%';
    }

    document.querySelectorAll('.exercise-card').forEach(card => {
        const checkboxes = card.querySelectorAll('.set-checkbox');
        const setsCompletedInput = card.querySelector('.sets-completed-input');
        const finishedInput = card.querySelector('.finished-input');

        checkboxes.forEach(cb => {
            cb.addEventListener('change', () => {
                const completedCount = Array.from(checkboxes).filter(ch => ch.checked).length;

                // Update hidden inputs for backend
                setsCompletedInput.value = completedCount;
                finishedInput.value = (completedCount === checkboxes.length);

                // Update UI appearance
                checkboxes.forEach(ch => {
                    const label = ch.nextElementSibling;
                    const statusSpan = label.nextElementSibling;
                    if (ch.checked) {
                        label.classList.add('completed-label');
                        statusSpan.style.display = 'inline';
                    } else {
                        label.classList.remove('completed-label');
                        statusSpan.style.display = 'none';
                    }
                });

                updateProgress();
            });
        });
    });

    // Suggestion buttons
    document.querySelectorAll('.accept-btn, .decline-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const exerciseId = this.getAttribute('data-id');
            const isAccept = this.classList.contains('accept-btn');

            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = isAccept ? 'acceptedIncreaseIds' : 'declinedIncreaseIds';
            input.value = exerciseId;

            document.getElementById('sessionForm').appendChild(input);

            const parent = this.closest('div.border');
            parent.innerHTML = '<span class="text-muted small italic"><i class="bi bi-info-circle"></i> Choice recorded. Submit session to apply.</span>';
        });
    });
});