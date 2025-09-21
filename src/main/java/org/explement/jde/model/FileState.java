package org.explement.jde.model;

public class FileState {
    private String content;
    private String lastSaved;

    public FileState(String content, String lastSaved) {
        this.content = content;
        this.lastSaved = lastSaved;
    }

    public boolean isDirty() {
        return !content.equals(lastSaved);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLastSaved() {
        return lastSaved;
    }

    public void setLastSaved(String lastSaved) {
        this.lastSaved = lastSaved;
    }
}
