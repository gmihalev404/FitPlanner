document.addEventListener('click', function (event) {
    const button = document.getElementById('notifDrop');
    const panel = document.getElementById('notifPanel');
    const wrapper = document.getElementById('notifDropdownWrapper');

    if (!button || !panel || !wrapper) return;

    if (button.contains(event.target)) {
        event.preventDefault();
        event.stopPropagation();

        // Use a toggle class instead of direct style for better mobile reliability
        panel.classList.toggle('show-notifs');

        // Force display block if the class is present
        if (panel.classList.contains('show-notifs')) {
            panel.style.display = 'block';
            console.log("Mobile/Desktop: Open");
        } else {
            panel.style.display = 'none';
        }
    }
    else if (!wrapper.contains(event.target)) {
        panel.style.display = 'none';
        panel.classList.remove('show-notifs');
    }
});