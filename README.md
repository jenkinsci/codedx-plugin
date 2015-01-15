codedx-jenkins-plugin
=====================

A Code Dx plugin for Jenkins


To compile and run use the following commands in the root directory:

**Windows**

```bat
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
mvn hpi:run
```

**Mac/Linux**
```sh
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn hpi:run
```
Mac users should ensure that their JAVA_HOME environment variable points to a Java 7 version of the JDK. If this isn't done explicitly, the mvn script will use the directory pointed to by the `/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK` symlink, which is the latest Apple JDK installed and isn't overwritten by Oracle's JDK installer.

To package the plugin run:

```sh
mvn package

```

To install the package, use the Jenkins web interface.

**Releasing to Jenkins plugin repository (for end-users to install easily within Jenkins)**

I had some difficulties releasing this plugin.  Below are the steps I ended up needing to take (on windows).  

* Sign up for a Jenkins account (https://jenkins-ci.org/account/signup).   
* Have git bash installed.  You will want to do the release using git bash. 
* Have the code code checked out and ready to be released.
* Edit your Maven settings.xml file (in C:\users\YOUR_USERNAME\.m2\ on Windows) for  so that it contains the following:


```
  <servers>
    <server>
      <id>maven.jenkins-ci.org</id> <!-- For parent 1.397 or newer; before this use id java.net-m2-repository -->
      <username>YOUR_JENKINS_USERNAME</username>
      <password>YOUR_JENKINS_PASSWORD</password>
    </server>
  </servers>
  
  ```
  
  
* Follow the instructions here: https://help.github.com/articles/generating-ssh-keys/ to generate an SSH key and add to github (if you don't already have this in place).  Make sure you have an ssh agent started and thet you add your key.
* Run the following commands to prepare and perform the release.  You will be prompted about the new version number.

```
mvn org.apache.maven.plugins:maven-release-plugin:2.5:prepare
mvn org.apache.maven.plugins:maven-release-plugin:2.5:perform

```

It is important to use 2.5 because some prior versions of the maven-release-plugin have issues that cause things to fail silently.
