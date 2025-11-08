const genderSelect = document.querySelector('.gender-select');
const genderButton = document.querySelector('.gender-button');
const genderOptions = document.querySelectorAll('.gender-options label');

genderButton.addEventListener('click', (e) => {
    e.stopPropagation();
    genderSelect.classList.toggle('open');
});

document.addEventListener('click', () => {
    genderSelect.classList.remove('open');
});

genderOptions.forEach(label => {
    label.addEventListener('click', () => {
        genderButton.textContent = label.textContent;
        genderSelect.classList.remove('open');
    });
});