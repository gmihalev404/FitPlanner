window.addEventListener('error', function (event) {
    const target = event.target;

    if (target.tagName === 'IMG') {
        const isTrainer = target.classList.contains('trainer-avatar');
        const fallbackPath = isTrainer ? '/images/default-user-image.webp' : '/default-program-image.png';

        // To avoid infinite loops, only update if the src is not already the fallback
        if (!target.src.includes(fallbackPath)) {
            target.src = fallbackPath;
        }
    }
}, true); // Use capture phase to ensure we catch the error event