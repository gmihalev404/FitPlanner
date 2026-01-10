function previewImage(input) {
    if (input.files && input.files[0]) {
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('profileDisplay').src = e.target.result;
        }
        reader.readAsDataURL(input.files[0]);
    }
}