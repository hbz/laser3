package com.k_int.kbplus


import de.laser.ContextService
import groovy.text.SimpleTemplateEngine
import org.gokb.GOKbTextUtils
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

@Deprecated
class EnrichmentService implements ApplicationContextAware {

  ApplicationContext applicationContext

  def executorService
  def grailsApplication
  def mailService
  def sessionFactory
  def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP

  def initiateHousekeeping() {
    com.k_int.kbplus.EnrichmentService.log.debug("initiateHousekeeping");
    def future = executorService.submit({
      doHousekeeping()
    } as java.util.concurrent.Callable)
    com.k_int.kbplus.EnrichmentService.log.debug("initiateHousekeeping returning");
  }

  def doHousekeeping() {
    try {
      Map<String, Object> result = [:]
      result.possibleDuplicates = []
      result.packagesInLastWeek = []
      doDuplicateTitleDetection(result)
      addPackagesAddedInLastWeek(result)
      sendEmail(result)
    }
    catch ( Exception e ) {
      com.k_int.kbplus.EnrichmentService.log.error("Problem in housekeeping",e);
    }
  }

  def doDuplicateTitleDetection(result) {
    com.k_int.kbplus.EnrichmentService.log.debug("Duplicate Title Detection");
    def initial_title_list = TitleInstance.executeQuery("select title.id, title.normTitle from TitleInstance as title order by title.id asc");
    initial_title_list.each { title ->
      // Compare this title against every other title
      def inner_title_list = TitleInstance.executeQuery("select title.id, title.normTitle from TitleInstance as title where title.id > ? order by title.id asc", title[0]);
      inner_title_list.each { inner_title ->
        def similarity = GOKbTextUtils.cosineSimilarity(title[1], inner_title[1])
        if ( similarity > ( ( grailsApplication.config.cosine?.good_threshold ) ?: 0.925 ) ) {
          com.k_int.kbplus.EnrichmentService.log.debug("Possible Duplicate:  ${title[1]} and ${inner_title[1]} : ${similarity}");
          result.possibleDuplicates.add([title[0], title[1], inner_title[0], inner_title[1],similarity]);
        }
      }
    }
  }

  def addPackagesAddedInLastWeek(result) {
    Date last_week = new Date(System.currentTimeMillis() - (1000*60*60*24*7))
    def packages_in_last_week = Package.executeQuery("select p from Package as p where p.dateCreated > ? order by p.dateCreated",[last_week])
    packages_in_last_week.each {
      result.packagesInLastWeek.add(it);
    }
  }

  def sendEmail(result) {

    com.k_int.kbplus.EnrichmentService.log.debug("sendEmail....");
    def emailTemplateFile = applicationContext.getResource("WEB-INF/mail-templates/housekeeping.gsp").file
    def engine = new SimpleTemplateEngine()
    def tmpl = engine.createTemplate(emailTemplateFile).make(result)
    def content = tmpl.toString()
    def systemEmail = grailsApplication.config?.systemEmail ?: null
    if (systemEmail){
      def currentServer = grailsApplication.config?.getCurrentServer()
      def subjectSystemPraefix = (currentServer == ContextService.SERVER_PROD)? "LAS:eR - " : (grailsApplication.config?.laserSystemId + " - ")

      mailService.sendMail {
        to systemEmail
        from systemEmail
        subject subjectSystemPraefix + 'LAS:eR Housekeeping Results'
        html content
      }
    }else{
      com.k_int.kbplus.EnrichmentService.log.debug("No system Email defined.")
    }
  }

  def initiateCoreMigration() {
    com.k_int.kbplus.EnrichmentService.log.debug("initiateCoreMigration");
    def future = executorService.submit({
      com.k_int.kbplus.EnrichmentService.log.debug("Submit job....");
      doCoreMigration()
    } as java.util.concurrent.Callable)
    com.k_int.kbplus.EnrichmentService.log.debug("initiateCoreMigration returning");
  }

  def doCoreMigration() {
    com.k_int.kbplus.EnrichmentService.log.debug("Running core migration....");
    try {
      def ie_ids_count = IssueEntitlement.executeQuery('select count(ie.id) from IssueEntitlement as ie')[0];
      def ie_ids = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement as ie');
      def start_time = System.currentTimeMillis();
      int counter=0

      ie_ids.each { ieid ->

        IssueEntitlement.withNewTransaction {

          com.k_int.kbplus.EnrichmentService.log.debug("Get ie ${ieid}");

          IssueEntitlement ie = IssueEntitlement.get(ieid);

          if ( ( ie != null ) && ( ie.subscription != null ) && ( ie.tipp != null ) ) {

            def elapsed = System.currentTimeMillis() - start_time
            def avg = counter > 0 ? ( elapsed / counter ) : 0
            com.k_int.kbplus.EnrichmentService.log.debug("Processing ie_id ${ieid} ${counter++}/${ie_ids_count} - ${elapsed}ms elapsed avg=${avg}");
            def inst = ie.subscription.getSubscriber()
            def title = ie.tipp.title
            def provider = ie.tipp.pkg.getContentProvider()
    
            if ( inst && title && provider ) {
              def tiinp = TitleInstitutionProvider.findByTitleAndInstitutionAndprovider(title, inst, provider)
              if ( tiinp == null ) {
                com.k_int.kbplus.EnrichmentService.log.debug("Creating new TitleInstitutionProvider");
                tiinp = new TitleInstitutionProvider(title:title, institution:inst, provider:provider).save(flush:true, failOnError:true)
              }

              com.k_int.kbplus.EnrichmentService.log.debug("Got tiinp:: ${tiinp}");
              if ( ie.coreStatusStart != null ) {
                tiinp.extendCoreExtent(ie.coreStatusStart, ie.coreStatusEnd );
              }
              else {
                com.k_int.kbplus.EnrichmentService.log.debug("No core start date - skip");
              }
            }
            else {
              com.k_int.kbplus.EnrichmentService.log.error("Missing title(${title}), provider(${provider}) or institution(${inst})");
            }
          }
          else {
            com.k_int.kbplus.EnrichmentService.log.error("IE ${ieid} is null, has no subscription or tipp.");
          }
        }

        if ( counter % 5000 == 0 ) {
          com.k_int.kbplus.EnrichmentService.log.debug("Clean up gorm");
          cleanUpGorm();
        }

      }
    }
    catch ( Exception e ) {
      com.k_int.kbplus.EnrichmentService.log.error("Problem",e);
    }
  }

  def cleanUpGorm() {
    com.k_int.kbplus.EnrichmentService.log.debug("Clean up GORM");
    def session = sessionFactory.currentSession
    session.flush()
    session.clear()
    propertyInstanceMap.get().clear()
  }

}
