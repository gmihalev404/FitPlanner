const FileUploader = {
    getFileType: function(file) {
        if (file.type.startsWith('image/')) return 'image';
        if (file.type.startsWith('video/')) return 'video';
        return 'other';
    },

    previewLocal: function(input, previewContainerSelector) {
        const file = input.files[0];
        if (!file) return;

        let container = document.querySelector(previewContainerSelector);
        if (!container) {
            container = input.closest('.row, .container, body').querySelector(previewContainerSelector);
        }
        if (!container) return;

        const type = this.getFileType(file);
        const reader = new FileReader();

        reader.onload = function(e) {
            let previewEl;
            if (type === 'image') {
                previewEl = container.querySelector('img') || document.createElement('img');
                previewEl.src = e.target.result;

                // КЛЮЧОВА КОРЕКЦИЯ: Премахваме класа .hidden, ако съществува
                previewEl.classList.remove('hidden');
                previewEl.style.display = 'block';

                if (container.querySelector('video')) container.querySelector('video').style.display = 'none';
            } else if (type === 'video') {
                previewEl = container.querySelector('video') || document.createElement('video');
                previewEl.src = e.target.result;
                previewEl.controls = true;
                previewEl.classList.remove('hidden');
                previewEl.style.display = 'block';
                if (container.querySelector('img')) container.querySelector('img').style.display = 'none';
            }

            const placeholder = container.querySelector('.placeholder, i, span');
            if (placeholder) {
                placeholder.style.setProperty('display', 'none', 'important');
            }
        };
        reader.readAsDataURL(file);
    },

    previewAndUpload: function(input, config) {
        const file = input.files[0];
        if (!file) return;

        // Валидация за размер (5MB)
        if (file.size > 5 * 1024 * 1024) {
            alert("File is too large! Max size is 2MB.");
            input.value = "";
            return;
        }

        this.previewLocal(input, config.containerSelector);

        const formData = new FormData();
        formData.append(config.paramName || 'file', file);

        fetch(config.uploadUrl, {
            method: 'POST',
            body: formData
        })
            .then(response => response.ok ? response.text() : Promise.reject('Upload failed'))
            .then(url => {
                if (url !== "error" && config.hiddenInputId) {
                    const hidden = document.getElementById(config.hiddenInputId);
                    if (hidden) hidden.value = url;
                }
            })
            .catch(err => console.error("Session upload failed:", err));
    }
};