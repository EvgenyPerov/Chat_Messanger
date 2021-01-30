package myChatMessanger;

import java.io.File;
import java.io.Serializable;

public class Message implements Serializable {
    private final MessageType type;
    private String data;
    private File file;
    private String toUserName;

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
        this.toUserName = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
        this.toUserName = null;
    }

    public Message(MessageType type, String data, String toUserName) {
        this.type = type;
        this.data = data;
        this.toUserName = toUserName;

    }

    public Message(MessageType type, File file, String toUserName) {
        this.type = type;
        this.file = file;
        this.toUserName = toUserName;

    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }

    public String getToUserName() {
        return toUserName;
    }

    public File getFile() { return file; }
}
