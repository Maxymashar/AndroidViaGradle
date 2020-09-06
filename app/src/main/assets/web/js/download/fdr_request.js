/* Request  the fdr as JSON */
export function getAvailableFiles() {
  const xhr = new XMLHttpRequest();
  xhr.open("GET", "/get_file_data");
  xhr.send(null);
  return xhr;
}
