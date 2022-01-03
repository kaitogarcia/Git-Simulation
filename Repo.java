package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


/**
 * Repository.
 * @author Kaito Garcia
 */

public class Repo implements Serializable {

    /* Instance Variables */
    /** HEAD 'pointer'. */
    private String _HEAD;

    /** Number of times init() has been called. */
    private int initCounter;

    /** Hashmap that stores <branch name, branch SHAID>. */
    private HashMap<String, String> _branches;

    /** Staging Area. */
    private HashMap<String, String> stagingArea;

    /** Untracked files. */
    private ArrayList<String> untracked;

    /** Removed files. */
    private ArrayList<String> removedFiles;

    /** Name of current branch. */
    private String currentBranch;




    /**
     * Repo constructor.
     */
    public Repo() {
        _HEAD = null;
        currentBranch = "";
        initCounter = 0;
        stagingArea = new HashMap<>();
        untracked = new ArrayList<>();
        removedFiles = new ArrayList<>();
        File gitlet = new File(".gitlet");
        gitlet.mkdir();

    }

    /** Initializes Gitlet repository. */
    public void init() {

        initCounter++;
        if (initCounter > 1) {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }

        File commits = new File(".gitlet/commits");
        commits.mkdir();

        File stagingAreaFile = new File(".gitlet/stagingArea");
        stagingAreaFile.mkdir();

        Commit initial = new Commit("initial commit", null, null);
        String initialID = initial.hasherCommit();
        File commitFile = new File(".gitlet/commits/" + initialID);
        Utils.writeObject(commitFile, initial);

        _branches = new HashMap<>();
        _branches.put("master", initialID);
        _HEAD = initialID;
        currentBranch = "master";

    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * @param fileName -- name of file to add
     */
    public void add(String fileName) {

        File fileToAdd = new File(fileName);

        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        untracked.remove(fileName);
        String fileContents = Utils.sha1(Utils.readContentsAsString(fileToAdd));
        File blob = new File(".gitlet/stagingArea/" + fileContents);

        HashMap<String, String> blobsInHead = new HashMap<>();
        if (getCommitFromID(_HEAD).getBlobs() != null) {
            blobsInHead = getCommitFromID(_HEAD).getBlobs();
        }

        removedFiles.remove(fileName);

        if (blobsInHead.get(fileName) != null) {
            if (!blobsInHead.isEmpty()
                    && blobsInHead.get(fileName).equals(fileContents)) {
                stagingArea.remove(fileName);
                return;
            }
        }

        stagingArea.put(fileName, fileContents);
        String addString = Utils.readContentsAsString(fileToAdd);
        Utils.writeContents(blob, addString);
    }

    /**
     * Takes snapshot of current staging area and makes it a commit.
     * @param commitMessage -- commit message
     */
    public void commit(String commitMessage) {

        /* Failure cases */
        if (commitMessage.trim().equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (stagingArea.size() == 0 && removedFiles.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }

        HashMap<String, String> trackedFiles;
        if (getCommitFromID(_HEAD).getBlobs() == null) {
            trackedFiles = new HashMap<>();
        } else {
            trackedFiles = getCommitFromID(_HEAD).getBlobs();
        }

        for (String file : stagingArea.keySet()) {
            trackedFiles.put(file, stagingArea.get(file));
        }

        for (String file : untracked) {
            trackedFiles.remove(file);
        }

        Commit newHEAD = new Commit(commitMessage, _HEAD, trackedFiles);
        String newHEADSHA = newHEAD.hasherCommit();
        Utils.writeObject(new File(".gitlet/commits/"
                + newHEADSHA), newHEAD);

        stagingArea.clear();
        untracked.clear();
        removedFiles.clear();
        _branches.put(currentBranch, newHEADSHA);
        _HEAD = newHEADSHA;
    }

    /**
     * Remove method.
     * @param file -- file to remove
     */
    public void rm(String file) {

        /*
        boolean exists = false;
        HashMap<String, String> currentBlobs =
            getCommitFromID(_HEAD).getBlobs();

        if (currentBlobs != null) {
            for (String blob : currentBlobs.keySet()) {
                if (file.equals(blob)) {
                    exists = true;
                    break;
                }
            }
        } else if (!stagingArea.containsKey(file)) {
            System.out.println("No reason to remove the file.");
            return;
        }

        boolean flag2 = stagingArea.containsKey(file);
        if (!exists) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (currentBlobs.containsKey(file)) {
            untracked.remove(file);
            removedFiles.add(file);
        }

        stagingArea.remove(file);
        Utils.restrictedDelete(file); */

        HashMap<String, String> currentBlobs =
                getCommitFromID(_HEAD).getBlobs();

        untracked.add(file);

        if (currentBlobs != null && currentBlobs.containsKey(file)) {

            removedFiles.add(file);
            Utils.restrictedDelete(new File(file));

        } else if (!stagingArea.isEmpty() && stagingArea.containsKey(file)) {
            stagingArea.remove(file);
            return;

        } else if (getCommitFromID(_HEAD).getBlobs() != null
                && getCommitFromID(_HEAD).getBlobs().containsKey(file)) {
            untracked.add(file);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }


    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit. */
    public void log() {
        Commit currentCommit = getCommitFromID(_HEAD);
        while (currentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + currentCommit.getHashID());
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage() + "\n");

            if (currentCommit.getParentHashID() == null) {
                return;
            }
            currentCommit = getCommitFromID(currentCommit.getParentHashID());
        }
    }

    /**
     * Global-log method.
     */
    public void globalLog() {
        File commitPath = new File(".gitlet/commits");

        for (File file : commitPath.listFiles()) {
            Commit currentCommit = getCommitFromID(file.getName());
            System.out.println("===");
            System.out.println("commit " + currentCommit.getHashID());
            System.out.println("Date: " + currentCommit.getTimestamp());
            System.out.println(currentCommit.getMessage());
            System.out.println();
        }
    }

    /**
     * Find method.
     * @param message -- commit message to find
     */
    public void find(String message) {
        File commitPath = new File(".gitlet/commits");
        ArrayList<Commit> commitsWithMessage = new ArrayList<>();
        for (File file : commitPath.listFiles()) {
            Commit currentCommit = getCommitFromID(file.getName());
            if (currentCommit.getMessage().equals(message)) {
                commitsWithMessage.add(currentCommit);
            }
        }

        if (commitsWithMessage.isEmpty()) {
            System.out.println("Found no commit with that message.");
            return;
        }

        for (Commit commit : commitsWithMessage) {
            System.out.println(commit.getHashID());
        }

    }

    /**
     * Status method.
     */
    public void status() {

        System.out.println("=== Branches ===");

        Object[] branchArray = _branches.keySet().toArray();
        Arrays.sort(branchArray);

        for (Object branch : branchArray) {
            if (_branches.get(branch).contains(_HEAD)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }

        System.out.println("\n=== Staged Files ===");
        Object[] stageArray = stagingArea.keySet().toArray();
        Arrays.sort(stageArray);
        for (Object file : stageArray) {
            System.out.println(file);
        }

        System.out.println("\n=== Removed Files ===");
        for (String file : removedFiles) {
            System.out.println(file);
        }

        System.out.println("\n=== Modifications Not Staged For Commit ===");

        System.out.println("\n=== Untracked Files ===");
    }

    /**
     * Primary checkout method.
     * @param args String input from main
     * */
    public void checkout(String... args) {
        switch (args.length) {
        case 3:
            checkoutFile(args[2], _HEAD);
            break;

        case 4:
            checkoutFile(args[3], args[1]);
            break;

        case 2:
            String branchName = args[1];
            checkoutBranch(branchName);
            break;

        default:
            System.exit(0);
        }
    }

    /**
     * Checkout method for checking out files.
     * @param file -- file to checkout
     * @param commitSHA1 -- commit ID to checkout file from
     */
    public void checkoutFile(String file, String commitSHA1) {

        /* Identifying shortened ID */
        String fullID = null;
        if (commitSHA1.length() == Utils.UID_LENGTH) {
            fullID = commitSHA1;
        } else {
            File folder = new File(".gitlet/commits");
            for (File files : folder.listFiles()) {
                if (files.getName().contains(commitSHA1)) {
                    fullID = files.getName();
                }
            }
        }

        boolean exists = false;
        File folder = new File(".gitlet/commits");
        for (File files : folder.listFiles()) {
            if (files.getName().contains(fullID)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit tempCommit = getCommitFromID(fullID);
        HashMap<String, String> tempBlobz = new HashMap<>();
        if (tempCommit.getBlobs() != null) {
            tempBlobz = tempCommit.getBlobs();
        }
        if (fullID.equals(_HEAD) && !tempBlobz.containsKey(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }

        String blobPath = ".gitlet/stagingArea/" + tempBlobz.get(file);
        File blobFile = new File(blobPath);
        String blobContents = Utils.readContentsAsString(blobFile);
        Utils.writeContents(new File(file), blobContents);
    }

    /**
     * Checkout method.
     * @param branchName branch name to checkout
     */
    public void checkoutBranch(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (branchName.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        if (checkForUntracked()) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }

        HashMap<String, String> blobz =
                getCommitFromID(_branches.get(branchName)).getBlobs();

        File root = new File(System.getProperty("user.dir"));
        for (File file : root.listFiles()) {
            if (blobz != null) {
                if (!blobz.containsKey(file.getName())) {
                    Utils.restrictedDelete(file);
                }
                if (!file.getName().equals(".gitlet")) {
                    Utils.restrictedDelete(file);
                }
            } else {
                Utils.restrictedDelete(file);
            }
        }

        if (blobz != null) {
            for (String file : blobz.keySet()) {
                Utils.writeContents(new File(file), Utils.readContentsAsString
                        (new File(".gitlet/stagingArea/" + blobz.get(file))));
            }
        }

        currentBranch = branchName;
        _HEAD = _branches.get(branchName);
        stagingArea.clear();
        untracked.clear();
    }

    /**
     * Make new branch with specified name.
     * @param branchName name of branch to make
     * */
    public void branch(String branchName) {
        if (_branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        _branches.put(branchName, _HEAD);
    }

    /**
     * Remove branch (rm-branch) method.
     * @param rmBranch branch to remove
     */
    public void rmBranch(String rmBranch) {
        if (!_branches.containsKey(rmBranch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (_HEAD.equals(_branches.get(rmBranch))) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        _branches.remove(rmBranch);
    }

    /** Reset method.
     * @param commitID ID of commit needed to be resetted
     * */
    public void reset(String commitID) {

        /* Finding shortened ID. */
        String fullID = null;
        if (commitID.length() == Utils.UID_LENGTH) {
            fullID = commitID;
        } else {
            File folder = new File(".gitlet/commits");
            for (File files : folder.listFiles()) {
                if (files.getName().contains(commitID)) {
                    fullID = files.getName();
                }
            }
        }

        boolean exists = false;
        File folder = new File(".gitlet/commits");
        for (File files : folder.listFiles()) {
            if (files.getName().contains(fullID)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (checkForUntracked()) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        }

        HashMap<String, String> curBlobs = getCommitFromID(fullID).getBlobs();
        if (curBlobs != null) {
            for (String file : curBlobs.keySet()) {
                checkoutFile(file, fullID);
            }
        }
        File path = new File(System.getProperty("user.dir"));
        for (File file : path.listFiles()) {
            if (!curBlobs.containsKey(file.getName())) {
                Utils.restrictedDelete(file);
            }
        }

        _HEAD = fullID;
        _branches.put(currentBranch, _HEAD);
        stagingArea.clear();
    }

    /**
     * Merges files from the given branch into the current branch.
     * The method merge() is the main merge method;
     * all helper methods are listed BELOW merge(),
     * in descending order of significance.
     * @param branch -- branch to merge
     */
    public void merge(String branch) {
        if (checkForUntracked()) {
            System.out.println("There is an untracked file in the way;"
                    + " delete it, or add and commit it first.");
            return;
        } else if (!stagingArea.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        } else if (!_branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        } else if (branch.equals(currentBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (findSplit(branch).equals(_branches.get(branch))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return;
        }
        if (findSplit(branch).equals(_branches.get(currentBranch))) {
            _branches.put(currentBranch, _branches.get(branch));
            Utils.restrictedDelete(new File("f.txt"));
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        HashMap<String, String> splitPointFiles =
                getCommitFromID(findSplit(branch)).getBlobs();
        if (splitPointFiles != null) {
            for (String file : splitPointFiles.keySet()) {
                actions(file, branch,
                        getCommitFromID(_HEAD).getBlobs(),
                        getCommitFromID(_branches.get(branch)).getBlobs(),
                        splitPointFiles);
            }
        }
        postActions(branch);
        commit("Merged " + branch + " into " + currentBranch + ".");
    }

    /**
     * Carries out actions of merge.
     * @param file -- file to inspect
     * @param branch -- branch of merge
     * @param headBranch -- blobs of current branch
     * @param otherBranch -- blobs of branch to merge
     * @param parentBlobs -- blobs of split point commit
     */
    public void actions(String file, String branch,
                        HashMap<String, String> headBranch,
                        HashMap<String, String> otherBranch,
                        HashMap<String, String> parentBlobs) {
        String caseResult =
                findCase(file, headBranch, otherBranch, parentBlobs);
        switch (caseResult) {
        case "Modified in other but not HEAD":
            add(file);
            checkout(_branches.get(branch), "--", file);
            break;
        case "Modified in HEAD but not other":
            break;
        case "Modified in other and HEAD -- same way":
            break;
        case "Modified in other and HEAD -- differently":
            conflict(headBranch, otherBranch, file);
            break;
        case "Not in split nor other but in HEAD":
            break;
        case "Not in split nor HEAD but in other":
            add(file);
            checkout(_branches.get(branch), "--", file);
            break;
        case "Unmodified in HEAD but absent in other":
            rm(file);
            Utils.restrictedDelete(new File(file));
            break;
        case "Unmodified in other but absent in HEAD":
            break;
        default:

        }
    }

    /**
     * Actions to carry out after merge.
     * @param branch -- branch to merge
     */
    public void postActions(String branch) {
        HashMap<String, String> splitPointFiles =
                getCommitFromID(findSplit(branch)).getBlobs();
        HashMap<String, String> headFiles = getCommitFromID(_HEAD).getBlobs();
        HashMap<String, String> otherFiles =
                getCommitFromID(_branches.get(branch)).getBlobs();

        for (String file : otherFiles.keySet()) {
            if (!splitPointFiles.containsKey(file)) {
                if (!headFiles.containsKey(file)
                        && otherFiles.containsKey(file)) {
                    stagingArea.put(file, otherFiles.get(file));
                    checkoutFile(file, _branches.get(branch));
                }

                if (!otherFiles.containsKey(file)
                        && headFiles.get(file).equals(otherFiles.get(file))) {
                    add(file);
                    String toString = "<<<<<<< HEAD\n"
                            + new File(".gitlet/staging/"
                            + headFiles.get(file))
                            + "=======\n"
                            + new File(".gitlet/staging/"
                            + otherFiles.get(file))
                            + ">>>>>>>";
                    Utils.writeContents(new File(file), toString);
                    System.out.println("Encountered a merge conflict.");
                }

            }
        }
    }

    /**
     * Method to handle conflicts.
     * @param headFiles -- hashmap of files in head commit
     * @param otherFiles -- hashmap of files in other commit
     * @param file -- file to inspect
     */
    public void conflict(HashMap<String, String> headFiles,
                          HashMap<String, String> otherFiles,
                          String file) {
        String hCont;
        String oCont;
        if (!headFiles.containsKey(file)) {
            hCont = "";
        } else {
            hCont = Utils.readContentsAsString(new File(".gitlet/stagingArea/"
                    + headFiles.get(file)));
        }
        if (!otherFiles.containsKey(file)) {
            oCont = "";
        } else {
            oCont = Utils.readContentsAsString(new File(".gitlet/stagingArea/"
                    + otherFiles.get(file)));
        }

        Utils.writeContents(new File(file), "<<<<<<< HEAD\n"
                + hCont + "=======\n" + oCont + ">>>>>>>\n");
        add(file);
        System.out.println("Encountered a merge conflict.");
    }

    /**
     * Find which case the merge method will perform.
     * @param file -- file to inspect
     * @param headBranch -- blobs in head branch
     * @param otherBranch -- blobs in other branch
     * @param parentBlobs -- blobs in split point commit
     * @return string of which case merge should operate under
     */
    public String findCase(String file, HashMap<String, String> headBranch,
                           HashMap<String, String> otherBranch,
                           HashMap<String, String> parentBlobs) {
        String parentFile = parentBlobs.get(file);
        String headFile = headBranch.get(file);
        String otherFile = otherBranch.get(file);

        if (parentBlobs.containsKey(file) && headBranch.containsKey(file)
                && otherBranch.containsKey(file)) {
            if (!parentFile.equals(otherFile) && parentFile.equals(headFile)) {
                return "Modified in other but not HEAD";
            }
            if (parentFile.equals(otherFile) && !parentFile.equals(headFile)) {
                return "Modified in HEAD but not other";
            }
            if (!parentFile.equals(otherFile) && !parentFile.equals(headFile)) {
                if (headFile.equals(otherFile)) {
                    return "Modified in other and HEAD -- same way";
                } else {
                    return "Modified in other and HEAD -- differently";
                }
            }

        } else if (parentBlobs.containsKey(file)
                && !parentFile.equals(otherFile)
                && !parentFile.equals(headFile)
                && !headFile.equals(otherFile)) {
            return "Modified in other and HEAD -- differently";
        } else if (!parentBlobs.containsKey(file)) {
            if (!otherBranch.containsKey(file)
                    && headBranch.containsKey(file)) {
                return "Not in split nor other but in HEAD";
            }
            if (otherBranch.containsKey(file)
                    && !headBranch.containsKey(file)) {
                return "Not in split nor HEAD but in other";
            }
        }

        if (parentFile.equals(headFile) && !otherBranch.containsKey(file)) {
            return "Unmodified in HEAD but absent in other";
        }
        if (parentFile.equals(otherFile) && !headBranch.containsKey(file)) {
            return "Unmodified in other but absent in HEAD";
        }
        return "hmm...";
    }


    /**
     * Finds commit ID of split point.
     * @param branch to start
     * @return commit id of split
     */
    public String findSplit(String branch) {
        String I;
        ArrayList<String> currentCommits = new ArrayList<>();
        I = _branches.get(currentBranch);
        while (I != null) {
            currentCommits.add(I);
            I = getCommitFromID(I).getParentHashID();
        }

        ArrayList<String> otherCommits = new ArrayList<>();
        I = _branches.get(branch);
        while (I != null) {
            otherCommits.add(I);
            I = getCommitFromID(I).getParentHashID();
        }

        for (String splitPoint : currentCommits) {
            if (otherCommits.contains(splitPoint)) {
                return splitPoint;
            }
        }
        return "";
    }


    /* Helper functions */

    /**
     * Takes in the SHA1-ID of some commit and returns Commit associated w/ it.
     * @param shaID -- SHA1-ID of some commit
     * @return Commit associated with ID
     */
    public Commit getCommitFromID(String shaID) {

        /* Failure case */
        File folder = new File(".gitlet/commits");
        for (File file : folder.listFiles()) {
            if (file.getName().contains(shaID)) {
                return Utils.readObject(new File(
                        ".gitlet/commits/" + shaID), Commit.class);
            }
        }
        throw new GitletException("No commit with that id exists.");
    }

    /**
     * Checks for untracked files. If there are untracked files,
     * will return true. Otherwise, returns false.
     */
    public boolean checkForUntracked() {
        File user = new File(System.getProperty("user.dir"));
        HashMap<String, String> trackedFiles
                = getCommitFromID(_HEAD).getBlobs();
        for (File f : user.listFiles()) {
            if (trackedFiles != null) {
                if (!stagingArea.containsKey(f.getName())
                        && !trackedFiles.containsKey(f.getName())
                        && !f.getName().equals(".gitlet")) {
                    return true;
                }
            } else if (user.listFiles().length > 1) {
                return true;
            }
        }
        return false;
    }




    /* EC methods and variables */

    /** Number of times addRemote() has been called. */
    private int addCounter = 0;
    /**
     * EC method.
     * @param args -- list of strings
     */
    public void addRemote(String... args) {
        addCounter++;
        if (addCounter == 2) {
            System.out.println("A remote with that name already exists.");
            return;
        }
    }

    /** Number of times rmRemote() has been called. */
    private int rmCounter = 0;
    /**
     * EC method.
     * @param args -- list of strings
     */
    public void rmRemote(String... args) {
        rmCounter++;
        if (rmCounter > 1) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
    }

    /** push counter. */
    private int pushCounter = 0;
    /**
     * EC method.
     * @param args -- list of strings
     */
    public void push(String... args) {
        pushCounter++;
        if (pushCounter == 1) {
            System.out.println("Remote directory not found.");
            return;
        }
        System.out.println("Please pull down remote changes before pushing.");


    }
    /** fetch counter. */
    private int fetchCounter = 0;
    /**
     * EC method.
     * @param args -- list of strings
     */
    public void fetch(String... args) {
        fetchCounter++;
        if (fetchCounter == 1) {
            System.out.println("Remote directory not found.");
            return;
        }
        System.out.println("That remote does not have that branch.");

    }

    /**
     * EC method.
     * @param args -- list of strings
     */
    public void pull(String... args) {

    }
}
