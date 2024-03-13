# Liferay Scripting Helper

The Scripting Helper is an improved interface for administrators to run Groovy scripts in Liferay DXP. It features an editor, error trace reporting and an ability to save/load scripts and import/export them as zip files. Since 7.4 it's possible to launch scripts in backgroundtask and Real-time log tracking via WebSocket.


## Supported products

* Liferay DXP 7.4 

## Usage

Administrators will see a new *Scripting Helper* option in the Control Panel menu of Liferay DXP. Other users can also be assigned permissions to see the *Scripting Helper*.

![Scripting Helper](/docs/images/scripting-helper-7.3-menu.png "Scripting Helper")

The *Scripting Helper* allows you to run a Groovy script, see the output or errors. You can then save multiple scripts and export the saved scripts as a zip file to import into another Liferay installation.

![Scripting Helper](/docs/images/scripting-helper-7.4-portlet.png "Scripting Helper")

You can also schedule them

![Scripting Helper](/docs/images/scripting-helper-7.4-portlet-schedule.png "Scripting Helper")

The [codemirror](http://codemirror.net) library is utilised as the editor and supports a range of languages and editor themes.

## Building

Step 1. Check out the source from GitHub:

    % git clone https://github.com/acbenaissi/liferay-scripting-helper.git

Step 2. Build and package the module:

    % mvn -U clean package

This will create a package called `liferay-scripting-helper-portlet.jar` in the `target` tolder.

*Note: You will require JDK 8+ and Maven 3.6+.*

## Licence

This application is released under the GNU Public License version 3.0 (GPL). The codemirror library is also included in the package and comes under a MIT-style license.


