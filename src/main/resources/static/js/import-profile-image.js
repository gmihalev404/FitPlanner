function previewImage(input) {
    if (input.files && input.files[0]) {
        const file = input.files[0];

        // Only allow image files
        if (!file.type.startsWith('image/')) {
            alert('Please select a valid image file.');
            input.value = ''; // Clear the input
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('profileDisplay').src = e.target.result;
        }
        reader.readAsDataURL(file);
    }
}