export function DownloadRequest(requested_file_name, file_request_id, fdr,uuid) {
  this.requested_file_name = requested_file_name;
  this.file_request_id = file_request_id;
  this.fdr = fdr;
  this.toDownload = true;
  this.state = "Not Downloaded";
  this.uuid = uuid;
}

export function setDownloadStatus(fdr, download_status) {
  fdr.querySelector(".status_text span").innerText = download_status;
}

export function getDownloadRequest(requested_file_name,request_id, request_array,uuid) {
  for (const req of request_array) {
    if (req.uuid == uuid) {
      return req;
    }
  }
/*   console.error(`Could not find the download request of the file >> ${requested_file_name} of the request id >> ${request_id}`); */
}
