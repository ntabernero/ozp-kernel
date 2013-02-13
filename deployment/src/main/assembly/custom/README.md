OzonePlatform Distribution 
======================

This is the distribution of the Ozone Platform.  This distribution is packaged 
inside a standalone version of Apache Tomcat.

This is the first alpha release of OWF 8 and is pretty much non-functional.  The release's main
goal was to build out the build and test infrastructure to be able to produce a release at all.  


Setup
-----
1. Unzip package

Running
--------
1. Run start.bat in the apache-tomcat-7.0.32 directory.
2. To view Karaf OSGI console, go to http://localhost:8181/system/console and login using username: "karaf", password: "karaf"
3. To view website, go to http://localhost:8181/owf/index_debug.html
4. Alpha release 3 introduces several different layout capabilities, but as OWF8 does not have the ability to switch between dashboards.  To see the layouts, hit the following URLs:
     Desktop layout - http://localhost:8181/owf/index_debug.html#guid=21a777b9-96e5-4f64-883e-8067ba99b3ee 
     Fit layout - http://localhost:8181/owf/index_debug.html#guid=31a777b9-96e5-4f64-883e-8067ba99b3ee 
     Portal layout -  http://localhost:8181/owf/index_debug.html#guid=11a777b9-96e5-4f64-883e-8067ba99b3ff 
     Two paned dashboard, one using tab layout one using fit layout - http://localhost:8181/owf/index_debug.html#guid=11a777b9-96e5-4f64-883e-8067ba99b3ee 
5. Alpha release 3 also has the first functionality of the dashboard designer, splitting a dashboard up into multiple panes.
   This can be viewed by pressing the "Create Dashboard" button in the toolbar.

Note that in Alpha release 3 changes to dashboards and their layouts will not as yet be persisted.  Soon to come!



