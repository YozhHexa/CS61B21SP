package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /** The timestamp of this Commit. */
    private Date timestamp;
    /** The parent of this Commit. */
    private String parent;
    /** TODO :The second parent of this Commit. */
    // private String secondParent;
    /** The snapshot of this Commit. */
    private HashMap<String, String> snapshot;

    /* TODO: fill in the rest of this class. */
    public Commit(String m, Date t, Commit p) {
        this.message = m;
        this.timestamp = t;
        this.snapshot = new HashMap<>();
        if (p != null) {
            this.parent = p.id();
        }
    }

    /** Return the sha1value of this commit. */
    public String id() {
        return Utils.sha1(Utils.serialize(this));
    }

    /** Return the message of this commit. */
    public String getMessage() {
        return message;
    }

    /** Return the sha1value of the file in this commit. */
    public String getFileId(String fileName) {
        return snapshot.get(fileName);
    }

    /** Return the copy of the snapshot in this commit. */
    public Map<String, String> getSnapshot() {
        Map<String, String> m = new HashMap<>(snapshot);
        return m;
    }

    /** Add file to snapshot. */
    public void put(String fileName, String id) {
        snapshot.put(fileName, id);
    }

    /** Remove file from snapshot. */
    public void remove(String fileName) {
        snapshot.remove(fileName);
    }

    /** Return the snapshot contains the key or not. */
    public boolean containsFile(String fileName) {
        return snapshot.containsKey(fileName);
    }

    public void printLog() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.ENGLISH);
        // Date: Thu Nov 9 20:00:05 2017 -0800

        formatter.format("Date: %1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", timestamp);
        System.out.println("===");
        System.out.println("commit " + id());
        //TODO: print merge log
        System.out.println(sb);
        System.out.println(message + "\n");
    }

    public Commit parent() {
        if (parent == null) {
            return null;
        }
        return Utils.readObject(Utils.join(Repository.COMMITS_DIR, parent), Commit.class);
    }

    public SortedSet<String> getFilesName() {
        return new TreeSet<>(getSnapshot().keySet());
    }

}
