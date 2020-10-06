#!/bin/bash
#
# src: grails-upgrade-source@https://github.com/hbz/laser
# 2020-10-06
#

SRC=./grails2/app
DST=.

LS=de/laser
KB=com/k_int/kbplus

# mkdirs

echo "> mkdirs .."

mkdir -p $DST/grails-app/controllers/$LS
mkdir -p $DST/grails-app/domain/$LS
mkdir -p $DST/grails-app/jobs
mkdir -p $DST/grails-app/domain/org/codehaus/groovy/grails/plugins/orm/auditable
mkdir -p $DST/grails-app/services/$KB
mkdir -p $DST/grails-app/services/$LS

mkdir -p $DST/grails-app/views/layouts/
mkdir -p $DST/grails-app/views/login/
mkdir -p $DST/grails-app/views/public/
mkdir -p $DST/grails-app/views/serverCodes/

mkdir -p $DST/src/main/groovy
mkdir -p $DST/src/main/resources
mkdir -p $DST/src/main/webapp

mkdir -p $DST/src/main/groovy/$KB/traits/
mkdir -p $DST/src/main/groovy/$KB/utils/

mkdir -p $DST/src/main/groovy/$LS/api/v0/
mkdir -p $DST/src/main/groovy/$LS/base/
mkdir -p $DST/src/main/groovy/$LS/controller/
mkdir -p $DST/src/main/groovy/$LS/exceptions/
mkdir -p $DST/src/main/groovy/$LS/helper/
mkdir -p $DST/src/main/groovy/$LS/interfaces/
mkdir -p $DST/src/main/groovy/$LS/oai/
mkdir -p $DST/src/main/groovy/$LS/quartz/
mkdir -p $DST/src/main/groovy/$LS/traits/
mkdir -p $DST/src/main/groovy/$LS/usage/
mkdir -p $DST/src/main/groovy/$LS/web/
mkdir -p $DST/src/main/groovy/org/gokb/

mkdir -p $DST/grails-app/assets/vendor

# copy sources

echo "> copying sources: [ $SRC ] -> [ $DST ]"

cp -r $SRC/grails-app/controllers/$LS/*                 $DST/grails-app/controllers/$LS/.

cp -r $SRC/grails-app/domain/$KB/*                      $DST/grails-app/domain/$KB/.
cp -r $SRC/grails-app/domain/$LS/*                      $DST/grails-app/domain/$LS/.

cp -r $SRC/grails-app/i18n/*                            $DST/grails-app/i18n/.

cp -r $SRC/grails-app/services/$KB/*                    $DST/grails-app/services/$KB/.
cp -r $SRC/grails-app/services/$LS/*                    $DST/grails-app/services/$LS/.

cp -r $SRC/grails-app/taglib/*                          $DST/grails-app/taglib/.

cp -r $SRC/grails-app/views/accessMethod		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/accessPoint			$DST/grails-app/views/.
cp -r $SRC/grails-app/views/address				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/admin				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/ajax				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/announcement		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/api					    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/contact				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/costConfiguration	$DST/grails-app/views/.
cp -r $SRC/grails-app/views/dataManager			$DST/grails-app/views/.
cp -r $SRC/grails-app/views/dev					    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/doc					    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/_fields				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/finance				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/globalDataSync		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/issueEntitlement	$DST/grails-app/views/.
cp -r $SRC/grails-app/views/layouts				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/license				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/licenseCompare		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/licenseImport		$DST/grails-app/views/.
#cp -r $SRC/grails-app/views/login				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/mailTemplates		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/myInstitution		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/organisation		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/package				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/person				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/platform			  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/profile				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/public				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/readerNumber		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/search				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/serverCodes			$DST/grails-app/views/.
cp -r $SRC/grails-app/views/stats				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/subscription		$DST/grails-app/views/.
cp -r $SRC/grails-app/views/survey				  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/task				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/templates			  $DST/grails-app/views/.
cp -r $SRC/grails-app/views/tipp				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/title				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/usage				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/user				    $DST/grails-app/views/.
cp -r $SRC/grails-app/views/yoda				    $DST/grails-app/views/.

cp -r $SRC/src/groovy/$KB/traits/*              $DST/src/main/groovy/$KB/traits/.
cp -r $SRC/src/groovy/$KB/utils/*               $DST/src/main/groovy/$KB/utils/.

cp -r $SRC/src/groovy/$LS/api/v0/*              $DST/src/main/groovy/$LS/api/v0/.
cp -r $SRC/src/groovy/$LS/base/*                $DST/src/main/groovy/$LS/base/.
cp -r $SRC/src/groovy/$LS/controller/*          $DST/src/main/groovy/$LS/controller/.

cp -r $SRC/src/groovy/$LS/dbm/*                 $DST/src/main/groovy/$LS/dbm/.

cp -r $SRC/src/groovy/$LS/exceptions/*          $DST/src/main/groovy/$LS/exceptions/.
cp -r $SRC/src/groovy/$LS/helper/*              $DST/src/main/groovy/$LS/helper/.
cp -r $SRC/src/groovy/$LS/interfaces/*          $DST/src/main/groovy/$LS/interfaces/.
cp -r $SRC/src/groovy/$LS/oai/*                 $DST/src/main/groovy/$LS/oai/.
cp -r $SRC/src/groovy/$LS/quartz/*              $DST/src/main/groovy/$LS/quartz/.
cp -r $SRC/src/groovy/$LS/traits/*              $DST/src/main/groovy/$LS/traits/.
cp -r $SRC/src/groovy/$LS/usage/*               $DST/src/main/groovy/$LS/usage/.
cp -r $SRC/src/groovy/$LS/web/*                 $DST/src/main/groovy/$LS/web/.

cp -r $SRC/src/groovy/org/gokb/*                $DST/src/main/groovy/org/gokb/.

cp -r $SRC/src/java/*                           $DST/src/main/java/.

cp -r $SRC/web-app/setup                    $DST/src/main/webapp/.
cp -r $SRC/web-app/resources/*              $DST/src/main/webapp/files
cp -r $SRC/web-app/swagger                  $DST/src/main/webapp/.

cp -r $SRC/web-app/css/*                    $DST/grails-app/assets/stylesheets/.
cp -r $SRC/web-app/images/*                 $DST/grails-app/assets/images/.
cp -r $SRC/web-app/js/*                     $DST/grails-app/assets/javascripts/.

cp -r $SRC/web-app/semantic                 $DST/grails-app/assets/themes
cp -r $SRC/web-app/vendor/*                 $DST/grails-app/assets/vendor/.

# specials

echo "> copying specials .."

cp -v $SRC/grails-app/conf/UrlMappings.groovy  $DST/grails-app/controllers/$LS/UrlMappings.groovy

# cleanup

echo "> cleanup .."

rm $DST/src/main/java/de/uni_freiburg/ub/IpAddressTest.java
rm $DST/src/main/java/de/uni_freiburg/ub/IpRangeCollectionTest.java
rm $DST/src/main/java/de/uni_freiburg/ub/IpRangeTest.java

rm $DST/src/main/groovy/de/laser/interfaces/AuditableSupport.groovy
rm $DST/src/main/groovy/de/laser/controller/AbstractDebugController.groovy

rm -r $DST/grails-app/domain/$KB/auth # old auth

# issues

currentDate=`date +"%Y-%m-%d %T"`

echo ""
echo "> finished: $currentDate"
echo ""
echo "> known issues:"
echo "- fix code signed with: grails-3-fix"
echo "- remove multiple <asset:deferredScripts/>"
echo "- update de.laser.auth.*"
