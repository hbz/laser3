package de.laser

import de.laser.config.ConfigMapper
import de.laser.remote.ApiSource
import de.laser.remote.GlobalRecordSource
import de.laser.system.SystemEvent
import de.laser.system.SystemMessage
import de.laser.system.SystemSetting
import de.laser.utils.AppUtils
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.json.JsonOutput
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.xcontent.XContentType

import java.text.SimpleDateFormat

/**
 * This service checks the system health
 */
@Transactional
class SystemService {

    ContextService contextService
    ESWrapperService ESWrapperService

    /**
     * Dumps the state of currently active services
     * @return a map with the services marked as active by configuration or with sources marked as active and their running state
     */
    Map<String, Object> serviceCheck() {
        Map<String, Object> checks = [:]

        if(SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_YODA') || ConfigMapper.getShowSystemInfo()) {
            //GlobalData Sync
            if ((GlobalRecordSource.findAllByActive(false)?.size() == GlobalRecordSource.findAll()?.size()) || GlobalRecordSource.findAll()?.size() == 0) {
                checks.globalSync = "NOT active"
            }

            if ((ApiSource.findAllByActive(false)?.size() == ApiSource.findAll()?.size()) || ApiSource.findAll()?.size() == 0) {
                checks.apiSource = "NOT active"
            }

            if (! ConfigMapper.getNotificationsJobActive()) {
                checks.notificationsJobActive = "NOT active"
            }
            if (! ConfigMapper.getGlobalDataSyncJobActive()) {
                checks.globalDataSyncJob = "NOT active"
            }
            if (! ConfigMapper.getIsUpdateDashboardTableInDatabase()) {
                checks.UpdateDashboardTableInDatabase = "NOT active"
            }
            if (! ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers()) {
                checks.SendEmailsForDueDatesOfAllUsers = "NOT active"
            }
            if (! ConfigMapper.getReporting()) {
                checks.Reporting = "ElasticSearch Config for Reporting not found"
            }
            if (ConfigMapper.getConfig('grails.mail.disabled', Boolean)) {
                checks.MailService = "NOT active"
            }
        }

        return checks
    }

    Map getStatusMessage(long messageId = 0) {
        Map result = [ status: 'error' ]

        try {
            result = [
                    status:      'ok',
                    maintenance: SystemSetting.findByName('MaintenanceMode').value == 'true',
                    messages:    SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION) ? true : false
            ]
        } catch(Exception e) {
            log.error( e.getMessage() )
        }
        if (messageId) {
            result.putAt('id', messageId)
        }

        result
    }

    boolean sendInsight_SystemEvents(String systemInsightIndex) {
        boolean done = false
        SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd_HHmmss()

        List<SystemEvent> events = SystemEvent.executeQuery(
                "select se from SystemEvent se where se.created > (CURRENT_DATE-1) and se.relevance in ('WARNING', 'ERROR') order by se.created desc"
        )

        Map<String, Object> output = [
                system  : "${ConfigMapper.getLaserSystemId()}",
                server  : "${AppUtils.getCurrentServer()}",
                created : "${sdf.format(new Date())}",
                data_type   : "SystemEvent",
                data_count  : events.size(),
                data        : []
        ]

        if (events) {
            events.each { e ->
                Map<String, Object> data = [
                        created : "${sdf.format(e.created)}",
                        level   : "${e.relevance.value}",
                        event   : "${e.getSource()} -> ${e.getEvent()}"
                ]
                if (e.payload) {
                    data.putAt('payload', "${e.payload}")
                }
                output.data.add( data )
            }
        }

        RestHighLevelClient esclient
        try {
            String json = JsonOutput.toJson(output)

            esclient = ESWrapperService.getNewClient(true)
            if (esclient) {
                IndexRequest request = new IndexRequest( systemInsightIndex )
                request.source( json, XContentType.JSON )
                IndexResponse response = esclient.index(request, RequestOptions.DEFAULT)

                // TODO
                done = response.getResult().toString() == 'CREATED'
                log.info 'sendInsight_SystemEvents( ' + systemInsightIndex + ' ): ' + events.size() + ' events -> ' + response.getResult()
            }
        }
        catch (Exception e) {
            log.error e.getMessage()
        }
        finally {
            if (esclient) { esclient.close() }
        }
        done
    }
}
