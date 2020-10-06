#!/bin/bash
#
# src: grails-upgrade-source@https://github.com/hbz/laser
# 2020-10-06
#

# migrations

echo "> processing migrations .."

declare -a locations=('./grails2/app/grails-app/controllers' './grails2/app/grails-app/domain' './grails2/app/grails-app/jobs' './grails2/app/grails-app/services' './grails2/app/grails-app/taglib' './grails2/app/src')
declare -a migrations=()

migrations+=('import au.com.bytecode.opencsv./import com.opencsv.')
migrations+=('import grails.plugin.mail.MailService/import grails.plugins.mail.MailService')
migrations+=('import grails.transaction.Transactional/import grails.gorm.transactions.Transactional')
migrations+=('import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler/import org.grails.core.artefact.DomainClassArtefactHandler')
migrations+=('import org.codehaus.groovy.grails.commons.GrailsApplication/import grails.core.GrailsApplication')
migrations+=('import org.codehaus.groovy.grails.commons.GrailsClass/import grails.core.GrailsClass')
migrations+=('import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil/import org.grails.orm.hibernate.cfg.GrailsHibernateUtil')
migrations+=('import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin/import org.grails.plugins.domain.DomainClassGrailsPlugin')
migrations+=('import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib/import org.grails.plugins.web.taglib.ApplicationTagLib')
migrations+=('import org.codehaus.groovy.grails.web.binding.DataBindingUtils/import grails.web.databinding.DataBindingUtils')
migrations+=('import org.codehaus.groovy.grails.web.json.JSONElement/import org.grails.web.json.JSONElement')
migrations+=('import org.codehaus.groovy.grails.web.json.JSONObject/import org.grails.web.json.JSONObject')
migrations+=('import org.codehaus.groovy.grails.web.json.parser.JSONParser/import org.grails.web.json.parser.JSONParser')
migrations+=('import org.codehaus.groovy.grails.web.mapping.LinkGenerator/import grails.web.mapping.LinkGenerator')
migrations+=('import org.codehaus.groovy.grails.web.servlet.FlashScope/import grails.web.mvc.FlashScope')
migrations+=('import org.codehaus.groovy.grails.web.servlet.mvc.GrailsHttpSession/import grails.web.servlet.mvc.GrailsHttpSession')
migrations+=('import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap/import grails.web.servlet.mvc.GrailsParameterMap')
migrations+=('import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest/import org.grails.web.servlet.mvc.GrailsWebRequest')
migrations+=('import org.codehaus.groovy.grails.web.util.WebUtils/import org.grails.web.util.WebUtils')
#migrations+=('import org.grails.commons.GrailsClass/import grails.core.GrailsClass')
migrations+=('import org.grails.web.servlet.mvc.GrailsHttpSession/import grails.web.servlet.mvc.GrailsHttpSession')
migrations+=('import org.hibernate.classic.Session/import org.hibernate.Session')
#migrations+=('import org.hibernate.event.PostUpdateEvent/import org.hibernate.event.spi.PostUpdateEvent')
migrations+=('import org.hibernate.event.PostDeleteEvent/import org.grails.datastore.mapping.engine.event.PostDeleteEvent')
migrations+=('import org.hibernate.event.PostInsertEvent/import org.grails.datastore.mapping.engine.event.PostInsertEvent')
migrations+=('import org.hibernate.event.PostUpdateEvent/import org.grails.datastore.mapping.engine.event.PostUpdateEvent')
migrations+=('import org.hibernate.impl.SQLQueryImpl/import org.hibernate.internal.SQLQueryImpl')

migrations+=('import groovy.util.logging.Log4j/import groovy.util.logging.Slf4j')
migrations+=('@Log4j/@Slf4j')
migrations+=('import com.k_int.kbplus.auth./import de.laser.auth.')
migrations+=('def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP/ \/\/def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP')
migrations+=('propertyInstanceMap.get().clear()/ \/\/propertyInstanceMap.get().clear()')

migrations+=('ctx.springSecurityService.getCurrentUser()?/principal.user?')

migrations+=('import de.laser.interfaces.AuditableSupport/import grails.plugins.orm.auditable.Auditable')
migrations+=('implements AuditableSupport/implements Auditable')
migrations+=('AuditableSupport auditable/Auditable auditable')
migrations+=('AuditableSupport obj/Auditable obj')
migrations+=('\.controlledProperties/.getLogIncluded()')
migrations+=('controlledProperties\./getLogIncluded().')
migrations+=('\.getClass().getLogIncluded()/.getLogIncluded()')

migrations+=(' extends AbstractDebugController/ ')
migrations+=('import de.laser.controller.AbstractDebugController/ ')

for LOC in "${locations[@]}"
do
  echo "> (*.groovy) for: [ $LOC ]"

  for TASK in "${migrations[@]}"
  do
    #echo "  - $TASK"

    find $LOC -name *.groovy -type f -exec sed -i "s/$TASK/g" {} \;
  done
done

declare -a locations=('./grails-app/views' './grails-app/taglib')
declare -a migrations=()

# gsp

migrations+=('org.codehaus.groovy.grails.web.errors.ExceptionUtils/org.grails.exceptions.ExceptionUtils')
migrations+=('org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes/org.grails.web.util.GrailsApplicationAttributes')
migrations+=('org.codehaus.groovy.grails.support.encoding./org.grails.encoder.')
migrations+=('org.codehaus.groovy.grails.web.pages.GroovyPage/org.grails.gsp.GroovyPage')
migrations+=('org.codehaus.groovy.grails.web.pages.TagLibraryLookup/org.grails.taglib.TagLibraryLookup')
migrations+=('de.laser.interfaces.AuditableSupport/grails.plugins.orm.auditable.Auditable')
migrations+=('AuditableSupport/Auditable')
migrations+=('com.k_int.kbplus.auth./de.laser.auth.')
migrations+=('<r:script>/<asset:script type="text\/javascript">')
migrations+=('<\/r:script>/<\/asset:script>')
migrations+=('<g:javascript src/<asset:javascript src')
migrations+=('<g:javascript/<asset:script type="text\/javascript"')
migrations+=('<\/g:javascript/<\/asset:script')
migrations+=('<r:layoutResources\/>/<asset:deferredScripts\/>')
migrations+=("dir: 'resources'/dir: 'files'")
migrations+=("dir: 'resources\/downloadFile'/dir: 'files'")

migrations+=('<r:require module="${currentTheme}" \/>/<asset:stylesheet src="laser.css"\/><asset:javascript src="laser.js"\/>')               # TMP
migrations+=('<r:require module="semanticUI" \/>/<asset:stylesheet src="laser.css"\/><asset:javascript src="laser.js"\/>')
migrations+=('<r:require module="chartist" \/>/<asset:stylesheet src="chartist.css"\/><asset:javascript src="chartist.js"\/>')
migrations+=('<r:require module="datatables" \/>/<asset:stylesheet src="datatables.css"\/><asset:javascript src="datatables.js"\/>')
migrations+=('<r:require module="swaggerApi" \/>/<asset:stylesheet src="swagger.css"\/><asset:javascript src="swagger.js"\/>')

for LOC in "${locations[@]}"
do
  echo "> (*.gsp/*.groovy) for: [ $LOC ]"

  for TASK in "${migrations[@]}"
  do
    #echo "  - $TASK"
    find $LOC \( -name *.gsp -o -name *.groovy \) -type f -exec sed -i "s/$TASK/g" {} \;
  done
done

# patches

echo "> applying file patches .."

KB=com/k_int/kbplus

sed -i "s/documents sort:'owner.id', order:'desc', batchSize: 10/documents batchSize: 10/g" ./grails2/app/grails-app/domain/$KB/License.groovy
sed -i "s/tipps sort:'title.title', order: 'asc', batchSize: 10/tipps batchSize: 10/g" ./grails2/app/grails-app/domain/$KB/Package.groovy
sed -i "s/tipps sort: 'title.title', order: 'asc', batchSize: 10/tipps batchSize: 10/g" ./grails2/app/grails-app/domain/$KB/Platform.groovy
sed -i "s/\/\/ return CacheManager.newInstance() \/\/ grails 3/return CacheManager.newInstance()/g" ./grails2/app/grails-app/services/de/laser/CacheService.groovy
sed -i "s/return CacheManager.getCacheManager('__DEFAULT__')/\/\/return CacheManager.getCacheManager('__DEFAULT__')/g" ./grails2/app/grails-app/services/de/laser/CacheService.groovy

# issues

currentDate=`date +"%Y-%m-%d %T"`

echo ""
echo "> finished: $currentDate"
echo ""
echo "> known issues:"
echo "- fix code signed with: grails-3-fix"
echo "- remove multiple <asset:deferredScripts/>"
echo "- update de.laser.auth.*"
