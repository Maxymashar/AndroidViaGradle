import { getFDRList } from "../common/fdr";

const fileSelectionContainer = document.querySelector(".selection_container");
const selectAll = document.querySelector(".select_all img");
const selectNone = document.querySelector(".select_none");
const invertSelection = document.querySelector(".invert_selection");
const fileArrayElement = document.querySelector(".chosen_files");
const searchIcon = document.querySelector(".search_icon");
const searchTextInput = document.querySelector(".search_input");
const fileListTitle = document.querySelector(".files_section .title");

searchIcon.addEventListener("click", function () {
  searchTextInput.focus();
});

selectAll.addEventListener("click", function () {
  for (const fdr of fileArrayElement.querySelectorAll("li")) {
    if (fdr.classList.contains("hidden")) {
      continue;
    }
    fdr.classList.add("selected");
  }
});

selectNone.addEventListener("click", function () {
  for (const fdr of fileArrayElement.querySelectorAll("li")) {
    if (fdr.classList.contains("hidden")) {
      continue;
    }
    fdr.classList.remove("selected");
  }
});

invertSelection.addEventListener("click", function () {
  for (const fdr of fileArrayElement.querySelectorAll("li")) {
    if (fdr.classList.contains("hidden")) {
      continue;
    }
    fdr.classList.toggle("selected");
  }
});

export function checkSelected() {
  if (getSelectedFDR().length > 0) {
    fileSelectionContainer.classList.add("enabled");
  } else {
    fileSelectionContainer.classList.remove("enabled");
  }
}

export function setSelection(fdr) {
  fdr.classList.toggle("selected");
}

export function getSelectedFDR() {
  const selectedFDR = [];
  for (const fdr of fileArrayElement.querySelectorAll("li")) {
    if (fdr.classList.contains("selected")) {
      selectedFDR.push(fdr);
    }
  }
  return selectedFDR;
}

export function setFileListTitle() {
  if ((searchTextInput.value == "")) {
    if (fileArrayElement.childElementCount - getDeletedFDR().length == 0) {
      fileListTitle.innerText = "No File(s) Available";
    } else {
      fileListTitle.innerText = "Available File(s)";
    }
  } else {
    if (getAvailableFDR.length == 0) {
      fileListTitle.innerText = "No File(s) Available";
    } else {
      fileListTitle.innerText = "Available File(s)";
    }
  }
}
function getAvailableFDR() {
  const availableFDR = [];
  for (const fdr of getFDRList()) {
    if (fdr.classList.contains("deleted") || fdr.classList.contains("hidden")) {
      continue;
    }
    availableFDR.push(fdr);
  }
  return availableFDR;
}

function getDeletedFDR() {
  const deletedFDR = [];
  for (const fdr of getFDRList()) {
    if (fdr.classList.contains("deleted")) {
      deletedFDR.push(fdr);
    }
  }
  return deletedFDR;
}
