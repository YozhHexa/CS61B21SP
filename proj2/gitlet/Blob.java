package gitlet;

import java.io.Serializable;
import java.io.File;
import java.nio.charset.StandardCharsets;

public class Blob implements Serializable {
    private byte[] content;

    public Blob(File f) {
        this.content = Utils.readContents(f);
    }

    public Blob(byte[] s) {
        this.content = s;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getContentAsString() {
        return new String(content, StandardCharsets.UTF_8);
    }
}
