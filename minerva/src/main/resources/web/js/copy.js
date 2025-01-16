function copy(copyBtn, text) {
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
}
