const alert = document.querySelector(".alert_container");

export function showAlert(message = "hello",time = 1250){
  // alert.style.display = "block";
  alert.querySelector("span").innerText = message;
  alert.style.animation = "slide_up 300ms ease-in";
  alert.style.animationFillMode = "forwards";
  setTimeout(()=>{
    alert.style.animation = "slide_down 300ms ease-in";
    alert.style.animationFillMode = "forwards";
  },time);
}
