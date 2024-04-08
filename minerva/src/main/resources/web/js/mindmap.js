"use strict";
// from https://dev.to/frankwisniewski/create-a-mindmap-22pb

// my helpers...
const mmm = function(t = null) {
  return new class {
    constructor(t) {
      t && (this.nodes = t.nodeType ? [t] : NodeList.prototype.isPrototypeOf(t) ? t : document.querySelectorAll(t), this.n = this.nodes[0])
    }
    each = t => (this.nodes.forEach((s => t(s))), this);
    addClass = t => this.each((s => s.classList.add(...t.split(",").map((t => t.trim())))), this);
    html = t => this.each(s => s.innerHTML = t)
    create = t => mmm(document.createElement(t));
    append = t => {
      let s = this.create(t);
      return this.n.appendChild(s.n), s
    };
    remove = () => this.each(_node => _node.remove())
    insertAfter = t => (t.n.after(this.n), this);
    setAttr = (t, s = "") => this.each((e => e.setAttribute(t, s)), this);
    sCreate = t => {
      let s = document.createElementNS("http://www.w3.org/2000/svg", "svg");
      return s.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink"), s.setAttribute("viewBox", t), mmm(s)
    };
    sAppend = t => (this.n.appendChild(t.n), this);
    sC = t => document.createElementNS("http://www.w3.org/2000/svg", t);
    id = t => (this.setAttr("id", t), this);
    d = t => (this.setAttr("d", t), this);
    fill = t => (this.setAttr("fill", t), this);
    stroke = t => (this.setAttr("stroke", t), this);
    strokeWidth = t => (this.setAttr("stroke-width", t), this);
    sBezier = (t, s, e, i) => mmm(this.sC("path")).d(`M${t},${s} C ${(t+e)/2},${s} ${(t+e)/2},${i} ${e},${i}`);
  }(t)
};
// by resize repaint SVG
const resizeObserver = new ResizeObserver(entries => {
  mmm('#mySVG').remove()
  drawLines(data)
})

function createMap(data) {
  //find depth of Elements
  function findDepth(id, depth = 0) {
    while (id !== null) {
      let aktEl = data.filter(x => x.id == id)
      depth++
      id = aktEl[0].parentId
      if (id == null) depth = depth * aktEl[0].div
    }
    return depth
  }
  // first div at right side (-1*-1=1)
  var aktDiv = 1
  // swap between left and right side
  data.filter(x => x.parentId == null)
    .forEach(x => {
      x.div = aktDiv;
      aktDiv *= -1
    })
  // set root Elements div = 0
  data.find(x => x.id == null).div = 0
  // set divs for Childs
  data.filter(x => (x.parentId !== null && x.id !== null))
    .forEach(x => x.div = findDepth(x.id))
  // find min and max Div-Number
  let minDiv = Math.min(...data.map(item => item.div))
  let maxDiv = Math.max(...data.map(item => item.div))
  // create Divs in container
  for (let i = minDiv; i <= maxDiv; i++) {
    mmm('#mindmapc').append('div').id(`div${i}`).addClass('f-col')
  }
  // put paragraphs in Div´s
  data.forEach(el => {
    var kk = el.text
    if (el.id != null) {
        kk = '<a href="' + mindMapBaseUrl + el.id + '">' + kk + '</a>'
    }
    var k = mmm(`#div${el.div}`).append('p').html(kk).id(`p${el.id}`)
    if (el.type) {
        k.addClass(el.type)
    }
  })
}

function drawLines(data) {
  // find Coordinates from Container
  let mC = mmm('#mindmapc').n.getBoundingClientRect();
  // create SVG with same ViewportSize as Container
  mmm().sCreate(`0,0,${mC.width},${mC.height}`).setAttr('id', 'mySVG').insertAfter(mmm('#mindmaproot'))
  // set SVG at same Position as Container
  mmm('#mySVG').n.style = `top:${mC.top}px;left:${mC.left}px;width:${mC.width}px;height:${mC.height}px;`
  // create Bezier from Element's to Parent´s
  data.forEach(el => {
    if ('parentId' in el) {
      let aktEl = mmm(`#p${el.id}`).n.getBoundingClientRect()
      let parEl = mmm(`#p${el.parentId}`).n.getBoundingClientRect()
      let aktLeft = (aktEl.left < parEl.left) ? aktEl.left + aktEl.width : aktEl.left
      let parLeft = (aktEl.left > parEl.left) ? parEl.left + parEl.width : parEl.left
      mmm('#mySVG').sAppend(
        mmm().sBezier(aktLeft - mC.left, aktEl.top - mC.top + (aktEl.height / 2), parLeft - mC.left, parEl.top - mC.top + (parEl.height / 2)).addClass('path'))
    }
  })
}
