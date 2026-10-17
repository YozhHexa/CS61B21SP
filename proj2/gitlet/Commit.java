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
    private String secondParent;
    /** The snapshot of this Commit. */
    private TreeMap<String, String> snapshot;

    public Commit(String m, Date t, Commit p, TreeMap<String, String> s) {
        this.message = m;
        this.timestamp = t;
        this.snapshot = s;
        if (p != null) {
            this.parent = p.getID();
        }
        secondParent = null;
    }

    public Commit(String m, Date t, Commit p1, Commit p2, TreeMap<String, String> s) {
        this.message = m;
        this.timestamp = t;
        this.snapshot = s;
        if (p1 != null) {
            this.parent = p1.getID();
        }
        if (p2 != null) {
            this.secondParent = p2.getID();
        }
    }

    /** Return the sha1value of this commit. */
    public String getID() {
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
    public TreeMap<String, String> getSnapshot() {
        TreeMap<String, String> m = new TreeMap<>(snapshot);
        return m;
    }

    /** Return the snapshot contains the key or not. */
    public boolean contains(String fileName) {
        return snapshot.containsKey(fileName);
    }

    public void printLog() {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.ENGLISH);
        // Date: Thu Nov 9 20:00:05 2017 -0800

        formatter.format("Date: %1$ta %1$tb %1$te %1$tH:%1$tM:%1$tS %1$tY %1$tz", timestamp);
        System.out.println("===");
        System.out.println("commit " + getID());
        if (secondParent != null) {
            System.out.println("Merge: " + parent.substring(0, 7) + " " + secondParent.substring(0, 7));
        }
        System.out.println(sb);
        System.out.println(message + "\n");
    }

    public Commit getparent() {
        if (parent == null) {
            return null;
        }
        return Utils.readObject(Utils.join(Repository.COMMITS_DIR, parent), Commit.class);
    }

    /**
     * Returns the commit is ancestor of Commit C or not.
     * Returns false if the commit is Commit c itself or Commit C is null.
     */
    public boolean isAncestor(Commit c) {
        if (c == null) {
            return false;
        }

        if (c.ancestors().keySet().contains(this.getID())) {
            return true;
        }

        return false;
    }

    public TreeMap<String, Commit> ancestors() {
        TreeMap<String, Commit> t = new TreeMap<>();
        Commit p = getparent();
        Commit p2 = getsecondParent();

        if (p != null) {
            t.put(p.getID(), p);
            t.putAll(p.ancestors());
        }

        if (p2 != null) {
            t.put(p2.getID(), p2);
            t.putAll(p2.ancestors());
        }

        return t;
    }

    public SortedSet<String> getFilesName() {
        return new TreeSet<>(getSnapshot().keySet());
    }

    public Commit getsecondParent() {
        if (secondParent == null) {
            return null;
        }
        return Utils.readObject(Utils.join(Repository.COMMITS_DIR, secondParent), Commit.class);
    }



}
