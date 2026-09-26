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
        Branches branches = new Branches();
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
        Branches branches = readObject(BRANCHES, Branches.class);
        branches.put(branchName, c.id());

        // clear the staging area and store
        writeObject(join(COMMITS_DIR, c.id()), c);
        writeObject(BRANCHES, branches);
        writeObject(STAGINGAREA, new StagingArea());
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

    /**
     *  make file content to what it is in a specific commit
     *  or make all CWD back to a specific branch
     * @param args
     */
    /*
     * Usages:
     * java gitlet.Main checkout -- [file name]
     * java gitlet.Main checkout [commit id] -- [file name]
     * java gitlet.Main checkout [branch name]
     */
    public static void checkout(String[] args) {
        if (args.length == 3 && args[1].equals("--")) {
            checkoutSpecificCommitFile(getHeadCommit().id(), args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutSpecificCommitFile(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        } else {
            throw Utils.error("Incorrect operands");
        }
    }

    /**
     * Replaces or creates {@code fileName} in the working directory with
     * the version tracked by the commit identified by {@code commitID}.
     * The {@code commitID} may be abbreviated. Does not modify the staging area.
     */
    public static void checkoutSpecificCommitFile(String commitID, String fileName) {
        Commit commit = readObject(join(COMMITS_DIR, commitID), Commit.class);
        if (commit == null) {
            throw Utils.error("No commit with that id exists.");
        }

        String blobID = commit.getFileId(fileName);
        if (blobID == null) {
            throw Utils.error("File does not exist in that commit.");
        }
        String content = readObject(join(BLOBS_DIR, blobID), Blob.class).getContent();
        writeContents(join(CWD, fileName), content);
    }

    /**
     * Replaces files in the CWD with the version tracked by the commit at the
     * head of {@code branchName}. Overwrites if the files is already there. Deletes
     * files tracked by current branch but not tracked by target commit.
     * Makes {@code branchName} the current branch. Clears the staging area.
     *
     * @throws GitletException if the {@code branchName} does not exist, is the current
     *                          branch, or the checkout would overwrite an untracked file.
     */
    public static void checkoutBranch(String branchName) {
        if (branchName.equals(readContentsAsString(HEAD))) {
            throw Utils.error("No need to checkout the current branch.");
        }

        Branches branches = readObject(BRANCHES, Branches.class);
        String targetCommitID = branches.get(branchName);
        if (targetCommitID == null) {
            throw Utils.error("No such branch exists.");
        }

        Commit targetCommit = readObject(join(COMMITS_DIR, targetCommitID), Commit.class);
        Set<String> targetFiles = targetCommit.getSnapshot().keySet();
        List<String> filesInCWD = plainFilenamesIn(CWD);

        Commit head = getHeadCommit();

        // untracked in the current branch and would be overwritten by the checkout
        for (String fileName : filesInCWD) {
            if (!head.containsFile(fileName) && targetFiles.contains(fileName)) {
                throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        // tracked by current branch but not present in the checkout branch
        for (String fileName : filesInCWD) {
            if (head.containsFile(fileName) && !targetFiles.contains(fileName)) {
                Utils.restrictedDelete(join(CWD, fileName));
            }
        }

        // overwrites the file 
        for (String fileName : targetFiles) {
            Blob b = readObject(join(BLOBS_DIR, targetCommit.getFileId(fileName)), Blob.class);
            Utils.writeContents(join(CWD, fileName), b.getContent());
        }

        writeContents(HEAD, branchName);
        writeObject(STAGINGAREA, new StagingArea());
    }

    /**
     * Shows the repository status, including current existing branches, staged addition
     * and removal, modification not staged for commit, untracked files.
     * Mark the current branch with asterisk. Entries should be listed in lexicographic order.
     */
    public static void status() {
        displayBranches();

        // display files staged for addition and removal
        displayStagedFiles();

        displayModificationNotStagedFiles();

        displayUntrackedFiles();
    }

    public static void displayBranches() {
        Set<String> branches = readObject(BRANCHES, Branches.class).keySet();
        String currentBranch = readContentsAsString(HEAD);
        System.out.println("=== Branches ===" );
        for (String branch : branches) {
            if (branch.equals(currentBranch)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println();
    }

    public static void displayStagedFiles() {
        StagingArea stagingArea = readObject(STAGINGAREA, StagingArea.class);
        Set<String> additions = stagingArea.getAdditionFilesName();
        Set<String> removals = stagingArea.getRemovalFilesName();

        System.out.println("=== Staged Files ===");
        for (String addition : additions) {
            System.out.println(addition);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String removal : removals) {
            System.out.println(removal);
        }
        System.out.println();
    }

    public static void displayModificationNotStagedFiles() {
        Commit headCommit = getHeadCommit();
        StagingArea stagingArea = readObject(STAGINGAREA, StagingArea.class);
        TreeSet<String> modificationsNotStaged = new TreeSet<>();
        Set<String> filesInHeadCommit = headCommit.getFilesName();
        List<String> filesInCWD = plainFilenamesIn(CWD);

        for (String file : filesInHeadCommit) {
            if (filesInCWD.contains(file)) {
                if (!sha1(readContentsAsString(join(CWD, file))).equals(headCommit.getFileId(file))
                        && !stagingArea.additionsContainsKey(file)
                        && !stagingArea.removalsContainsKey(file)) {
                    // file's content in CWD is different from that in head commit
                    modificationsNotStaged.add(file + " (modified)");
                }
            } else {
                if (!stagingArea.removalsContainsKey(file)) {
                    modificationsNotStaged.add(file + " (deleted)");
                }
            }
        }

        Set<String> additions = stagingArea.getAdditionFilesName();
        for (String addition : additions) {
            // Staged for addition, but deleted in the working directory
            if (!filesInCWD.contains(addition)) {
                modificationsNotStaged.add(addition + " (deleted)");
            } else if (!sha1(readContentsAsString(join(CWD, addition))).equals(stagingArea.getAdditionFileSha1value(addition))) {
                // Staged for addition, but with different contents than in the working directory
                modificationsNotStaged.add(addition + " (modified)");
            }

        }

        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String modification : modificationsNotStaged) {
            System.out.println(modification);
        }
        System.out.println();
    }

    public static void displayUntrackedFiles() {
        List<String> filesInCWD = plainFilenamesIn(CWD);
        StagingArea stagingArea = readObject(STAGINGAREA, StagingArea.class);

        System.out.println("=== Untracked Files ===");
        for (String file : filesInCWD) {
            if (!stagingArea.additionsContainsKey(file) && !getHeadCommit().containsFile(file)) {
                System.out.println(file);
            } else if (getHeadCommit().containsFile(file) && stagingArea.removalsContainsKey(file)) {
                System.out.println(file);
            }
        }
        System.out.println();
    }

    public static Commit getHeadCommit() {
        Branches branches = readObject(BRANCHES, Branches.class);
        String branchName = readContentsAsString(HEAD);
        String id = branches.get(branchName);
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

}
