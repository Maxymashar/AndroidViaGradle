export function sendUpdatedFiles(request_array) {
  const requestPath = "/upload_file_data";
  const JSON_REQUEST = JSON.stringify(request_array);
  /* Open an ajax call */
  const ajax = new XMLHttpRequest();
  ajax.open("POST", requestPath);
  ajax.setRequestHeader("Type", "NotNull");
  ajax.send(JSON_REQUEST);
}

export function UpdateRequest(file_name, file_size, action,id) {
  this.file_name = encodeURI(file_name);
  this.file_size = file_size;
  this.action = action;
  this.uuid = id;
}
