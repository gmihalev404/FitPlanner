document.addEventListener('DOMContentLoaded', () => {
    const genderSelect = document.querySelector('.gender-select');
    const genderButton = genderSelect.querySelector('.gender-button');
    const genderLabels = genderSelect.querySelectorAll('.gender-options label');

    const experienceSelect = document.querySelector('.experience-select');
    const experienceButton = experienceSelect.querySelector('.experience-button');
    const experienceLabels = experienceSelect.querySelectorAll('.experience-options label');

    function toggleOpen(container, button, open) {
        if (open) {
            container.classList.add('open');
            button.setAttribute('aria-expanded', 'true');
        } else {
            container.classList.remove('open');
            button.setAttribute('aria-expanded', 'false');
        }
    }

    document.addEventListener('click', (e) => {
        if (!genderSelect.contains(e.target)) toggleOpen(genderSelect, genderButton, false);
        if (!experienceSelect.contains(e.target)) toggleOpen(experienceSelect, experienceButton, false);
    });

    genderButton.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleOpen(genderSelect, genderButton, !genderSelect.classList.contains('open'));
    });

    experienceButton.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleOpen(experienceSelect, experienceButton, !experienceSelect.classList.contains('open'));
    });

    const handleSelection = (labels, button, container) => {
        labels.forEach(label => {
            label.addEventListener('click', (e) => {
                const inputId = label.htmlFor;
                const radio = document.getElementById(inputId);
                if (radio) radio.checked = true;
                button.textContent = label.textContent;
                toggleOpen(container, button, false);
            });
        });
    };

    handleSelection(genderLabels, genderButton, genderSelect);
    handleSelection(experienceLabels, experienceButton, experienceSelect);

    const initButtonText = (labels, button) => {
        labels.forEach(label => {
            const input = document.getElementById(label.htmlFor);
            if (input && input.checked) button.textContent = label.textContent;
        });
    };

    initButtonText(genderLabels, genderButton);
    initButtonText(experienceLabels, experienceButton);
});