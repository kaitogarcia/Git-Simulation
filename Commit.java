package gitlet;

/* Data Structures */
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Commit object.
 * @author Kaito Garcia
 */

public class Commit implements Serializable {

    /**
     * Constructor for Commit obj.
     * @param msg -- commit message
     * @param commitParent -- commit parent's SHA1-ID
     * @param textFiles -- HashMap with <filename.txt, SHA1-ID>
     */
    public Commit(String msg, String commitParent,
                  HashMap<String, String> textFiles) {
        _message = msg;
        _parentHashID = commitParent;
        _blobs = textFiles;

        if (commitParent == null) {
            _timestamp = "Wed Dec 31 16:00:00 1969 -0800";
        } else {

            SimpleDateFormat date =
                    new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy");
            _timestamp = date.format(new Date()) + " -0800";
        }

        _hashID = hasherCommit();
    }

    /**
     * Takes in all data in commit object to convert to SHA1-ID.
     * @return Hashed commit object
     */
    public String hasherCommit() {

        String filesAsString;
        if (_blobs == null) {
            filesAsString = "";
        } else {
            filesAsString = _blobs.toString();
        }

        String tempHash;
        if (_parentHashID == null) {
            tempHash = "";
        } else {
            tempHash = _parentHashID;
        }

        return Utils.sha1(_message, tempHash,
                _timestamp, filesAsString);
    }


    /* Getter methods */

    /**
     * Get commit message.
     * @return commit message
     */
    public String getMessage() {
        return _message;
    }

    /**
     * Getter method for timestamp.
     * @return timestamp
     */
    public String getTimestamp() {
        return _timestamp;
    }

    /**
     * Getter method for hash ID.
     * @return Hash ID
     */
    public String getHashID() {
        return _hashID;
    }

    /**
     * Getter method for parent ID.
     * @return parent ID
     */
    public String getParentHashID() {
        return _parentHashID;
    }

    /**
     * Getter method for blobs in commit.
     * @return blobs
     */
    public HashMap<String, String> getBlobs() {
        return _blobs;
    }


    /* Instance Variables */
    /** Commit message. */
    private String _message;

    /** Timestamp of creation. */
    private String _timestamp;

    /** String of commit's SHA1-ID. */
    private String _hashID;

    /** String of commit's PARENT'S SHA1-ID. */
    private String _parentHashID;

    /** HashMap with filename as key and SHA1 as contents (<fileName, SHA1>). */
    private HashMap<String, String> _blobs;

}
