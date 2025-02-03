(function(){
    // span 요소 노드 가져오기
    const spanEl = document.querySelector("main h2 span");
    // 화면에 표시할 문장 배열
    const txtArr = ['같이 게임할 시간을 정해요.','좋아하는 게임을 공유해요.'];
    // 배열의 인덱스 초깃값
    let index = 0;
    // 화면에 표시할 문장 배열에서 요소를 하나 가져온 뒤, 배열로 만들기
    let currentTxt = txtArr[index].split("");

    function writeTxt(){
        spanEl.textContent += currentTxt.shift();
        if(currentTxt.length !== 0){
            setTimeout(writeTxt, Math.floor(Math.random()*100));
        }else{
            currentTxt = spanEl.textContent.split("");
            setTimeout(deleteTxt,3000);
        }
    }
    writeTxt();

    function deleteTxt(){
        currentTxt.pop();
        spanEl.textContent = currentTxt.join("");
        if(currentTxt.length !== 0){
            setTimeout(deleteTxt, Math.floor(Math.random()*100));
        }else{
            index = (index + 1) % txtArr.length;
            currentTxt = txtArr[index].split("");
            writeTxt();
        }
    }
})();

const headerEl = document.querySelector("header");
window.addEventListener('scroll', function(){
    const broswerScrollY = this.window.pageYOffset;
    if(broswerScrollY>0){
        headerEl.classList.add("active");
    }else{
        headerEl.classList.remove("active");
    }
});



const animationMove = function(selector){
    const targetEl = document.querySelector(selector);
    const broswerScrollY = window.scrollY;
    const targetScrollY = targetEl.getBoundingClientRect().top + broswerScrollY;
    window.scrollTo({ top: targetScrollY, behavior: 'smooth' });
};

const scollMoveEl = document.querySelectorAll("[data-animation-scroll='true']");
for(let i = 0; i < scollMoveEl.length; i++){
    scollMoveEl[i].addEventListener('click', function(e){
        const target = this.dataset.target;
        animationMove(target);
    });
}