# cn1-deploy
CLI tool for deploying Codename One applications

## Installation

~~~~
$ npm install cn1-deploy -g
~~~~

NOTE:  On unix/linux/Mac systems you'll need to use `sudo` for the command.  On Windows this command should work as long as your are using an admin account.

## Usage

Open terminal and navigate to a Codename One project directory.

**Initializing a Desktop Project**

~~~~
$ cn1-deploy init javase
~~~~

This creates a folder "cn1-deployments/javase" with a package.json file with all of the settings necessary to build and deploy the app for Desktops.

**Building the App for Desktop**

~~~~
$ cn1-deploy build javase
~~~~

The first time this is run, this will submit a job to the Codename One build server to generate the desktop app.  When complete, the app you will have an executable jar ready to run in "cn1-deployments/javase/dist".

**Installing the App on Local Machine**

~~~~
$ cn1-deploy install javase
~~~~

This will bundle the app and make it so it can be run on the command line of the local machine when the user enters the command name.

**Publishing App to NPM**

~~~~
$ cn1-deploy publish javase
~~~~

This will publish your app on NPM so that it can be installed on any machine that has NodeJS installed via the one-line command:

~~~~
$ npm install your-app-name
~~~~

and then will be runnable immediately via:

~~~~
$ your-command-name
~~~~

