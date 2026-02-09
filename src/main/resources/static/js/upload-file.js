/**
 * Универсален модул за обработка на всякакви файлове (Изображения и Видео)
 */
const FileUploader = {

    // Помощна функция за определяне на типа
    getFileType: function(file) {
        if (file.type.startsWith('image/')) return 'image';
        if (file.type.startsWith('video/')) return 'video';
        return 'other';
    },

    // 1. Универсален локален преглед
    previewLocal: function(input, previewContainerSelector) {
        const file = input.files[0];
        if (!file) return;

        const container = input.closest('.row, .container, body').querySelector(previewContainerSelector);
        if (!container) return;

        const type = this.getFileType(file);
        const reader = new FileReader();

        reader.onload = function(e) {
            // Намираме или създаваме правилния елемент според типа
            let previewEl;

            if (type === 'image') {
                previewEl = container.querySelector('img') || document.createElement('img');
                previewEl.src = e.target.result;
                previewEl.style.display = 'block';
                if (container.querySelector('video')) container.querySelector('video').style.display = 'none';
            } else if (type === 'video') {
                previewEl = container.querySelector('video') || document.createElement('video');
                previewEl.src = e.target.result;
                previewEl.controls = true;
                previewEl.style.display = 'block';
                if (container.querySelector('img')) container.querySelector('img').style.display = 'none';
            }

            // Скриваме иконата/текста (placeholder)
            const placeholder = container.querySelector('.placeholder, i, span');
            if (placeholder) placeholder.style.display = 'none';
        };

        reader.readAsDataURL(file);
    },

    // 2. Универсален преглед + AJAX качване
    previewAndUpload: function(input, config) {
        const file = input.files[0];
        if (!file) return;

        // Първо: Локален преглед
        this.previewLocal(input, config.containerSelector);

        // Второ: AJAX качване
        const formData = new FormData();
        formData.append(config.paramName || 'file', file);

        fetch(config.uploadUrl, {
            method: 'POST',
            body: formData
        })
            .then(response => response.ok ? response.text() : Promise.reject('Upload failed'))
            .then(url => {
                if (url !== "error") {
                    console.log("File saved to session:", url);
                    if (config.hiddenInputId) {
                        const hidden = document.getElementById(config.hiddenInputId);
                        if (hidden) hidden.value = url;
                    }
                }
            })
            .catch(err => console.error("Session upload failed:", err));
    }
};