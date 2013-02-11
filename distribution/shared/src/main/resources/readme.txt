#
# Copyright 2013 Next Century Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

OzonePlatform
-------------------

Security Prerequisites
----------------------
By default, no users are defined for the container. You can run the container in the
foreground in this case, but you will not be able to access the container remotely and you will
not be able to run it in the background.

To enable remote access to the container, you must create at least one user in
the ./etc/users.properties file. It is recommended that you create at least one user
with the admin role by adding a line with the following syntax:

<Username>=<Password>,admin

The admin role grants full administration privileges to the user.

To make the container's JMX port accessible, modify the following lines of the
./etc/system.properties file to use the credentials of one of the users from the
users.properties file:

webconsole.jmx.user=<Username>
webconsole.jmx.password=<Password>

To make the ActiveMQ Web console accessible, add the following lines to the
./etc/system.properties file, using the credentials of one of the users from the
users.properties file:

webconsole.jms.user=<Username>
webconsole.jms.password=<Password>

Quick Start
-----------
To start Fuse ESB Enterprise ${project.version}, run 'bin/fuseesb' on 
Linux/Unix or 'bin\fuseesb.bat' on Windows.

Examples
-------------
Examples with instructions are in the 'examples' directory.

Documentation
-------------
You can find documentation online at:
http://fusesource.com/documentation/fuse-esb-enterprise-documentation

