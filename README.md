#README.md

This is the README for the Blackboard Downloader. This program will prompt for a username and password for blackboard and will download all documents off Blackboard. It structures the downloads in the same fashion that blackboard does, so the file for your syllabus will be under the 'Syllabus' folder, the assignments from the year will be under the 'Assignments' folder, etc. It will only go to a depth of one sub-folder. For instance, if you navigate to Course Content and have to click to another folder to get to the documents, it will download all of the files in there, but it will NOT go any deeper than that. 

##Creator
- Lucas Bruder
- lbruder827@gmail.com
- Last modified: May 9, 2015

##Contents
- Downloader.java (in src/)
- login_info.txt
- README.md
- BlackboardDownloader.jar

##Features
- Downloads all documents off blackboard
- Structures the files in the same format as blackboard. ie: Syllabus, Assignments, Course Content, etc.
- Safe storage of username and password by deleting the file contents after being read
- Downloads all files up to 1 subfolder depth

##Requirements
- Java (http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
- Google Chrome (Download @ https://www.google.com/chrome/browser/desktop/)
- Internet connection
- Blackboard account
- All of your courses MUST be visible on the blackboard homepage under the 'My Course' section
- Your default download location MUST be the downloads folder (should be by default unless you changed it)
-- To change it back, go to Chrome > Preferences. Click "Show Advanced Settings" at the bottom. Scroll down to 'Downloads' and ensure the location is /Users/username/Downloads

##How to use
1. Open the login_info.txt file. On the first line, erase "username" and put in your blackboard username. On the second line, erase "password" and enter in your password". On the third line, erase the "number 1-5" and put a number between 1 and 5. This determines how long it will wait before clicking stuff. If you have a slow internet connection, you want to choose a number closer to 5. If you have a fast internet connection, use a number like 1 or 2.
2. Navigate to the folder that contains the 'Downloader.jar' file in Terminal/Console and type in java -jar Downloader.jar. Instructions for mac Search for terminal. Type in 'cd Downloads/BlackboardDownloader-master/'. After doing step number 1, type in 'java -jar BlackboardDownloader.jar'. Instructions for PC: not sure yet
3. Don't interfere with the web browser or download any documents while the program is running. There is a high chance it will open up new tabs, but you can either leave those open or close them, but DO NOT close the main tab.

##Errors
- If at any point you receive an error along the lines of "Unsupported major.minor version...", update to the newest version of Java given in the link under "Requirements" section above.

##Notes
- Sometimes when clicking on a link, it will open new tabs and you won't be able to see what is happening. You can either let it be or close those tabs.
