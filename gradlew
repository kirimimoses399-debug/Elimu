#!/bin/sh

#
# Copyright 2015 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
app_path=$0

# Need this for daisy-chained symlinks.
while
    APP_HOME=${app_path%"${app_path##*/}"}
    [ -h "$app_path" ]
do
    APP_HOME=$( cd "$APP_HOME" && pwd -P ) || exit
    app_path=$( readlink "$app_path" ) || exit
done

APP_HOME=$( cd "${APP_HOME%.}" && pwd -P ) || exit

APP_NAME="Gradle"
APP_BASE_NAME=${0##*/}

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='" -Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != "max".
MAX_FD=maximum

warn () {
    echo "$*" >&2
}

die () {
    echo
    echo "$*" >&2
    exit 1
}

# OS specific support (must be 'true' or 'false').
if [ "$(uname)" != Linux ] ; then
    case "$(uname)" in
      CYGWIN* )
        cygwin=true
        ;;
      Darwin* )
        darwin=true
        ;;
      MSYS* | MINGW* )
        msys=true
        ;;
      NONSTOP* )
        nonstop=true
        ;;
    esac
fi

if [ "$darwin" = true ] && [ "$JAVA_HOME" = "" ] && [ -d /usr/libexec/java_home ]; then
    export JAVA_HOME=$(/usr/libexec/java_home)
fi

if [ "$JAVA_HOME" != "" ] ; then
    if [ ! -x "$JAVA_HOME/bin/java" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
    fi
else
    JAVA_CMD=java
fi

[ -z "$JAVA_CMD" ] || exec "$JAVA_CMD" $JAVA_OPTS $GRADLE_OPTS "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
