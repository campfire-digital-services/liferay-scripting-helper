# Liferay Scripting Helper Portlet

*liferay-scripting-helper-portlet*

Liferay Scripting Helper is an improved interface to scripting in Liferay. It features a codemirror editor, error trace reporting and an ability to save/load scripts and import/export them as zip files.

## Supported Products

* Liferay Portal 6.1 CE GA2 (6.1.1+)
* Liferay Portal 6.1 EE GA2 (6.1.20+)


## Downloads

The latest releases are available from [SourceForge](http://sourceforge.net/projects/permeance-apps/files/liferay-scripting-helper/ "Liferay Scripting Helper").

Liferay Marketplace submission pending.


## Usage

Administrators will see a "Scripting Helper" portlet in the Server area of the Control Panel.
Other users can also be assigned permissions to see the Scripting Helper Portlet.

![Scripting Helper Portlet](/docs/images/scripting-helper-1.png "Scripting Helper Portlet")

The user can run the script, see any script output or script error traces, save the script, save/load a script, export the saved scripts as a zip file, and import it into another Liferay installation.

The [codemirror](http://codemirror.net) library is utilized as an editor, and supports a range of languages and editor themes.



## Building

Step 1. Checkout source from GitHub project

    % git  clone  https://github.com/permeance/liferay-scripting-helper

Step 2. Build and package

    % mvn  -U  clean  package

This will build "liferay-scripting-helper-portlet-XXX.war" in the targets tolder.

NOTE: You will require JDK 1.6+ and Maven 3.


## Installation

### Liferay Portal + Apache Tomcat Bundle

eg.

Deploy "liferay-scripting-helper-portlet-1.0.0.0.war" to "LIFERAY_HOME/deploy" folder.


## Plugin Security

This app will be released as a Marketplace application without Plugin Security enabled. 

While we have provided a list of plugin security entries to run the basic scripting helper, the administrator would need to add additional entries for any script functions/services that might be called. As such, the scripting helper is not really helpful unless it can be installed without plugin security.


## Support for Liferay 6.1 GA 1 (6.1.0, 6.1.10)

The source code supports Liferay 6.1 GA1 but needs to be recompiled with those libraries, due to a method signature change in ScriptingUtil. Check out the source from the GitHub project, and in the pom.xml, change liferay.version to 6.1.0, and rebuild the project. The resultant WAR can be deployed to a Liferay 6.1 GA1 instance.


## License

This application is released under the GNU Public License version 3.0 (GPL). The codemirror library is also included in the package and comes under a MIT-style license. 


## Project Team

* Chun Ho - chun.ho@permeance.com.au
