const queue = document.getElementById('queue');
const review = document.getElementById('review');

const question_queue = document.getElementById('question-queue');
const question_review = document.getElementById('question-review');

const prefix = document.getElementById('prefix');
const managerreview = document.getElementById('managerreview');
const reviewchannel = document.getElementById('reviewchannel');
const embedcolor = document.getElementById('embedcolor');

const permissionrole = document.getElementById('permissionrole');
const managerrole = document.getElementById('managerrole');
const updateconfig = document.getElementById('updateconfig');


const list_title_text = document.getElementById('list-title-text');

function deleteQOTD(index) {

}

queue.addEventListener("click", function () {
    question_queue.style.display = "block";
    question_review.style.display = "none";

    list_title_text.innerHTML = "Queue:";
});

review.addEventListener("click", function () {
    question_queue.style.display = "none";
    question_review.style.display = "block";

    list_title_text.innerHTML = "Review:";
});

function getConfig() {
    httpGetAsync("/api/v1/getconfig", (res) => {

    });
}


function setConfig() {
    httpGetAsync(`/api/v1/setconfig?prefix=${prefix.value}&managerreview=${managerreview.value}&reviewchannel=${reviewchannel.value}&embedcolor=${embedcolor.value}&permissionrole=${permissionrole.value}&managerrole=${managerrole.value}`, (res) => {

    });
}

function httpGetAsync(url, callback) {
    console.log(url);

    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
            callback(xmlHttp.responseText);
        }
    }
    xmlHttp.open("GET", url, true);
    xmlHttp.send(null);
}

queue.click();