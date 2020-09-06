/* This function should be asynchronous */
export function uploadFile(fileArray, requestId,uuid) {
  const file = getFile(fileArray,uuid);
  const xhr = new XMLHttpRequest();
  xhr.open("POST", "/request_file_download");
  xhr.setRequestHeader("FileName", encodeURI(file.name));
  xhr.setRequestHeader("RequestId", requestId);
  xhr.setRequestHeader("uuid",uuid);
  xhr.setRequestHeader("Type", "NotNull");
  xhr.send(file);
}
function getFile(file_array, uuid) {
  for (const fl of file_array) {
    // console.log(fl.uuid);
    // console.log(uuid);
    if (fl.uuid == uuid) {
      // console.log("Found File");
      return fl.file;
    }
  }
}
