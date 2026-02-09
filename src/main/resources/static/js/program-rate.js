document.addEventListener('DOMContentLoaded', function() {
    const ratingForm = document.getElementById('ratingForm');
    const stars = ratingForm.querySelectorAll('input[name="rating"]');

    stars.forEach(star => {
        star.addEventListener('change', () => {
            // Визуално потвърждение преди изпращане
            ratingForm.style.opacity = '0.5';
            ratingForm.submit();
        });
    });
});