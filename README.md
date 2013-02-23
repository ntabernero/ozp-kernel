Ozone Kernel [![Build Status](https://travis-ci.org/ntabernero/ozp-kernel.png)](https://travis-ci.org/ntabernero/ozp-kernel)
======================

This is the Kernel module of the Ozone Platform.  It is a sub-module of the ozoneplatform project.
It packages OSGI bundles from other sub-modules into a runnable container.  The easiest way to build and work with this
is to get the entire ozoneplatform project, https://github.com/ozoneplatform/ozoneplatform.git, and follow
that project's README.md for building and running the system.

If you have a specific need and you understand the technical ramifications, you can build just this sub-module.
* Install all the prerequisites mentioned in the ozoneplatform project's README.md.
* Run `mvn clean install`

This will collect OSGI bundles available on OWF's Nexus repository and package them into a runnable container.
To run the container
* Unpack the zip file kernel/deployment/target/deployment-XXX.zip
* Run deployment-XXX/apache-tomcat-7.0.32/start.bat

