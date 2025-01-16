document.querySelectorAll('.copy').forEach(copyBtn => {
    copyBtn.addEventListener('click', () => {
        const targetElement = document.querySelector(copyBtn.dataset.copy)
        const text = targetElement.textContent.replace(/\s+/g, ' ')
        let temp = $('<input>')
        $('body').append(temp)
        try {
            temp.val(text).select()
            document.execCommand('copy')
        } finally {
            temp.remove()
        }
        copyBtn.style.color = '#0a0'
        copyBtn.style.transform = 'scale(1.2) rotate(10deg)'
        setTimeout(() => { copyBtn.style.color = ''; copyBtn.style.transform = ''; }, 500)
    })
})

$('#tocModal').on('shown.bs.modal', function() {
    $('#tocLevels').focus();
})
