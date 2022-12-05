package de.laser.system

import de.laser.Org
import de.laser.storage.BeanStore
import de.laser.utils.AppUtils

/**
 * This class keeps track of the loading times for each page. Recorded is every page which has a longer loading time than 1000 msecs
 * @see de.laser.ajax.AjaxController#notifyProfiler()
 */
class SystemProfiler {

    String  uri
    String  params
    Org     context
    Integer ms
    String  archive

    Date dateCreated

    static mapping = {
        id          column:'sp_id'
        version     false
        uri         column:'sp_uri',        index: 'sp_uri_idx'
        params      column:'sp_params',     type: 'text'
        context     column:'sp_context_fk', index: 'sp_context_idx'
        ms          column:'sp_ms'
        archive     column:'sp_archive',    index: 'sp_archive_idx'
        dateCreated column:'sp_created'
    }

    static constraints = {
        uri     (blank:false)
        params  (nullable:true, blank:true)
        context (nullable:true)
        ms      (nullable:true)
        archive (nullable:true, blank:false)
    }

    /**
     * Delivers the current version of the system environment according to the metadata file (build.gradle)
     * @return the current version
     */
    static String getCurrentArchive() {
        String av = AppUtils.getMeta('info.app.version') ?: 'unkown'
        List<String> avList = av.split("\\.")
        if (avList.size() >= 2) {
            return "${avList[0]}.${avList[1]}"
        }

        return av
    }

    /**
     * Triggered via {@link de.laser.ajax.AjaxController#notifyProfiler()} where the given action the given delay time is being recorded
     * @param delta the time deviation from 1000 msecs
     * @param actionUri the action to record the loading time for
     */
    static void update(long delta, String actionUri) {

        withTransaction {

            // store current request
            if (delta && delta > 0) {
                (new SystemProfiler(
                        uri: actionUri,
                        context: BeanStore.getContextService().getOrg(),
                        ms: delta,
                        archive: SystemProfiler.getCurrentArchive()
                )).save()
            }
        }
    }
}
