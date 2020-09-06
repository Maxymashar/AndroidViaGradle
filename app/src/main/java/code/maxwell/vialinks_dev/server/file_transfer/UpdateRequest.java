package code.maxwell.vialinks_dev.server.file_transfer;

import com.google.gson.annotations.SerializedName;

public class UpdateRequest {
    @SerializedName("file_name")
    private final String fileName;
    @SerializedName("file_size")
    private final String fileSize;
    @SerializedName("uuid")
    private final String randomUUID;
    private final String action;

    public UpdateRequest(String fileName, String fileSize, String action, String randomUUID) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.action = action;
        this.randomUUID = randomUUID;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getRandomUUID() {
        return randomUUID;
    }

    public String getAction() {
        return action;
    }
}
