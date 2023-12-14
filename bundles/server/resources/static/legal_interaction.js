function hideLegal(legalID) {
    let element = document.getElementById(legalID);
    element.classList.add("hidden-legal");
}

function showLegal(legalID) {
    let element = document.getElementById(legalID);
    element.classList.remove("hidden-legal");
}