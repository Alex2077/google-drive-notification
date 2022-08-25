# Google Drive Notification Plugin

This is a plugin for Rundeck >= 3.4.0. It helps to upload a file to google drive with Rundeck community edition

## Build
Run `./gradlew build` to build the jar file

## Install
Copy the file `google-drive-notification-plugin-<version>.jar` file to the `$RDECK_BASE\libext` folder

## Configuration
To configure this plugin you need add these parameters:

* `Credentials file path` - Location of the json file that contains the data of the google service account.
* `Folder ID` - This unique identifier for objects in Google Drive can be found in the URL when you open an folder.
* `File to upload` - Path where the file to upload is located.

## Tips
To create a google service account: https://www.youtube.com/watch?v=gb0bytUGDnQ
In addtion, you must share on google drive folder the service account to make it work.

