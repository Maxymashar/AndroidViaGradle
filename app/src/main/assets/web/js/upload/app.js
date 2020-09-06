(function () {
  const fileListTitle = document.querySelector(".files_section .title span");
  setTimeout(() => {
    if (typeof EventSource == "undefined") {
      fileListTitle.innerText = "Sorry : Your browser is not supported";
      document.querySelector("body").style.pointerEvents = "none";
      return;
    } else {
      checkForOpenTab().then((isOpen) => {
        // console.log(isOpen);
        if (isOpen.is_open) {
          fileListTitle.innerText = "Error : Another tab is open";
          document.querySelector("body").style.pointerEvents = "none";
          return;
        }
        fileListTitle.innerText = "No File(s) Available";
        document.querySelector("body").style.pointerEvents = "all";
        main();
      });
    }
  }, 500);
})();

function checkForOpenTab() {
  return new Promise((resolve) => {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/get_tab_status/upload");
    xhr.send(null);
    xhr.onload = function () {
      resolve(JSON.parse(xhr.responseText));
    };
  });
}
// import "../common/evs_checker";
import { checkSelected } from "../common/select";
import { getSelectedFDR } from "../common/select";
import { uploadFile } from "./upload";
import { filterFDR } from "../common/filter";
import { sendUpdatedFiles } from "./update";
import { getFDRList } from "../common/fdr";
import { getFDRText } from "../common/fdr";
import { UpdateRequest } from "./update";
import { setFileListTitle } from "../common/select";
import { showAlert } from "../common/alert";
import "./fileadd";
const chosenFiles = require("./fileadd").chosenFiles;

function main() {
  const fileArrayElement = document.querySelector(".chosen_files");
  const fileSearchInput = document.querySelector(".search_input");

  const eventSource = new EventSource(`${location.origin}/events/upload`);
  eventSource.onopen = function () {
    // console.log("EventSource is Open");
  };
  eventSource.onerror = function (e) {
    // console.error(e);
  };

  eventSource.addEventListener("upload_file", function (e) {
    // console.log("UploadRequest Received");
    const decodedRequest = decodeURI(e.data);
    const uploadRequest = JSON.parse(decodedRequest);
    const requestId = uploadRequest.request_id;
    const uuid = uploadRequest.uuid;
    // console.log(uploadRequest);
    uploadFile(
      chosenFiles, // the file array
      requestId, // The request id
      uuid
    );
  });

  eventSource.addEventListener("close_connection", function () {
    eventSource.close();
    // console.log("Received close-connection event >> closed the connection");
  });

  eventSource.addEventListener("ping", () => {
    // console.log("PING");
  });

  eventSource.addEventListener("reload", () => {});

  const deleteSelected = document.querySelector(".delete_selected");
  deleteSelected.addEventListener("click", function () {
    const updateRequestArray = [];
    const selectedFDRList = getSelectedFDR();
    if (selectedFDRList.length == 0) {
      showAlert("No files selected");
      return;
    }
    for (const selectedFDR of selectedFDRList) {
      selectedFDR.style.display = "none";
      selectedFDR.classList.remove("selected");
      selectedFDR.classList.add("deleted");
      fileArrayElement.removeChild(selectedFDR);

      const fileName = selectedFDR
        .querySelector("div.fdr_file_name span")
        .innerText.trim();
      const fileSize = selectedFDR
        .querySelector("div.fdr_file_size span")
        .innerText.trim();
      updateRequestArray.push(new UpdateRequest(fileName, fileSize, "remove"));
    }
    checkSelected();
    resetSearch();
    removeDeletedFiles();
    sendUpdatedFiles(updateRequestArray);
  });

  function resetSearch() {
    setFileListTitle();
    fileSearchInput.value = "";
    filterFDR("", getFDRList());
  }

  /* Remove the files from the chosen files Array */
  function removeDeletedFiles() {
    let found = false;
    for (const fl of chosenFiles) {
      if (found) {
        /* Incase there are many files with the same filename only remove the first one in the list */
        break;
      }
      const file_name = fl.fileName;
      for (const fdr of getFDRList()) {
        if (
          fdr.classList.contains("deleted") &&
          getFDRText(fdr) == file_name.trim()
        ) {
          fl.deleted = true;
          found = true;
        }
      }
    }
  }

  fileSearchInput.addEventListener("input", function () {
    filterFDR(this.value, getFDRList());
  });

  function getUpdateFileList() {
    const fdrArray = [];
    for (const fdr of getFDRList()) {
      if (fdr.classList.contains("deleted")) {
        continue;
      }
      fdrArray.push(fdr);
    }
    return fdrArray;
  }
}
