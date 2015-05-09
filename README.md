#README.md

This is the README for the Blackboard Downloader. This program will prompt for a username and password for blackboard and will download all documents off Blackboard. It structures the downloads in the same fashion that blackboard does, so the file for your syllabus will be under the 'Syllabus' folder, the assignments from the year will be under the 'Assignments' folder, etc. It will only go to a depth of one sub-folder. For instance, if you have Course Content -> HW -> HW Solutions, it will download everything in the HW folder, but won't download anything in the HW Solutions folder. Most course layout seems to be Course Content -> HW Solutions -> ..files.., so this is what it was written for. 

##Creator
- Lucas Bruder
- lbruder827@gmail.com
- Last modified: May 9, 2015

##Features
- Downloads all documents off blackboard
- Structures the files in the same format as blackboard. ie: Syllabus, Assignments, Course Content, etc.

##Requirements
- Java
- Google Chrome (Download @ https://www.google.com/chrome/browser/desktop/)
- Internet connection
- Blackboard account
- All of your courses MUST be visible on the blackboard homepage under the 'My Course' section

##How to use
1. Enter in your username and password into the login_info.txt file like below:
username
password
Right now, this is how the file looks, so erase username and password and enter in your own. This will be erased as soon as it's used, so keep this in mind when you are attempting to login multiple times.
2. 