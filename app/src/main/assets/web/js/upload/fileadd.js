import { File } from "../common/file";
import { getNewFDR as getFDR } from "../common/fdr";
import { showAlert } from "../common/alert";
import { setFileListTitle } from "../common/select";
import { sendUpdatedFiles } from "./update";
import { UpdateRequest } from "./update";
const idGenerator = require("uuid").v4;

const fileAddButton = document.querySelector(".file_add_btn");
const fileDropZone = document.querySelector(".file_add_container");
const fileArrayElement = document.querySelector(".chosen_files");

const hiddenFileInput = document.createElement("input");
hiddenFileInput.setAttribute("type", "file");
hiddenFileInput.setAttribute("multiple", null);
fileAddButton.addEventListener("click", function () {
  hiddenFileInput.click();
  hiddenFileInput.onchange = function () {
    addFiles(hiddenFileInput.files);
  };
});

window.addEventListener("dragover", function (e) {
  e.preventDefault();
});

window.addEventListener("drop", function (e) {
  e.stopPropagation();
  e.preventDefault();
  // addFilesFromEvent(e);
});

fileDropZone.ondragover = function (e) {
  e.preventDefault();
};
fileDropZone.ondrop = function (e) {
  e.stopPropagation();
  e.preventDefault();
  addFilesFromEvent(e);
};
function addFilesFromEvent(e) {
  const items = e.dataTransfer.items;
  const entries = [];
  for (const item of items) {
    if (item.kind != "file") {
      continue;
    }
    const entry = item.webkitGetAsEntry();
    entries.push(entry);
  }
  const files = getDroppedFiles(entries);
  checkArray(files);

  function checkArray(array) {
    setTimeout(() => {
      if (array.length == 0) {
        checkArray(array);
      } else {
        addFiles(array);
      }
    }, 1000);
  }
}
function addFiles(fileList) {
  const updateRequestArray = [];
  for (const file of fileList) {
    const fileName = file.name;
    const fileSize = getFileSize(file.size);
    const id = idGenerator();
    chosenFiles.push(new File(file,id));
    const newFDR = getFDR(fileName, fileSize, true,id);
    fileArrayElement.appendChild(newFDR);
    updateRequestArray.push(new UpdateRequest(fileName, fileSize, "add",id));
  }
  showAlert(`Added ${fileList.length} file(s)`);

  function getFileSize(file_size) {
    const KB_SIZE = Math.pow(1024, 1);
    const MB_SIZE = Math.pow(1024, 2);
    const GB_SIZE = Math.pow(1024, 3);
    const TB_SIZE = Math.pow(1024, 4);
    const ZB_SIZE = Math.pow(1024, 5);
    let strFileSize = "";

    const numFileSize = parseFloat(file_size);
    if (numFileSize <= KB_SIZE) {
      strFileSize = `${numFileSize} B`;
    } else if (numFileSize <= MB_SIZE && numFileSize > KB_SIZE) {
      strFileSize = `${(numFileSize / KB_SIZE).toFixed(2)} KB`;
    } else if (numFileSize <= GB_SIZE && numFileSize > MB_SIZE) {
      strFileSize = `${(numFileSize / MB_SIZE).toFixed(2)} MB`;
    } else if (numFileSize <= TB_SIZE && numFileSize > GB_SIZE) {
      strFileSize = `${(numFileSize / GB_SIZE).toFixed(2)} GB`;
    } else if (numFileSize <= ZB_SIZE && numFileSize > TB_SIZE) {
      strFileSize = `${(numFileSize / TB_SIZE).toFixed(2)} TB`;
    }

    return strFileSize;
  }
  /* Set the title */
  setFileListTitle();
  sendUpdatedFiles(updateRequestArray);
}

export const chosenFiles = [];

function getDroppedFiles(droppedEntries) {
  const droppedFiles = [];
  for (const droppedEntry of droppedEntries) {
    if (droppedEntry.isFile) {
      droppedEntry.file((file) => {
        droppedFiles.push(file);
      });
    } else {
      readDroppedFiles(droppedEntry);
    }
  }
  return droppedFiles;
  function readDroppedFiles(entry) {
    readAllDirectoryEntries(entry).then((allEntries) => {
      allEntries.forEach((innerEntry) => {
        if (innerEntry.isFile) {
          innerEntry.file((file) => {
            droppedFiles.push(file);
          });
        } else if (innerEntry.isDirectory) {
          readDroppedFiles(innerEntry);
        }
      });
    });
  }
  function readAllDirectoryEntries(directoryEntry) {
    return new Promise((resolve) => {
      const directoryEntries = [];
      const directoryReader = directoryEntry.createReader();
      readEntries();
      function readEntries() {
        directoryReader.readEntries(function (entries) {
          entries.forEach((entry) => {
            directoryEntries.push(entry);
          });
          if (entries.length != 0) {
            readEntries();
          } else {
            resolve(directoryEntries);
          }
        });
      }
    });
  }
}
