const programForm = document.getElementById('programForm');

programForm.querySelectorAll('input, select').forEach(el => {
    el.addEventListener('change', () => {
        const formData = new FormData(programForm);
        fetch('/update-program-session', {
            method: 'POST',
            body: new URLSearchParams(formData)
        })
            .then(response => {
                if (!response.ok) {
                    console.error('Failed to update session');
                }
                return response.text();
            })
            .catch(error => console.error('Error updating session:', error));
    });
});
