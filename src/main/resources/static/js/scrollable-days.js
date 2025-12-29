document.addEventListener('DOMContentLoaded', function() {
    const container = document.getElementById('daysContainer');
    const leftBtn = document.getElementById('leftArrow');
    const rightBtn = document.getElementById('rightArrow');

    if (container && leftBtn && rightBtn) {
        function getScrollValue() {
            const item = container.querySelector('.scroll-snap-item');
            const gap = 15;
            return item ? item.offsetWidth + gap : 300;
        }

        leftBtn.onclick = () => container.scrollBy({ left: -getScrollValue(), behavior: 'smooth' });
        rightBtn.onclick = () => container.scrollBy({ left: getScrollValue(), behavior: 'smooth' });

        container.scrollLeft = 0;
    }
});