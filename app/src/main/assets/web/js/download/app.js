(function () {
  window.addEventListener("dragover", function (e) {
    e.preventDefault();
  });

  window.addEventListener("drop", function (e) {
    e.stopPropagation();
    e.preventDefault();
  });

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
        document.querySelector("body").style.pointerEvents = "all";
        main();
      });
    }
  }, 500);
})();

function checkForOpenTab() {
  return new Promise((resolve) => {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/get_tab_status/download");
    xhr.send(null);
    xhr.onload = function () {
      resolve(JSON.parse(xhr.responseText));
    };
  });
}
// import "../common/evs_checker";
import { filterFDR } from "../common/filter";
import { getNewFDR } from "../common/fdr";
import { getFDRList } from "../common/fdr";
import { getFDRText } from "../common/fdr";
import { setFileListTitle } from "../common/select";
import { getSelectedFDR } from "../common/select";
import { getDownloadRequest } from "./downloadrequest";
const requestIdGenerator = require("uuid").v4;
import { DownloadRequest } from "./downloadrequest";
import { setDownloadStatus } from "./downloadrequest";
import { showAlert } from "../common/alert";
import { virtualLink } from "./virtual_link";

function main() {
  const fileSearchInput = document.querySelector(".search_input");
  const titleTextSpan = document.querySelector("div.title span");
  const fileArrayElement = document.querySelector(".chosen_files");
  const STATE_2 = "Pending ...  ⏲️";
  const STATE_3 = "Downloading ...  😀";
  const STATE_4 = "Downloaded  👍";
  requestAvailableFiles();

  function requestAvailableFiles() {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", "/download_file_data");
    xhr.setRequestHeader("Type", "NotNull");
    xhr.send(null);
    xhr.onload = function () {
      const updateRequestArray = JSON.parse(xhr.responseText);
      updateDOM(updateRequestArray);
    };
  }
  const downloadRequestArray = [];
  function updateDOM(updateRequestArray) {
    /* Updates the DOM */
    if (updateRequestArray.length == 0) {
      titleTextSpan.innerText = "No Available Files";
    } else {
      titleTextSpan.innerText = "Available Files";
      for (let i = 0; i < updateRequestArray.length; i++) {
        const fileName = decodeURI(updateRequestArray[i].file_name);
        const fileSize = updateRequestArray[i].file_size;
        const action = updateRequestArray[i].action;
        const uuid = updateRequestArray[i].uuid;
        if (action == "delete_all") {
          // location.reload(true);
          for (const fdr of getFDRList()) {
            fileArrayElement.removeChild(fdr);
          }
          setFileListTitle();
          return;
        }
        if (action == "add") {
          const newFDR = getNewFDR(fileName, fileSize, false, uuid);
          const newDownloadRequest = new DownloadRequest(
            fileName,
            requestIdGenerator(),
            newFDR,
            uuid
          );
          newFDR
            .querySelector(".status_icon img.download")
            .addEventListener("click", function () {
              addDownload(newDownloadRequest);
            });
          newFDR
            .querySelector(".status_icon img.cancel")
            .addEventListener("click", function () {
              removeDownloadRequest(newDownloadRequest);
            });
          fileArrayElement.appendChild(newFDR);
        } else if (action == "remove") {
          removeFDR(fileName);
        }
      }
    }
    setFileListTitle();
  }

  function removeFDR(fileName) {
    for (const fdr of getFDRList()) {
      // console.log(getFDRText(fdr));
      if (getFDRText(fdr) == fileName) {
        fdr.style.display = "none";
        fdr.classList.remove("selected");
        fdr.classList.add("deleted");
        fileArrayElement.removeChild(fdr);
        // console.log("Found the file >> " + fileName);
        return;
      }
    }
    // console.error("Did not find the file >> " + fileName);
  }

  fileSearchInput.addEventListener("input", function () {
    filterFDR(this.value, getFDRList());
  });

  /* Event Source */
  const path = `${location.origin}/events/download`;
  const eventSource = new EventSource(path);

  eventSource.onopen = function () {
    // console.log("Download EventSource Open");
  };

  eventSource.onerror = function (e) {
    // console.error(e);
  };

  eventSource.addEventListener("update_list", function (e) {
    const updateRequestArray = JSON.parse(e.data);
    // console.log(updateRequestArray);
    updateDOM(updateRequestArray);
  });

  eventSource.addEventListener("close_connection", function () {
    eventSource.close();
    // console.log("Received close-connection event >> closed the connection");
  });

  eventSource.addEventListener("ping", () => {});

  let isDownloading = false;
  let downloadedFilesCount = 0;

  document
    .querySelector(".download_selected img")
    .addEventListener("click", function () {
      const selectedFDR = getSelectedFDR();
      if (selectedFDR.length == 0) {
        showAlert("No files selected");
        return;
      }
      for (const fdr of selectedFDR) {
        const requestedFilename = getFDRText(fdr);
        const requestId = requestIdGenerator();
        const uuid = fdr.querySelector(".uuid").innerText;
        const dRequest = new DownloadRequest(
          requestedFilename,
          requestId,
          fdr,
          uuid
        );
        setDownloadStatus(fdr, STATE_2);
        downloadRequestArray.push(dRequest);
        fdr.classList.remove("selected");
      }
      if (!isDownloading) {
        if (downloadRequestArray.length != downloadedFilesCount) {
          downloadFile(downloadRequestArray[downloadedFilesCount]);
          isDownloading = true;
        }
      }
    });

  function removeDownloadRequest(downloadRequest) {
    downloadRequest.toDownload = false;
    setDownloadStatus(downloadRequest.fdr, "Not Downloaded");
  }

  function downloadFile(download_request) {
    const requestedFileName =
      download_request.requested_file_name; /* Get the actual filename */
    const requestId = download_request.file_request_id;
    const requestFDR = download_request.fdr;
    const fileId = download_request.uuid;

    let downloadRequestPath = `/request_file_upload/${fileId}/${requestId}`;
    // console.log("Request url >> " + downloadRequestPath);
    virtualLink.setAttribute("href", downloadRequestPath);
    virtualLink.click();
    downloadedFilesCount++;
    requestFDR.querySelector(".status_text span").innerText = STATE_3;
    // download_request.status = STATE_3;
  }

  function addDownload(download_request) {
    downloadRequestArray.push(download_request);
    if (!isDownloading) {
      downloadFile(download_request);
      isDownloading = true;
    } else {
      setDownloadStatus(download_request.fdr, STATE_2);
    }
  }

  eventSource.addEventListener("download_completed", function (e) {
    const responseJSON = JSON.parse(e.data);
    // console.log("Download Completed");
    // console.log(responseJSON);
    const completedDownloadFileName = responseJSON.comp_file_name;
    const completedDownloadRequestId = responseJSON.comp_request_id;
    const completedDownloadFileId = responseJSON.uuid;
    const request = getDownloadRequest(
      completedDownloadFileName,
      completedDownloadRequestId,
      downloadRequestArray,
      completedDownloadFileId
    );
    const message = `Done downloading : ${completedDownloadFileName}`;
    showAlert(message, 2500);
    setDownloadStatus(request.fdr, STATE_4);
    // request.status = STATE_4;
    isDownloading = false;
    if (!isDownloading) {
      if (downloadRequestArray.length != downloadedFilesCount) {
        downloadFile(downloadRequestArray[downloadedFilesCount]);
        isDownloading = true;
      } else if (downloadRequestArray.length == downloadedFilesCount) {
        // console.log(`Finished download of ${downloadedFilesCount} files`);
      }
    }
  });
}
