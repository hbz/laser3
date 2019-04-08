package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.helper.EhcacheWrapper
import de.laser.helper.RDStore
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.converters.*
import com.k_int.kbplus.auth.*
import static com.k_int.kbplus.UserSettings.KEYS.*
import static de.laser.helper.RDStore.*

@Secured(['IS_AUTHENTICATED_FULLY'])
class ProfileController {

    def cacheService
    def contextService
    def genericOIDService
    def springSecurityService
    def passwordEncoder
    def errorReportService
    def refdataService
    def propertyService
    def instAdmService

    @Secured(['ROLE_USER'])
    def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.editable = true

        result.availableOrgs  = Org.executeQuery('from Org o where o.sector.value = ? order by o.name', 'Higher Education')
        result.availableOrgRoles = Role.findAllByRoleType('user')
        result
    }

    @Secured(['ROLE_USER'])
    def errorReport() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)

        if (params.sendErrorReport) {
            def data = [
                    author:     result.user,
                    title:      params.title?.trim(),
                    described:  params.described?.trim(),
                    expected:   params.expected?.trim(),
                    info:       params.info?.trim(),
                    status:     RefdataValue.getByValueAndCategory('New', 'Ticket.Status'),
                    category:   RefdataValue.getByValueAndCategory('Bug', 'Ticket.Category')
            ]
            result.sendingStatus = (errorReportService.writeReportIntoDB(data) ? 'ok' : 'fail')
        }

        result.title = params.title
        result.described = params.described
        result.expected = params.expected
        result.info = params.info

        result
    }

    @Secured(['ROLE_USER'])
    def errorOverview() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)

        result.tickets = SystemTicket.where{}.list(sort: 'dateCreated', order: 'desc')

        result.editable = SpringSecurityUtils.ifAnyGranted("ROLE_YODA,ROLE_TICKET_EDITOR")
        result
    }

    @Secured(['ROLE_USER'])
    def help() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result
    }

    @Secured(['ROLE_USER'])
    def processJoinRequest() {
        log.debug("processJoinRequest(${params}) org with id ${params.org} role ${params.formalRole}")

        User user       = User.get(springSecurityService.principal.id)
        Org org         = Org.get(params.org)
        Role formalRole = Role.get(params.formalRole)

        if (user && org && formalRole) {
            instAdmService.createAffiliation(user, org, formalRole, UserOrg.STATUS_PENDING, flash)
        }

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def processCancelRequest() {
        log.debug("processCancelRequest(${params}) userOrg with id ${params.assoc}")
        def user        = User.get(springSecurityService.principal.id)
        def userOrg     = UserOrg.findByUserAndId(user, params.assoc)

        if (userOrg) {
            userOrg.delete(flush:true)
        }

        redirect(action: "index")
    }

  @Secured(['ROLE_USER'])
  def updateProfile() {
    def user = User.get(springSecurityService.principal.id)

    flash.message=""

    if ( user.display != params.userDispName ) {
      user.display = params.userDispName
      flash.message += message(code:'profile.updateProfile.updated.name', default:"User display name updated<br/>")
    }

    if ( user.email != params.email ) {
      def mailPattern = /[_A-Za-z0-9-]+(\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\.[A-Za-z0-9-]+)*(\.[A-Za-z]{2,})/
      if ( params.email ==~ mailPattern ) {
        user.email = params.email
        flash.message += message(code:'profile.updateProfile.updated.email', default:"User email address updated<br/>")
      }
      else {
        flash.error = message(code:'profile.updateProfile.updated.email.error', default:"Emails must be of the form user@domain.name<br/>")
      }
    }


    // deprecated
    if ( params.defaultPageSize != null ) {
      try {
        long l = Long.parseLong(params.defaultPageSize);
        if ( ( l >= 5 ) && ( l <= 100 ) ) {
          Long new_long = new Long(l);
          if ( new_long != user.getDefaultPageSizeTMP() ) {
            flash.message += message(code:'profile.updateProfile.updated.pageSize', default:"User default page size updated<br/>")
          }
            //user.setDefaultPageSizeTMP(new_long)
            def setting = user.getSetting(UserSettings.KEYS.PAGE_SIZE, null)
            setting.setValue(size)
     
        }
        else {
          flash.message+= message(code:'profile.updateProfile.updated.pageSize.error', default:"Default page size must be between 5 and 100<br/>");
        }
      }
      catch ( Exception e ) {
      }
    }

      user.save();

    if ( params.defaultDash != user.getSettingsValue(UserSettings.KEYS.DASHBOARD)?.getId().toString() ) {
      flash.message+= message(code:'profile.updateProfile.updated.dash', default:"User default dashboard updated<br/>")
        def setting = user.getSetting(UserSettings.KEYS.DASHBOARD, null)

      if ( params.defaultDash == '' ) {
          setting.setValue(null)
      }
      else {
          def org = genericOIDService.resolveOID(params.defaultDash)
          setting.setValue(org)
      }
    }

    redirect(action: "index")
  }
  @Secured(['ROLE_USER'])
  def updateReminderSettings() {
    def user = User.get(springSecurityService.principal.id)

    flash.message = ""
    flash.error = ""
    changeValue(user.getSetting(DASHBOARD_REMINDER_PERIOD, 14),    params.dashboardReminderPeriod,     'profile.updateProfile.updated.dashboardReminderPeriod')

      if ( (! user.email) && user.getSetting(IS_REMIND_BY_EMAIL, YN_NO).equals(YN_NO)) {
          flash.error = message(code:'profile.updateProfile.updated.isRemindByEmail.error', default:"Please enter the email address<br/>")
      } else {
          changeValue(user.getSetting(IS_REMIND_BY_EMAIL, YN_NO),                     params.isRemindByEmail?:"N",                 'profile.updateProfile.updated.isRemindByEmail')
      }


    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_NOTICEPERIOD, YN_NO),    params.isSubscriptionsNoticePeriod?:"N",     'profile.updateProfile.updated.subscriptions.noticePeriod')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_ENDDATE, YN_NO),         params.isSubscriptionsEnddate?:"N",          'profile.updateProfile.updated.subscriptions.enddate')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_CUSTOM_PROP, YN_NO),     params.isSubscriptionsCustomProp?:"N",       'profile.updateProfile.updated.subscriptions.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_SUBSCRIPTIONS_PRIVATE_PROP, YN_NO),    params.isSubscriptionsPrivateProp?:"N",      'profile.updateProfile.updated.subscriptions.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_LICENSE_CUSTOM_PROP, YN_NO),           params.isLicenseCustomProp?:"N",             'profile.updateProfile.updated.license.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_LIZENSE_PRIVATE_PROP, YN_NO),          params.isLicensePrivateProp?:"N",            'profile.updateProfile.updated.license.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_ORG_CUSTOM_PROP, YN_NO),               params.isOrgCustomProp?:"N",                 'profile.updateProfile.updated.org.customProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_ORG_PRIVATE_PROP, YN_NO),              params.isOrgPrivateProp?:"N",                'profile.updateProfile.updated.org.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_PERSON_PRIVATE_PROP, YN_NO),           params.isPersonPrivateProp?:"N",             'profile.updateProfile.updated.person.privateProperty')
    changeValue(user.getSetting(IS_REMIND_FOR_TASKS, YN_NO),                         params.isTasks?:"N",                         'profile.updateProfile.updated.tasks')

    user.save();

    redirect(action: "index")
  }
    private void changeValue(UserSettings userSetting, def newValue, String messageSuccessfull) {
        def oldValue = userSetting.value
        if (newValue) {
            if (oldValue instanceof RefdataValue) {//} && ((RefdataValue)oldValue).belongsTo.owner. == com.k_int.kbplus.RefdataCategory.{
                if (newValue == 'Y') {
                    newValue = YN_YES
                } else {
                    newValue = YN_NO
                }
            }
            if (userSetting.key.type == Integer) {
                newValue = Integer.parseInt(newValue)
            }
            boolean valueHasChanged = oldValue != newValue
            if (valueHasChanged) {
                userSetting.setValue(newValue)
                flash.message += (message(code: messageSuccessfull) + "<br/>")
            }
        } else {
            if (DASHBOARD_REMINDER_PERIOD == userSetting.key) {
                flash.error += (message(args: userSetting.key, code: 'profile.updateProfile.updated.error.dashboardReminderPeriod') + "<br/>")
            } else {
                flash.error += (message(args: userSetting.key, code: 'profile.updateProfile.updated.error') + "<br/>")
            }
        }
    }

    @Secured(['ROLE_USER'])
    def updateIsRemindByEmail() {
        def user1 = User.get(springSecurityService.principal.id)

        flash.message=""
        def was_isRemindByEmail = user1.getSetting(UserSettings.KEYS.IS_REMIND_BY_EMAIL, RDStore.YN_NO)
        if ( was_isRemindByEmail != params.isRemindByEmail ) {
            was_isRemindByEmail = params.isRemindByEmail
            flash.message += message(code:'profile.updateProfile.updated.isRemindByEmail', default:"isRemindByEmail updated<br/>")
            if ( ! user1.email && was_isRemindByEmail.equals(RDStore.YN_YES)) {
                flash.error = message(code:'profile.updateProfile.updated.isRemindByEmail.error', default:"Please enter the email address<br/>")
            }
        }
        user.save();

        redirect(action: "index")
    }

    @Secured(['ROLE_USER'])
    def updatePassword() {
        def user = User.get(springSecurityService.principal.id)
        flash.message = ""

        if (passwordEncoder.isPasswordValid(user.password, params.passwordCurrent, null)) {
            if (params.passwordNew.trim().size() < 5) {
                flash.message += message(code:'profile.password.update.enterValidNewPassword', default:"Please enter new password (min. 5 chars)")
            } else {
                user.password = params.passwordNew

                if (user.save()) {
                    flash.message += message(code:'profile.password.update.success', default:"Password succesfully updated")
                }
            }

        } else {
            flash.message += message(code:'profile.password.update.enterValidCurrentPassword', default:"Please enter valid current password")
        }
        redirect(action: "index")
    }

    private def addTransforms() {

    def user = User.get(springSecurityService.principal.id)
    def transforms = Transforms.findById(params.transformId)
    
    if(user && transforms){
      def existing_transform = UserTransforms.findByUserAndTransforms(user,transforms);
      if ( existing_transform == null ) {
        new UserTransforms(
            user: user,
            transforms: transforms).save(failOnError: true)
        flash.message="Transformation added"
      }
      else {
        flash.error="You already have added this transform."
      }
    }else{  
      log.error("Unable to locate transforms");
      flash.error="Error we could not add this transformation"
    }

    redirect(action: "index")
  }


    private def removeTransforms() {
    def user = User.get(springSecurityService.principal.id)
    def transforms = Transforms.findById(params.transformId)
    
    //Check if has already transforms
    if(user && transforms){
      def existing_transform = UserTransforms.findByUserAndTransforms(user,transforms);
      if(existing_transform){
        transform.delete(failOnError: true, flush: true)
        flash.message="Transformation removed from your list."
      }else{
        flash.error="This transformation is not in your list."
      }
    }else{
      log.error("Unable to locate transforms");
      flash.error="Error we could not remove this transformation"
    }
    
    redirect(action: "index")
  }

    @Secured(['ROLE_USER'])
    def createReminder() {
        log.debug("Profile :: createReminder - ${params}")
        def result    = [:]
        def user      = User.load(springSecurityService.principal.id)
        def trigger   = (params.int('trigger'))? RefdataValue.load(params.trigger) : RefdataCategory.lookupOrCreate("ReminderTrigger","Subscription Manual Renewal Date")
        def remMethod = (params.int('method'))?  RefdataValue.load(params.method)  : RefdataCategory.lookupOrCreate("ReminderMethod","email")
        def unit      = (params.int('unit'))?    RefdataValue.load(params.unit)    : RefdataCategory.lookupOrCreate("ReminderUnit","Day")


        def reminder = new Reminder(trigger: trigger, unit: unit, reminderMethod: remMethod, amount: params.getInt('val')?:1, user: user, active: Boolean.TRUE)
        if (reminder.save())
        {
            log.debug("Profile :: Index - Successfully saved reminder, adding to user")
            user.addToReminders(reminder)
            log.debug("User has following reminders ${user.reminders}")
            result.status   = true
            result.reminder = reminder
        } else {
            result.status = false
            flash.error="Unable to create the reminder, invalid data received"
            log.debug("Unable to save Reminder for user ${user.username}... Params as follows ${params}")
        }
        if (request.isXhr())
            render result as JSON
        else
            redirect(action: "index", fragment: "reminders")
    }

    @Secured(['ROLE_USER'])
    def updateReminder() {
        def result    = [:]
        result.status = true
        result.op     = params.op
        def user      = User.get(springSecurityService.principal.id)
        def reminder  = Reminder.findByIdAndUser(params.id,user)
        if (reminder)
        {
            switch (result.op)
            {
                case 'delete':
                    user.reminders.clear()
                    user.reminders.remove(reminder)
                    reminder.delete(flush: true)
                    break
                case 'toggle':
                    reminder.active = !reminder.active
                    result.active   = reminder.active? 'disable':'enable'
                    break
                default:
                    result.status = false
                    log.error("Profile :: updateReminder - Unsupported operation for update reminder ${result.op}")
                    break
            }
        } else
            result.status = false

        render result as JSON
    }
    
    @Secured(['ROLE_USER'])
    def properties() {

        // def ctxCache = contextService.getCache('ProfileController/properties/')
        EhcacheWrapper cache = cacheService.getTTL300Cache('ProfileController/properties/')

        def propDefs = [:]

        if (cache.get('propDefs')) {
            propDefs = cache.get('propDefs')
            log.debug('propDefs from cache')
        }
        else {
            PropertyDefinition.AVAILABLE_CUSTOM_DESCR.each { it ->
                def itResult = PropertyDefinition.findAllByDescrAndTenant(it, null, [sort: 'name']) // NO private properties!
                propDefs << ["${it}": itResult]
            }
            cache.put('propDefs', propDefs)
        }

        def usedRdvList, rdvAttrMap, usedPdList, pdAttrMap

        if (cache.get('usedRdvList')) {
            usedRdvList = cache.get('usedRdvList')
            log.debug('usedRdvList from cache')
        }
        else {
            (usedRdvList, rdvAttrMap) = refdataService.getUsageDetails()
            cache.put('usedRdvList', usedRdvList)
        }

        if (cache.get('usedPdList')) {
            usedPdList = cache.get('usedPdList')
            log.debug('usedPdList from cache')
        }
        else {
            (usedPdList, pdAttrMap) = propertyService.getUsageDetails()
            cache.put('usedPdList', usedPdList)
        }

        render view: 'properties', model: [
                editable    : false,
                cachedContent : cache.getCache().name,
                propertyDefinitions: propDefs,
                rdCategories: RefdataCategory.where{}.sort('desc'),
                usedRdvList : usedRdvList,
                usedPdList  : usedPdList
        ]
    }
}
