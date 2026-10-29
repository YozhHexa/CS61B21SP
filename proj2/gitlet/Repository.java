package gitlet;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Objects;

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
        Commit initCommit = new Commit("initial commit", new Date(0), null, new TreeMap<>());
        branches.put("master", initCommit.getID());
        writeObject(join(COMMITS_DIR, initCommit.getID()), initCommit);
        writeContents(HEAD, "master");
        writeObject(BRANCHES, branches);
    }


    public static void add(String fileName) {
        File file = join(CWD, fileName);
        if (!file.exists()) {
            throw Utils.error("File does not exist.");
        }

        String sha1Value = sha1(readContents(file));

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
        Blob b = new Blob(file);
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
        TreeMap<String, String> snapshot = headcommit.getSnapshot();

        Map<String, String> additions = s.getAdditions();
        snapshot.putAll(additions);

        Set<String> removals = s.getRemovals();
        for (String fileName : removals) {
            snapshot.remove(fileName);
        }

        Commit c = new Commit(msg, new Date(), headcommit, snapshot);
        // change head commit, by changing branch's pointer
        String branchName = readContentsAsString(HEAD);
        Branches branches = readObject(BRANCHES, Branches.class);
        branches.put(branchName, c.getID());

        // clear the staging area and store
        writeObject(join(COMMITS_DIR, c.getID()), c);
        writeObject(BRANCHES, branches);
        writeObject(STAGINGAREA, new StagingArea());

    }

    public static void rm(String fileName) {
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        Commit head = getHeadCommit();

        if (!s.additionsContainsKey(fileName) && !head.contains(fileName)) {
            throw Utils.error("No reason to remove the file.");
        }

        if (s.additionsContainsKey(fileName)) {
            s.unstageAddition(fileName);
        }

        if (head.contains(fileName)) {
            s.stageRemoval(fileName);
            File f = join(CWD, fileName);
            restrictedDelete(f);
        }
        writeObject(STAGINGAREA, s);
    }

    public static void log() {
        // TODO: if merge, print merge: id id

        Commit commit = getHeadCommit();
        commit.printLog();

        while (commit.getparent() != null) {
            commit = commit.getparent();
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
                System.out.println(c.getID());
            }
        }

        if (!found) {
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
            checkoutSpecificCommitFile(getHeadCommit().getID(), args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            checkoutSpecificCommitFile(args[1], args[3]);
        } else if (args.length == 2) {
            checkoutBranch(args[1]);
        } else {
            throw Utils.error("Incorrect operands");
        }
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

    /**
     * Creates a new branch with the given branch name, and points it at current head commit.
     * Does not switch to the new branch.
     */
    public static void branch(String branchName) {
        Branches branches = readObject(BRANCHES, Branches.class);
        if (branches.contains(branchName)) {
            throw Utils.error("A branch with that name already exists.");
        }
        branches.put(branchName, getHeadCommit().getID());
        writeObject(BRANCHES, branches);
    }

    /**
     * Deletes the pointer associated with the {@code branchName}.
     * Does not delete all commits under the {@code branchName}.
     */
    public static void rmBranch(String branchName) {
        Branches branches = readObject(BRANCHES, Branches.class);
        if (!branches.contains(branchName)) {
            throw Utils.error("A branch with that name does not exist.");
        }
        if (readContentsAsString(HEAD).equals(branchName)) {
            throw Utils.error("Cannot remove the current branch.");
        }

        branches.delete(branchName);
        writeObject(BRANCHES, branches);
    }

    /**
     * Checks out all files tracked by the commit identified by {@code commitID}.
     * For files in the CWD but not in the given commit, removes if tracked, throws
     * error if untracked. Moves the current branch's head pointer to the commit.
     * Clears the staging area. The {@code commitID} may be abbreviated.
     */
    public static void reset(String commitID) {
        Commit targetCommit = resolveCommitID(commitID);
        if (targetCommit == null) {
            throw Utils.error("No commit with that id exists.");
        }
        List<String> filesInCWD = plainFilenamesIn(CWD);

        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        for (String file : filesInCWD) {
            if (targetCommit.contains(file) && !getHeadCommit().contains(file) && !s.additionsContainsKey(file)) {
                throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
            }
        }

        for (String file : filesInCWD) {
            if (!targetCommit.contains(file) && getHeadCommit().contains(file)) {
                Utils.restrictedDelete(join(CWD, file));
            }
        }

        Map<String, String> snapshot = targetCommit.getSnapshot();
        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            Blob b = readObject(join(BLOBS_DIR, entry.getValue()), Blob.class);
            byte[] content = b.getContent();
            writeContents(join(CWD, entry.getKey()), content);
        }

        writeObject(STAGINGAREA, new StagingArea());
        Branches branches = readObject(BRANCHES, Branches.class);
        branches.put(readContentsAsString(HEAD), targetCommit.getID());
        writeObject(BRANCHES, branches);
    }

    /**
     * Merges files from the given branch named by {@code branchName} into the current branch.
     * @param branchName
     */
    public static void merge(String branchName) {
        // If the staging area is not blank, throws error and exits.
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        if (!s.isBlank()) {
            throw Utils.error("You have uncommitted changes.");
        }

        String targetCommitID = readObject(BRANCHES, Branches.class).get(branchName);
        // If the given branch doesn't exist, throws error and exist.
        if (targetCommitID == null) {
            throw Utils.error("A branch with that name does not exist.");
        }

        Commit given = readObject(join(COMMITS_DIR, targetCommitID), Commit.class);
        Commit current = getHeadCommit();
        Commit split = splitPoint(current, given);

        // If the given branch is current branch, throws error and exits.
        if (readContentsAsString(HEAD).equals(branchName)) {
            throw Utils.error("Cannot merge a branch with itself.");
        }

        //  If an untracked file in the current commit would be overwritten or
        //  deleted by the merge, print There is an untracked file in the way;
        //  delete it, or add and commit it first.
        List<String> filesInCWD = plainFilenamesIn(CWD);
        for (String file : filesInCWD) {
            if (!current.contains(file) && given.contains(file)) {
                throw Utils.error("There is an untracked file in the way; delete it, or add " +
                        "and commit it first.");
            }
        }

        // If given commit is ancestor of current commit.
        if (given.getID().equals(split.getID())) {
            System.out.println("Given branch is an ancestor of the current branch");
            return;
        }
        // If current commit is ancestor of given commit.
        if (current.getID().equals(split.getID())) {
            checkoutBranch(branchName);
            System.out.println("Current branch fast-forwarded");
            return;
        }

        Map<String, String> givenFiles = given.getSnapshot();
        Map<String, String> currentFiles = current.getSnapshot();
        Map<String, String> splitFiles = split.getSnapshot();

        Set<String> allFiles = new TreeSet<>(givenFiles.keySet());
        allFiles.addAll(currentFiles.keySet());
        allFiles.addAll(splitFiles.keySet());

        boolean conflict = false;
        for (String fileName : allFiles) {
            String gBlob = givenFiles.get(fileName);
            String sBlob = splitFiles.get(fileName);
            String cBlob = currentFiles.get(fileName);

            if (Objects.equals(gBlob, cBlob)) {
                continue;
            }
            // If the file was changed in the given branch but remained unchanged
            // in the current branch since the split point, use the given version.
            // This also covers files newly added only in the given branch.
            if (Objects.equals(cBlob, sBlob) && gBlob != null ) {
                writeContents(join(CWD, fileName), readObject(join(BLOBS_DIR, gBlob), Blob.class).getContent());
                s.stageAddition(fileName, gBlob);
                continue;
            }

            // If the file was deleted in the given branch but existed in the split commit
            // and the current commit, it should be removed and untracked.
            if (Objects.equals(sBlob, cBlob) && gBlob == null) {
                restrictedDelete(join(CWD, fileName));
                s.stageRemoval(fileName);
                continue;
            }

            if (Objects.equals(sBlob, gBlob)) {
                continue;
            }

            conflict = true;
            byte[] c = conflictContent(cBlob, gBlob).getBytes(StandardCharsets.UTF_8);
            writeContents(join(CWD, fileName), c);
            Blob b = new Blob(c);
            s.stageAddition(fileName, sha1(c));
            writeObject(join(BLOBS_DIR, sha1(c)), b);
        }
        writeObject(STAGINGAREA, s);

        String msg = "Merged " + branchName + " into " + readContentsAsString(HEAD) + '.';
        mergeCommit(msg, current, given);

        if (conflict) {
            System.out.println("Encountered a merge conflict");
        }
    }

    private static void mergeCommit(String msg, Commit p1, Commit p2) {
        // check the staging area is blank or not
        StagingArea s = readObject(STAGINGAREA, StagingArea.class);
        if (s.isBlank()) {
            throw Utils.error("No changes added to the commit.");
        }

        Commit headcommit = p1;
        TreeMap<String, String> snapshot = headcommit.getSnapshot();

        Map<String, String> additions = s.getAdditions();
        snapshot.putAll(additions);

        Set<String> removals = s.getRemovals();
        for (String fileName : removals) {
            snapshot.remove(fileName);
        }

        Commit c = new Commit(msg, new Date(), headcommit, p2, snapshot);
        // change head commit, by changing branch's pointer
        String branchName = readContentsAsString(HEAD);
        Branches branches = readObject(BRANCHES, Branches.class);
        branches.put(branchName, c.getID());

        // clear the staging area and store
        writeObject(join(COMMITS_DIR, c.getID()), c);
        writeObject(BRANCHES, branches);
        writeObject(STAGINGAREA, new StagingArea());
    }

    /**
     * Print the error message when encountering file conflict.
     */
    public static String conflictContent(String currentBlobID, String targetBlobID) {
        String currentContent = "";
        if (currentBlobID != null) {
            currentContent = readObject(join(BLOBS_DIR, currentBlobID), Blob.class).getContentAsString();
        }
        //System.out.println(currentContent);
        String targetContent = "";
        if (targetBlobID != null) {
            targetContent = readObject(join(BLOBS_DIR, targetBlobID), Blob.class).getContentAsString();
        }
        //System.out.println(targetContent);

        String conflictContent = "<<<<<<< HEAD\n" + currentContent + "=======\n" + targetContent + ">>>>>>>\n";

        return conflictContent;
    }

    /**
     * Returns the split point of Commit A and Commit B
     */
    public static Commit splitPoint(Commit a, Commit b) {
        if (a.isAncestor(b)) {
            return a;
        }
        if (b.isAncestor(a)) {
            return b;
        }

        if (a.getID().equals(b.getID())) {
            return a;
        }
        TreeMap<String, Commit> aAncestors = a.ancestors();
        TreeMap<String, Commit> bAncestors = b.ancestors();

        Set<Commit> common = new HashSet<>();
        for (String ancestorID : aAncestors.keySet()) {
            if (bAncestors.keySet().contains(ancestorID)) {
                common.add(aAncestors.get(ancestorID));
            }
        }

        return minCommit(common);
    }

    public static Commit minCommit(Set<Commit> s) {
        for (Commit c : s) {
            boolean older = false;
            for (Commit b : s) {
                if (c.isAncestor(b)) {
                    older = true;
                    break;
                }
            }
            if (!older) {
                return c;
            }
        }

        return null;
    }
    /**
     * Returns the commit identified by {@code commitID}, the commitID
     * may be abbreviated. Returns null if no such commit exists.
     */
    public static Commit resolveCommitID(String commitID) {
        if (commitID.length() < 40) {
            List<String> commits = plainFilenamesIn(COMMITS_DIR);
            for (String commit : commits) {
                if (commit.startsWith(commitID)) {
                    commitID = commit;
                    break;
                }
            }
        }
        if (!join(COMMITS_DIR, commitID).exists()) {
            return null;
        }
        Commit commit = readObject(join(COMMITS_DIR, commitID), Commit.class);
        return commit;
    }


    public static Commit getHeadCommit() {
        Branches branches = readObject(BRANCHES, Branches.class);
        String branchName = readContentsAsString(HEAD);
        String id = branches.get(branchName);
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

    /**
     * Replaces or creates {@code fileName} in the working directory with
     * the version tracked by the commit identified by {@code commitID}.
     * The {@code commitID} may be abbreviated. Does not modify the staging area.
     */
    public static void checkoutSpecificCommitFile(String commitID, String fileName) {
        Commit commit = resolveCommitID(commitID);
        if (commit == null) {
            throw Utils.error("No commit with that id exists.");
        }

        String blobID = commit.getFileId(fileName);
        if (blobID == null) {
            throw Utils.error("File does not exist in that commit.");
        }
        byte[] content = readObject(join(BLOBS_DIR, blobID), Blob.class).getContent();
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
            if (!head.contains(fileName) && targetFiles.contains(fileName)) {
                throw Utils.error("There is an untracked file in the way; " +
                        "delete it, or add and commit it first.");
            } else if (head.contains(fileName) && !targetFiles.contains(fileName)) {
                // tracked by current branch but not present in the checkout branch
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
            if (!stagingArea.additionsContainsKey(file) && !getHeadCommit().contains(file)) {
                System.out.println(file);
            } else if (getHeadCommit().contains(file) && stagingArea.removalsContainsKey(file)) {
                System.out.println(file);
            }
        }
        System.out.println();
    }
}
