#!/bin/bash
#
# src: grails-upgrade-source@https://github.com/hbz/laser
# 2020-10-06
#

SRC=./grails2/app
DST=.

LS=de/laser
KB=com/k_int/kbplus

# moving sources

echo "> moving sources: [ $SRC ] -> [ $DST ]"

git mv ./grails2/app/grails-app/controllers/de/laser/*      ./grails-app/controllers/de/laser

mkdir -p ./grails-app/domain/com/k_int/kbplus

git mv ./grails2/app/grails-app/domain/com/k_int/kbplus/*.groovy    ./grails-app/domain/com/k_int/kbplus

git mv ./grails2/app/grails-app/domain/de/laser/finance     ./grails-app/domain/de/laser
git mv ./grails2/app/grails-app/domain/de/laser/oap         ./grails-app/domain/de/laser
git mv ./grails2/app/grails-app/domain/de/laser/properties  ./grails-app/domain/de/laser
git mv ./grails2/app/grails-app/domain/de/laser/system      ./grails-app/domain/de/laser
git mv ./grails2/app/grails-app/domain/de/laser/titles      ./grails-app/domain/de/laser

git mv ./grails2/app/grails-app/domain/de/laser/*.groovy    ./grails-app/domain/de/laser

git mv ./grails2/app/grails-app/jobs/*        ./grails-app/jobs

git mv ./grails2/app/grails-app/services/*    ./grails-app/services

git mv ./grails2/app/grails-app/taglib/*      ./grails-app/taglib

git mv ./grails2/app/grails-app/views/accessMethod      ./grails-app/views
git mv ./grails2/app/grails-app/views/accessPoint       ./grails-app/views
git mv ./grails2/app/grails-app/views/address           ./grails-app/views
git mv ./grails2/app/grails-app/views/admin             ./grails-app/views
git mv ./grails2/app/grails-app/views/ajax              ./grails-app/views
git mv ./grails2/app/grails-app/views/announcement		  ./grails-app/views
git mv ./grails2/app/grails-app/views/api					      ./grails-app/views
git mv ./grails2/app/grails-app/views/compare				    ./grails-app/views
git mv ./grails2/app/grails-app/views/contact				    ./grails-app/views
git mv ./grails2/app/grails-app/views/costConfiguration	    ./grails-app/views
git mv ./grails2/app/grails-app/views/dataManager			  ./grails-app/views
git mv ./grails2/app/grails-app/views/dev					      ./grails-app/views
git mv ./grails2/app/grails-app/views/doc					      ./grails-app/views
git mv ./grails2/app/grails-app/views/_fields				    ./grails-app/views
git mv ./grails2/app/grails-app/views/finance           ./grails-app/views
git mv ./grails2/app/grails-app/views/globalDataSync      ./grails-app/views
git mv ./grails2/app/grails-app/views/issueEntitlement	  ./grails-app/views
git mv ./grails2/app/grails-app/views/layouts				    ./grails-app/views
git mv ./grails2/app/grails-app/views/license				    ./grails-app/views
git mv ./grails2/app/grails-app/views/licenseCompare		./grails-app/views
git mv ./grails2/app/grails-app/views/licenseImport		  ./grails-app/views
git mv ./grails2/app/grails-app/views/login/denied.gsp  ./grails-app/views/login
git mv ./grails2/app/grails-app/views/mailTemplates		  ./grails-app/views
git mv ./grails2/app/grails-app/views/myInstitution		  ./grails-app/views
git mv ./grails2/app/grails-app/views/organisation		  ./grails-app/views
git mv ./grails2/app/grails-app/views/package				    ./grails-app/views
git mv ./grails2/app/grails-app/views/person				    ./grails-app/views
git mv ./grails2/app/grails-app/views/platform			    ./grails-app/views
git mv ./grails2/app/grails-app/views/profile				    ./grails-app/views
git mv ./grails2/app/grails-app/views/public				    ./grails-app/views
git mv ./grails2/app/grails-app/views/readerNumber  	  ./grails-app/views
git mv ./grails2/app/grails-app/views/search				    ./grails-app/views
git mv ./grails2/app/grails-app/views/serverCodes			  ./grails-app/views
git mv ./grails2/app/grails-app/views/stats				      ./grails-app/views
git mv ./grails2/app/grails-app/views/subscription		  ./grails-app/views
git mv ./grails2/app/grails-app/views/survey				    ./grails-app/views
git mv ./grails2/app/grails-app/views/task				      ./grails-app/views
git mv ./grails2/app/grails-app/views/templates			    ./grails-app/views
git mv ./grails2/app/grails-app/views/tipp				      ./grails-app/views
git mv ./grails2/app/grails-app/views/title				      ./grails-app/views
git mv ./grails2/app/grails-app/views/usage             ./grails-app/views
git mv ./grails2/app/grails-app/views/user				      ./grails-app/views
git mv ./grails2/app/grails-app/views/yoda				      ./grails-app/views
git mv ./grails2/app/grails-app/views/index.gsp 	      ./grails-app/views

git mv ./grails2/app/grails-app/i18n/*              ./grails-app/i18n

mkdir ./src/main/groovy/com
mkdir ./src/main/groovy/org

git mv ./grails2/app/src/groovy/com/* ./src/main/groovy/com
git mv ./grails2/app/src/groovy/org/* ./src/main/groovy/org

git mv ./grails2/app/src/groovy/de/laser/api          ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/base         ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/controller   ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/dbm          ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/exceptions   ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/helper       ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/interfaces   ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/oai          ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/quartz       ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/traits       ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/usage        ./src/main/groovy/de/laser
git mv ./grails2/app/src/groovy/de/laser/web          ./src/main/groovy/de/laser

git mv ./grails2/app/src/java/* ./src/main/java

mkdir ./src/main/webapp/files

git mv ./grails2/app/web-app/setup                ./src/main/webapp
git mv ./grails2/app/web-app/resources/*          ./src/main/webapp/files
git mv ./grails2/app/web-app/swagger              ./src/main/webapp/

git mv ./grails2/app/web-app/css/jquery-editable.css  ./grails-app/assets/stylesheets
git mv ./grails2/app/web-app/css/tmp_semui.css        ./grails-app/assets/stylesheets
git mv ./grails2/app/web-app/images/gasco         ./grails-app/assets/images
git mv ./grails2/app/web-app/images/landingpage   ./grails-app/assets/images
#git mv -f ./grails2/app/web-app/images/*            ./grails-app/assets/images
git mv    ./grails2/app/web-app/js/libs             ./grails-app/assets/javascripts
git mv    ./grails2/app/web-app/js/submodules       ./grails-app/assets/javascripts
git mv    ./grails2/app/web-app/js/*.js             ./grails-app/assets/javascripts

mkdir ./grails-app/assets/vendor

git mv ./grails2/app/web-app/semantic             ./grails-app/assets/themes
git mv ./grails2/app/web-app/vendor/*             ./grails-app/assets/vendor

git mv ./grails2/app/grails-app/conf/UrlMappings.groovy  ./grails-app/controllers/de/laser/UrlMappings.groovy

# issues

currentDate=`date +"%Y-%m-%d %T"`

echo ""
echo "> finished: $currentDate"
echo ""
echo "> known issues:"
echo ""
echo "./grails2/app/web-app/images/* (move manually)"

