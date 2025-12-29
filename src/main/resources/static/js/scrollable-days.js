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

        leftBtn.onclick = () => {
            const cardWidth = container.querySelector('.scroll-snap-item').offsetWidth;
            const gap = 20;
            container.scrollBy({ left: -(cardWidth + gap), behavior: 'smooth' });
        };

        rightBtn.onclick = () => {
            const cardWidth = container.querySelector('.scroll-snap-item').offsetWidth;
            const gap = 20;
            container.scrollBy({ left: (cardWidth + gap), behavior: 'smooth' });
        };
        container.scrollLeft = 0;
    }
});