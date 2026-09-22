package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  Storing the commit log and file blob maybe everything
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The commits' directory. */
    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");
    /** The blobs' directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    /** The branches. file*/
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    /** The staging area. */
    public static final File STAGINGAREA = join(GITLET_DIR, "stagingArea");
    /** The head pointer. */
    public static final File HEAD = join(GITLET_DIR, "head");



    /* TODO: fill in the rest of this class. */

    /** Creating the initial commit and all dir needed in .gitlet including itself
     *  TODO: creating all dir, including blobs, commits, stagingArea, something else that I haven't not know yet
     */
    public static void init() {
        // check if the dir has been gitleted yet
        if (GITLET_DIR.exists()) {
            throw Utils.error("A Gitlet version-control system already exists in the current directory.");
        }

        // create all dir or file needed in .gitlet
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        HashMap<String, String> branches = new HashMap<>();
        StagingArea s = new StagingArea();
        writeObject(STAGINGAREA, s);


        // create and store the initial commit
        Commit initCommit = new Commit("initial commit", new Date(0), null);
        String idInitCommit = sha1(serialize(initCommit));
        branches.put("master", idInitCommit);
        writeObject(join(COMMITS_DIR, idInitCommit), initCommit);
        writeContents(HEAD, "master");
        writeObject(BRANCHES, branches);
    }


    public static void add(String fileName) {
        File fileToAdd = join(CWD, fileName);
        if (!fileToAdd.exists()) {
            throw Utils.error("File does not exist.");
        }

        String fileContent = readContentsAsString(fileToAdd);
        String sha1Value = sha1(fileContent);

        // unstage for removal
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        if (s.removalsContainsKey(fileName)) {
            s.unstageRemoval(fileName);
        }

        // if identical to the version in head commit, unstage and if already, delete it
        Commit head = getHeadCommit();
        if (sha1Value.equals(head.getFileId(fileName))) {
            if (s.additionsContainsKey(fileName)) {
                s.unstageAddition(fileName);
            }
            writeObject(STAGINGAREA, s);
            return;
        }

        // add or overwrite and store
        s.stageAddition(fileName, sha1Value);
        Blob b = new Blob(fileContent);
        writeObject(join(BLOBS_DIR, sha1Value), b);
        writeObject(STAGINGAREA, s);
    }

    public static void commit(String msg) {
        // check the staging area is blank or not
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        if (s.isBlank()) {
            throw Utils.error("No changes added to the commit.");
        }

        Commit headcommit = getHeadCommit();
        Commit c = new Commit(msg, new Date(), headcommit);
        Map<String, String> snapshotHeadCommit = headcommit.getSnapshot();
        Map<String, String> additions = s.getAdditions();
        Set<String> removals = s.getRemovals();

        // copy file from snapshot in head commit and track or untrack files in staging area.
        for (Map.Entry<String, String> entry : snapshotHeadCommit.entrySet()) {
            c.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : additions.entrySet()) {
            c.put(entry.getKey(), entry.getValue());
        }
        for (String fileName : removals) {
            c.remove(fileName);
        }

        // change head commit, by changing branch's pointer
        String branchName = readContentsAsString(HEAD);
        HashMap<String, String> branches = readObject(BRANCHES, HashMap.class);
        branches.put(branchName, c.id());

        // clear the staging area and store
        writeObject(join(COMMITS_DIR, c.id()), c);
        writeObject(BRANCHES, branches);
        s = new StagingArea();
        writeObject(STAGINGAREA, s);
    }

    public static void rm(String fileName) {
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        Commit head = getHeadCommit();

        if (!s.additionsContainsKey(fileName) && !head.containsFile(fileName)) {
            throw Utils.error("No reason to remove the file.");
        }

        if (s.additionsContainsKey(fileName)) {
            s.unstageAddition(fileName);
        }

        if (head.containsFile(fileName)) {
            s.stageRemoval(fileName);
            File f = join(CWD, fileName);
            restrictedDelete(f);
            writeObject(STAGINGAREA, s);
        }
    }

    public static void log() {
        // TODO: if merge, print merge: id id

        Commit commit = getHeadCommit();
        commit.printLog();

        while (commit.parent() != null) {
            commit = commit.parent();
            commit.printLog();
        }
    }

    public static void globalLog() {
        List<String> commitsID = Utils.plainFilenamesIn(COMMITS_DIR);
        if (commitsID == null) {
            return;
        }

        for (String commitID : commitsID) {
            Commit c = readObject(join(COMMITS_DIR, commitID), Commit.class);
            c.printLog();
        }
    }

    public static void find(String msg) {
        List<String> commitsID = Utils.plainFilenamesIn(COMMITS_DIR);
        if (commitsID == null) {
            return;
        }

        boolean found = false;
        for (String commitID : commitsID) {
            Commit c = readObject(join(COMMITS_DIR, commitID), Commit.class);
            if (c.getMessage().equals(msg)) {
                found = true;
                System.out.println(c.id());
            }
        }

        if (found) {
            throw Utils.error("Found no commit with that message.");
        }
    }

    public static void checkout(String[] args) {
        // Usages:
        //
        //java gitlet.Main checkout -- [file name]
        //
        //java gitlet.Main checkout [commit id] -- [file name]
        //
        //java gitlet.Main checkout [branch name]

        if (args.length == 3 && args[1].equals("--")) {
            String fileName = args[2];

            String content = getFileContentFromCommit(getHeadCommit(), fileName);
            writeContents(join(CWD, fileName), content);
        }

        if (args.length == 4 && args[2].equals("--")) {
            String commitID = args[1];
            String fileName = args[3];

            Commit c = readObject(join(COMMITS_DIR, commitID), Commit.class);
            String content = getFileContentFromCommit(c, fileName);
            writeContents(join(CWD, fileName), content);
        }

        if (args.length == 2) {
            String branch = args[1];

            Map<String, String> branches = readObject(BRANCHES, HashMap.class);
            String commitID = branches.get(branch);
            if (commitID == null) {
                throw Utils.error("No such branch exists.");
            } else if (commitID.equals(getHeadCommit().id())) {
                throw Utils.error("No need to checkout the current branch.");
            }

            Commit c = readObject(join(COMMITS_DIR, branches.get(branch)), Commit.class);
            Set<String> filesCommit = c.getSnapshot().keySet();
            List<String> filesCWD = plainFilenamesIn(CWD);

            if (filesCWD == null) {
                return;
            }

            Commit head = getHeadCommit();
            for (String fileName : filesCWD) {
                // tracked by current branch but not present in the checkout branch
                if (head.containsFile(fileName) && !filesCommit.contains(fileName)) {
                    Utils.restrictedDelete(join(CWD, fileName));
                } else if (!head.containsFile(fileName) && filesCommit.contains(fileName)) {
                    // untracked in the current branch and would be overwritten by the checkout
                    throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
                } else {
                    String content = readObject(join(BLOBS_DIR, c.getFileId(fileName)), Blob.class).getContent();
                    Utils.writeContents(join(CWD, fileName), content);
                }
            }

            writeContents(HEAD, branch);
        }
    }

    public static void status() {
        /*
        === Branches ===
*master
other-branch

=== Staged Files ===
wug.txt
wug2.txt

=== Removed Files ===
goodbye.txt

=== Modifications Not Staged For Commit ===
junk.txt (deleted)
wug3.txt (modified)

=== Untracked Files ===
random.stuff
         */
        // TODO: get all branches and head branch
        // TODO: get files to add and remove
        // TODO:
        //      Tracked in the current commit, changed in the working directory, but not staged; or
        //  Staged for addition, but with different contents than in the working directory; or
        //  Staged for addition, but deleted in the working directory; or
        //      Not staged for removal, but tracked in the current commit and deleted from the working directory.
        //
        // TODO: files present in the working directory but neither staged for addition nor tracked. This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge.


    }

    public static Commit getHeadCommit() {
        HashMap<String, String> branches = readObject(BRANCHES, HashMap.class);
        String branchName = readContentsAsString(HEAD);
        String id = branches.get(branchName);
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

    public static String getFileContentFromCommit(Commit c, String fileName) {
        String contentID = c.getFileId(fileName);
        if (contentID == null) {
            throw Utils.error("File does not exist in that commit.");
        }

        return readObject(join(BLOBS_DIR, contentID), Blob.class).getContent();
    }


}
