const fileListTitle = document.querySelector(".files_section .title");
export function filterFDR(filter_name, fdr_list) {
  const availableFiles = [];
  for (const fdr of fdr_list) {
    /* Do not affect the deleted elements */
    if (fdr.classList.contains("deleted")) {
      continue;
    } else if (
      getFileNameSpan(fdr).toUpperCase().indexOf(filter_name.toUpperCase()) ==
      -1
    ) {
      fdr.style.display = "none";
      fdr.classList.add("hidden");
    } else {
      availableFiles.push(fdr);
      fdr.style.display = "flex";
      fdr.classList.remove("hidden");
    }
  }
  if (availableFiles.length == 0) {
    fileListTitle.innerText = "No File(s) Available";
  } else {
    fileListTitle.innerText = "Available File(s)";
  }
  function getFileNameSpan(fdr) {
    return fdr.querySelector("div span").innerText;
  }
}
