function copy(copyBtn, text) {
    let temp = $('<input>')
    $('body').append(temp)
    temp.val(text).select()
    document.execCommand('copy')
    temp.remove()
    copyBtn.style.color = '#0a0'
    copyBtn.style.transform = 'scale(1.2) rotate(10deg)'
    setTimeout(() => { copyBtn.style.color = ''; copyBtn.style.transform = ''; }, 500)
}
