const fileArrayElement = document.querySelector(".chosen_files");

function downloadFile(file_name) {
  // console.log("Sent file upload file upload >> " + file_name);
  virtualLink.setAttribute("href", `/request_file_upload/${file_name}`);
  virtualLink.click();
  downloadedFiles++;

  for (const fdr of getFDRList()) {
    if (fdr.querySelector("div span").innerText == file_name) {
      fdr.querySelector(".status_text span").innerText = STATE_4;
      break;
    }
  }
}

function getFDRList() {
  return fileArrayElement.querySelectorAll("li");
}