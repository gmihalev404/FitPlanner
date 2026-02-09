// static/js/program-sync.js

const ProgramSyncer = {
    // Основна функция за синхронизация
    async sync() {
        const nameField = document.getElementById('prog-name');
        const monthsField = document.getElementById('prog-months');
        const notifyField = document.getElementById('notify');
        const publicField = document.getElementById('public');

        // Проверка дали елементите съществуват на текущата страница
        if (!nameField || !monthsField) return;

        const formData = new FormData();
        formData.append('name', nameField.value);
        formData.append('scheduleMonths', monthsField.value);
        formData.append('notifications', notifyField.checked);
        formData.append('isPublic', publicField.checked);

        try {
            const response = await fetch('/update-program-session', {
                method: 'POST',
                body: formData
            });
            if (response.ok) {
                console.log("Session auto-saved successfully");
            }
        } catch (err) {
            console.error("Auto-save sync failed:", err);
        }
    },

    // Инициализация на слушателите
    init() {
        const fields = ['prog-name', 'prog-months', 'notify', 'public'];

        fields.forEach(id => {
            const el = document.getElementById(id);
            if (el) {
                // За текстови полета ползваме 'blur', за другите 'change'
                const eventType = el.type === 'text' ? 'blur' : 'change';
                el.addEventListener(eventType, () => this.sync());
            }
        });

        // Синхронизация при кликване на бутони, които напускат страницата
        document.querySelectorAll('.sync-required').forEach(form => {
            form.addEventListener('submit', async (e) => {
                await this.sync();
            });
        });
    }
};

// Стартираме при зареждане на DOM
document.addEventListener('DOMContentLoaded', () => {
    ProgramSyncer.init();
});