// collapse/expand tree items
function treeclick(id) {
    let c = document.getElementById(id);
    let i = document.getElementById('i' + id);
    if (c.style.display == 'none') {
        c.style.display = 'block';
        i.classList.remove('fa-caret-right');
        i.classList.add('fa-caret-down');
        i.style.paddingRight = '0';
    } else {
        c.style.display = 'none';
        i.classList.remove('fa-caret-down');
        i.classList.add('fa-caret-right');
        i.style.paddingRight = '4px';
    }
}
