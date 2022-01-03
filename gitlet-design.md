# Gitlet Design Document

**Kaito Garcia**:

## Classes and Data Structures

### Main
* Driver class
* Will take in input from terminal and interpret the command
* Takes care of basic failure scenarios:
  * If a user doesn't input any arguments, print the message Please enter a command. and exit.
  * If a user inputs a command that doesn't exist, print the message No command with that name exists. and exit.
  * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
  * If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory), but is not in such a directory, print the message Not in an initialized Gitlet directory.

### Commit
#### Instance variables
* String message -- commit message
* String timestamp -- time & date data
* String hashID -- commit's SHA1-ID
* String parentHashID -- commit parent's SHA1-ID
* HashMap<String, String> blob -- <file name, SHA1> data of blob, with file name as key and SHA1 as contents
#### hasher method
* Takes in Commit obj
* Uses all data from Commit obj to generate unique SHA-1 ID
* 

### Repo
* Holds all methods for commands
* Catches all failure cases in each method

## Algorithms



## Persistence

