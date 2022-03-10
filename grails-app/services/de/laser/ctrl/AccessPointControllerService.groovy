package de.laser.ctrl

import de.laser.*
import de.laser.helper.RDStore
import de.laser.oap.*
import de.uni_freiburg.ub.Exception.InvalidRangeException
import de.uni_freiburg.ub.IpRange
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.json.JsonOutput
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException

/**
 * This service is a container for the complex data manipulation methods of the {@link AccessPointController}
 */
@Transactional
class AccessPointControllerService {

    MessageSource messageSource
    ContextService contextService
    AccessService accessService

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    /**
     * Adds the given IP range to the institution's access points after validating the input
     * @param params the parameter map containing the input
     * @return the status map: OK if the submit was successful, ERROR otherwise
     */
    Map<String,Object> addIpRange(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        OrgAccessPoint orgAccessPoint = OrgAccessPoint.get(params.id)
        // need to check if contextOrg == orgAccessPoint.org for ORG_CONSORTIUM? The template has no editable elements
        // in that context (would need to fake a post request), similar for deleteIpRange method.
        List<IpRange> validRanges = []
        List<IpRange> invalidRanges = []
        // allow multiple ip ranges as input (must be separated by comma)
        String[] ipCol = params.ip.replaceAll("\\s+", " ").split(",")
        for (range in ipCol) {
            try {
                // check if given input string is a valid ip range
                IpRange ipRange = IpRange.parseIpRange(range)
                List<AccessPointData> accessPointDataList = AccessPointData.findAllByOrgAccessPoint(orgAccessPoint);
                // so far we know that the input string represents a valid ip range
                // check if the input string is already saved
                boolean isDuplicate = false
                for (accessPointData in accessPointDataList) {
                    if (accessPointData.getInputStr() == ipRange.toInputString()) {
                        isDuplicate = true
                    }
                }
                if (!isDuplicate) {
                    validRanges << ipRange
                }
            }
            catch (InvalidRangeException e) {
                invalidRanges << range
            } catch (NumberFormatException e) {
                invalidRanges << range
            }
        }
        // persist all valid ranges
        for (ipRange in validRanges) {
            String jsonData = JsonOutput.toJson([
                    inputStr  : ipRange.toInputString(),
                    startValue: ipRange.lowerLimit.toHexString(),
                    endValue  : ipRange.upperLimit.toHexString()]
            )
            AccessPointData accessPointData = new AccessPointData()
            accessPointData.orgAccessPoint = orgAccessPoint
            accessPointData.datatype = 'ip' + ipRange.getIpVersion()
            accessPointData.data = jsonData
            accessPointData.save()
            orgAccessPoint.lastUpdated = new Date()
            orgAccessPoint.save()
        }
        if(invalidRanges) {
            // return only those input strings to UI which represent a invalid ip range
            Object[] args = [invalidRanges.join(' ')]
            result.invalidRanges = invalidRanges
            result.error = messageSource.getMessage('accessPoint.invalid.ip', args, LocaleContextHolder.getLocale())
            [result:result,status:STATUS_ERROR]
        }
        else [result:result,status:STATUS_OK]
    }

    /**
     * Creates a new access point for the given institution with the given access method
     * @param params the parameter map containing the input
     * @param accessMethod the access method for the new access point
     * @return OK if the creation was successful, ERROR otherwise
     */
    Map<String,Object> createAccessPoint(GrailsParameterMap params, RefdataValue accessMethod) {
        Map<String,Object> result = [:]
        Locale locale = LocaleContextHolder.getLocale()
        // without the org somehow passed we can only create AccessPoints for the context org
        Org orgInstance = accessService.checkPerm("ORG_CONSORTIUM") ? Org.get(params.id) : contextService.getOrg()
        if(!params.name) {
            result.error = messageSource.getMessage('accessPoint.require.name', null, locale)
            [result:result,status:STATUS_ERROR]
        }
        else {
            List<OrgAccessPoint> oap = OrgAccessPoint.findAllByNameAndOrg(params.name, orgInstance)
            if (oap) {
                Object[] args = [params.name]
                result.error = messageSource.getMessage('accessPoint.duplicate.error', args, locale)
                [result:result,status:STATUS_ERROR]
            }
            else {
                OrgAccessPoint accessPoint
                switch(accessMethod) {
                    case RDStore.ACCESS_POINT_TYPE_OA: accessPoint = new OrgAccessPointOA()
                        accessPoint.entityId = params.entityId
                        break
                    case RDStore.ACCESS_POINT_TYPE_VPN: accessPoint = new OrgAccessPointVpn()
                        break
                    case RDStore.ACCESS_POINT_TYPE_EZPROXY: accessPoint = new OrgAccessPointEzproxy()
                        accessPoint.url = params.url
                        break
                    case RDStore.ACCESS_POINT_TYPE_SHIBBOLETH: accessPoint = new OrgAccessPointShibboleth()
                        accessPoint.entityId = params.entityId
                        break
                    default: accessPoint = new OrgAccessPoint()
                        break
                }
                accessPoint.org = orgInstance
                accessPoint.name = params.name
                accessPoint.accessMethod = accessMethod
                accessPoint.save()
                result.accessPoint = accessPoint
                [result:result,status:STATUS_OK]
            }
        }
    }

    /**
     * Deletes the given access point if no platforms nor subscriptions are linked to it
     * @param params the parameter map containing the id to delete
     * @return OK if the deletion was successful, ERROR otherwise
     */
    Map<String,Object> delete(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        Locale locale = LocaleContextHolder.getLocale()
        OrgAccessPoint accessPoint = OrgAccessPoint.get(params.id)
        if (!accessPoint) {
            Object[] args = [messageSource.getMessage('accessMethod.label',null,locale), params.id]
            result.error = messageSource.getMessage('default.not.found.message', args, locale)
            [result:result,status:STATUS_ERROR]
        }
        else {
            Org org = accessPoint.org
            boolean inContextOrg = (org.id == contextService.getOrg().id)
            if(((accessService.checkPerm('ORG_BASIC_MEMBER') && inContextOrg) || (accessService.checkPerm('ORG_CONSORTIUM')))) {
                Long oapPlatformLinkCount = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNull(true, accessPoint)
                Long oapSubscriptionLinkCount = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNotNull(true, accessPoint)
                if (oapPlatformLinkCount != 0 || oapSubscriptionLinkCount != 0) {
                    Object[] args = [oapPlatformLinkCount, oapSubscriptionLinkCount]
                    result.error = messageSource.getMessage('accessPoint.list.deleteDisabledInfo', args, locale)
                    [result:result,status:STATUS_ERROR]
                }
                else {
                    Long orgId = org.id
                    try {
                        accessPoint.delete()
                        result.orgId = orgId
                        Object[] args = [messageSource.getMessage('accessPoint.label',null,locale), accessPoint.name]
                        result.message = messageSource.getMessage('default.deleted.message', args, locale)
                        [result:result,status:STATUS_OK]
                    }
                    catch (DataIntegrityViolationException e) {
                        Object[] args = [messageSource.getMessage('address.label',null,locale), accessPoint.id]
                        result.error = messageSource.getMessage('default.not.deleted.message', args, locale)
                        [result:result,status:STATUS_ERROR]
                    }
                }
            }
            else {
                [result:null,status:STATUS_ERROR]
            }
        }
    }



}
