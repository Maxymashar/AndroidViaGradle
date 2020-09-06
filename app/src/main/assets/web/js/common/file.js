export function File(file,id){
  this.file = file;
  this.fileName = file.name;
  this.fileSize = file.size;
  this.deleted = false;
  this.uuid = id;
}