#! /bin/sh

# This script must be run from the project root directory
#
# Release Process.
#
# 1.  Do clean check out of source from svn and note revision number.
# 2.  Switch to JDK 1.4 and run "mvn compile" in core.
# 3.  Set the version number in the pom.xml files of all the modules.
# 3a. If doing a release rather than snapshot build, run "find . -name pom.xml | xargs grep SNAPSHOT" and make sure
#     there are no important snapshot dependencies.
# 3b. Set the same version number in this script.
# 4.  Set the correct spring version number in the pom.xml.
# 4a. Make sure there are no snapshot dependencies in the release.
# 4b. Remove any references to external maven repositories in the parent pom.xml (remove <repositories> element).
# 4c. Check that all dependencies are downloadable from maven central repo.
# 5.  Run this script to generate the artifacts and web site in the 'release' directory.
# 6.  Copy the archives and unpack them to check the contents.
# 7.  The archives are tar archives. Create zip versions from the contents and check the internal paths are Ok.
# 8.  Check the site looks Ok.
# 9.  Check the reference guide links in the site are valid and that images are shown and paths in HTML are relative.
# 10. Deploy the contacts and tutorial sample apps in Jetty and Tomcat and check they work.
# 11. Check there have been no further commits since checkout (svn update). If there have, go to 1.
# 12. Commit the source with the changed version numbers and note the revision number (should be 'build revision' + 1).
# 13. Label/tag the source tree with the release version number.
# 14. Update the pom file versions to the appropriate snapshot version, do a grep to make sure none have been missed
#     and commit them.
# 15. Upload the site to acegisecurity.org (or wherever).
# 16. scp the release archives to shell.sf.net. Check md5 matches to make sure transfer was OK.
# 17. ftp them to the sourceforge upload server, uploads.sourceforge.net.
# 18. Put the jars, poms and signature files into the acegi repository for synchronization.
#
# (alternatively, the maven release plugin takes care of a lot of this stuff).

########################################################################################################################
#
# Edit this release number before running. It is used to check jar names etc.
#
########################################################################################################################

RELEASE_VERSION=1.2.2-SNAPSHOT

# Project Name. Used for creating the archives.
PROJECT_NAME=spring-ldap

PROJ_DIR=`pwd`;
RELEASE_DIR=$PROJ_DIR/$PROJECT_NAME-$RELEASE_VERSION
DOCS_DIR=$RELEASE_DIR/docs
SITE_DIR=target/site
DIST_DIR=dist

echo "** Project directory is $PROJ_DIR"

SVN_REV=`svn info $PROJ_DIR | grep Revision | sed "s/Revision: //"`

echo "** Building from revision $SVN_REV"

########################################################################################################################
#
# Create the release directory
#
########################################################################################################################

if [[ -e $RELEASE_DIR ]]
then
   echo "Deleting $RELEASE_DIR."
   rm -Rf $RELEASE_DIR
fi

if [[ -e $DIST_DIR ]]
then
   echo "Deleting $RELEASE_DIR."
   rm -Rf $DIST_DIR
fi

mkdir $RELEASE_DIR
mkdir $DOCS_DIR
mkdir $DIST_DIR

########################################################################################################################
#
# run maven to generate jars
#
########################################################################################################################

mvn clean install -DcreateChecksum=true

if [ "$?" -ne 0 ]
then
  echo "mvn install failed"
  exit 1;
fi

########################################################################################################################
#
# Generate Maven Web Site and Process Docbook Source.
#
########################################################################################################################

echo "** Generating site".

mvn site

if [ "$?" -ne 0 ]
then
  echo "mvn site generation failed"
  exit 1;
fi

cp -r $SITE_DIR/apidocs $SITE_DIR/reference $DOCS_DIR 
########################################################################################################################
#
# Patch the module site files to point to the root css files, change names of oversized menus,
# remove dodgy standard maven text etc.
#
########################################################################################################################

pushd $SITE_DIR

find . -maxdepth 2 -mindepth 2 -name "*.html" | xargs perl -i -p -e 's#"\./css/#"\.\./css/#;' \
   -e 's/Maven Surefire Report/Unit Tests/;' \
   -e 's/Cobertura Test Coverage/Test Coverage/;' \
   -e 's/A successful project.*greatly appreciated\.//;'

find . -maxdepth 3 -mindepth 3 -name "*.html" | xargs perl -i -p -e 's#"\./css/#"\.\./\.\./css/#;'

popd

########################################################################################################################
#
# Assemble the required jar files, make sure there are the expected number and produce signatures.
#
########################################################################################################################

pushd core
mvn assembly:single
popd

pushd tiger
mvn assembly:single
popd

cp -r core/target/spring-ldap-${RELEASE_VERSION}-assembly.dir/* $RELEASE_DIR
cp -r tiger/target/spring-ldap-tiger-${RELEASE_VERSION}-assembly.dir/* $RELEASE_DIR

########################################################################################################################
#
# Build the release archives.
#
########################################################################################################################

# Get rid of mac DS_Store files.

find . -name .DS_Store -exec rm "{}" ";"

cp notice.txt readme.txt license.txt $RELEASE_DIR

zip -r $DIST_DIR/${PROJECT_NAME}-bin-${RELEASE_VERSION}.zip ${PROJECT_NAME}-${RELEASE_VERSION} -x ${PROJECT_NAME}-${RELEASE_VERSION}/lib/ ${PROJECT_NAME}-${RELEASE_VERSION}/lib/* 
zip -r $DIST_DIR/${PROJECT_NAME}-bin-with-dependencies-${RELEASE_VERSION}.zip ${PROJECT_NAME}-${RELEASE_VERSION}
