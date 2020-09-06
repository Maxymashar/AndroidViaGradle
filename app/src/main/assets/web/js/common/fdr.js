import { setSelection } from "../common/select";
// import { addDownload } from "../download/app";
const fileArrayElement = document.querySelector(".chosen_files");
let selectionStartFdr = null;
let selectionEndFDR = null;
let startIndex = 0;
let endIndex = 0;
let count = 0;
let foundFirst = false;

export function getNewFDR(file_name, file_size, is_upload,uuid) {
  /* Replace all the spaces with the &nbsp; entity */
  const newFDR = document.createElement("li");
  const newFDR_FileName = document.createElement("div");
  const newFDR_FileSize = document.createElement("div");
  const newSpan_FileName = document.createElement("span");
  const newSpan_FileSize = document.createElement("span");
  const newUUIDSpan = document.createElement("span");

  newUUIDSpan.style.display = "none";
  newUUIDSpan.innerText = uuid;
  newUUIDSpan.setAttribute("class","uuid");
  newFDR.setAttribute("class", "file_data_row");
  newFDR_FileName.setAttribute("class", "fdr_file_name");
  // newFDR_FileName.setAttribute("title", file_name);
  newFDR_FileSize.setAttribute("class", "fdr_file_size");
  // newFDR_FileSize.setAttribute("title", file_size);
  newFDR_FileName.appendChild(newSpan_FileName);
  newFDR_FileSize.appendChild(newSpan_FileSize);
  /* Add the text */
  newSpan_FileName.innerText = file_name;
  newSpan_FileName.setAttribute("title", file_name);
  newSpan_FileSize.innerText = file_size;

  newFDR.appendChild(newFDR_FileName);
  newFDR.appendChild(newFDR_FileSize);
  newFDR.appendChild(newUUIDSpan);

  newSpan_FileName.addEventListener("click", function (e) {
    selectionStartFdr = selectionEndFDR;
    selectionEndFDR = newFDR;

    // console.log(selectionStartFdr);
    // console.log(selectionEndFDR);

    if (e.shiftKey) {
      if (selectionStartFdr != null) {
        const indexes = getBetweenFDRListIndex(
          selectionStartFdr,
          selectionEndFDR
        );
        for (let i = indexes[0]; i <= indexes[1]; i++) {
          const toSelectFDR = getFDRList()[i];
          removeSelection(toSelectFDR);
          setSelection(toSelectFDR);
        }
        selectionStartFdr = null;
        selectionEndFDR = null;
        startIndex = 0;
        endIndex = 0;
        count = 0;
        foundFirst = false;
        return;
      }
    }
    setSelection(newFDR);
  });

  if (is_upload) {
    return newFDR;
  }

  /* Add more rows is its download fdr */
  const statusDiv = document.createElement("div");
  const innerTextDiv = document.createElement("div");
  const innerIconDiv = document.createElement("div");

  statusDiv.setAttribute("class", "status_container");
  innerTextDiv.setAttribute("class", "status_text");
  innerIconDiv.setAttribute("class", "status_icon");
  innerIconDiv.setAttribute("title", "Download");

  const innerDownloadImg = document.createElement("img");
  innerDownloadImg.setAttribute(
    "src",
    "../../assets/via-download-btn-no-outline.svg"
  );
  innerDownloadImg.classList.add("download");

  const innerCancelImg = document.createElement("img");
  innerCancelImg.setAttribute("src", "../../assets/via-cancel-icon-no.svg");
  innerCancelImg.classList.add("cancel");
  innerCancelImg.classList.add("disabled");
  // innerDownloadImg.addEventListener("click", () => {
  //   // addDownload(file_name);
  // });

  const textSpan = document.createElement("span");
  textSpan.innerText = "Not Downloaded";

  innerTextDiv.appendChild(textSpan);
  innerIconDiv.appendChild(innerDownloadImg);
  innerIconDiv.appendChild(innerCancelImg);
  statusDiv.appendChild(innerTextDiv);
  statusDiv.appendChild(innerIconDiv);

  newFDR.appendChild(statusDiv);
  return newFDR;
}

export function getFDRList() {
  return fileArrayElement.querySelectorAll("li");
}

export function getFDRText(fdr) {
  return fdr.querySelector("div span").innerText.trim();
}

function getBetweenFDRListIndex(fdr_start, fdr_end) {
  for (const fdr of getFDRList()) {
    if (fdr == fdr_start || fdr == fdr_end) {
      if (!foundFirst) {
        startIndex = count;
        foundFirst = true;
      } else {
        endIndex = count;
      }
    }
    count++;
  }
  count = 0;
  return [startIndex, endIndex];
}

function removeSelection(fdr) {
  fdr.classList.remove("selected");
}

// function replaceAllSpacesWithEntitySymbol(string){
//   const spaceEntity = "&nbsp;";
//   for(let i = 0;i < string.length; i++){
//     if(string[i] == " "){
//       string = setCharAt(string,i,spaceEntity);
//     }
//   }
// }
// function setCharAt(string, index, char) {
//   let firstSubStr = string.substr(0, index);
//   let secondSubStr = string.substr(index + 1, string.length);
//   return firstSubStr + char + secondSubStr;
// }
