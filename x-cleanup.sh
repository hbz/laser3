#!/bin/bash
#
# src: grails-upgrade-source@https://github.com/hbz/laser
# 2020-10-06
#

SRC=./grails2/app
DST=.

LS=de/laser
KB=com/k_int/kbplus

# cleanup

echo "> cleanup: [ $DST ]"

rm -rv ./grails-app/assets/images
rm -rv ./grails-app/assets/javascripts/deprecated
rm -rv ./grails-app/assets/javascripts/libs
rm -rv ./grails-app/assets/javascripts/submodules
rm -rv ./grails-app/assets/stylesheets/deprecated
rm -rv ./grails-app/assets/themes
rm -rv ./grails-app/assets/vendor
rm -rv ./grails-app/controllers/com
rm -v ./grails-app/controllers/de/laser/*       #rollback: SystemProfilerInterceptor.groovy
rm -rv ./grails-app/domain/com
rm -v ./grails-app/domain/de/laser/*.groovy
rm -v ./grails-app/i18n/*
rm -rv ./grails-app/services/*
rm -rv ./grails-app/taglib/*
rm -rv ./grails-app/views/*                     #rollback: some files
rm -rv ./grails-app/taglib/*
rm -rv ./src/main/groovy/com
rm -rv ./src/main/groovy/de/laser/api
rm -rv ./src/main/groovy/de/laser/base
rm -rv ./src/main/groovy/de/laser/controller
rm -rv ./src/main/groovy/de/laser/exceptions
rm -rv ./src/main/groovy/de/laser/helper
rm -rv ./src/main/groovy/de/laser/interfaces
rm -rv ./src/main/groovy/de/laser/oai
rm -rv ./src/main/groovy/de/laser/quartz
rm -rv ./src/main/groovy/de/laser/traits
rm -rv ./src/main/groovy/de/laser/usage
rm -rv ./src/main/groovy/de/laser/web
rm -rv ./src/main/groovy/org
rm -rv ./src/main/java/*
rm -rv ./src/main/webapp/*

# issues

currentDate=`date +"%Y-%m-%d %T"`

echo ""
echo "> finished: $currentDate"
echo ""
echo "> known issues (git rollback required):"
echo ""
echo "./grails-app/controllers/de/laser/SystemProfilerInterceptor.groovy"
echo "./grails-app/views/*"
