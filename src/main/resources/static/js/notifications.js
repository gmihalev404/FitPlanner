document.addEventListener('DOMContentLoaded', function () {
    const button = document.getElementById('notifDrop');
    const panel = document.getElementById('notifPanel');
    const wrapper = document.getElementById('notifDropdownWrapper');

    // по default панелът е затворен
    panel.style.display = 'none';

    // toggle панела при клик на камбанката
    button.addEventListener('click', function (e) {
        e.stopPropagation(); // спира клик извън панела
        panel.style.display = panel.style.display === 'block' ? 'none' : 'block';
    });

    // затваряне при клик извън панела
    document.addEventListener('click', function(e){
        if (!wrapper.contains(e.target)) {
            panel.style.display = 'none';
        }
    });

    // Escape -> затваряне
    document.addEventListener('keydown', function(e){
        if (e.key === 'Escape') {
            panel.style.display = 'none';
        }
    });
});