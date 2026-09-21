package gitlet;

import java.io.Serializable;

public class Blob implements Serializable{
    private String content;

    public Blob(String c) {
        this.content = c;
    }

    public String getContent() {
        return this.content;
    }
}
