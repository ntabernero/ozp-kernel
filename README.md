Ozone Widget Framework
======================

Initial Setup
-------------

1. Install J2SE 6.0 SDK (or later), which can be downloaded from 
   http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html
   Use version of "JDK 6.0 Update 29" (or later).

2. Make sure that your JAVA_HOME environment variable is set to the newly installed 
   JDK location, and that your PATH includes %JAVA_HOME%\bin (windows) or 
   $JAVA_HOME$/bin (unix).

3. Install Maven 3.0.3 (or later), which can be downloaded from 
   http://maven.apache.org/download.html. Make sure that your PATH includes 
   the MVN_HOME/bin directory. 

4. Be sure to give Maven enough memory `MAVEN_OPTS=-Xmx512m -XX:MaxPermSize=128m`
   

Building
--------
1. Run `mvn clean install` from the root folder

Running
--------
1.

