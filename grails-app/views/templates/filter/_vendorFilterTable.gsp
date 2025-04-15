<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.survey.SurveyConfigVendor; de.laser.survey.SurveyVendorResult; de.laser.CustomerTypeService; de.laser.survey.SurveyInfo; de.laser.utils.AppUtils; de.laser.convenience.Marker; java.time.temporal.ChronoUnit; de.laser.utils.DateUtils; de.laser.survey.SurveyOrg; de.laser.survey.SurveyResult; de.laser.Subscription; de.laser.addressbook.PersonRole; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.ReaderNumber; de.laser.addressbook.Contact; de.laser.auth.User; de.laser.auth.Role; grails.plugin.springsecurity.SpringSecurityUtils; de.laser.SubscriptionsQueryService; de.laser.storage.RDConstants; de.laser.storage.RDStore; java.text.SimpleDateFormat; de.laser.License; de.laser.Org; de.laser.OrgRole; de.laser.OrgSetting; de.laser.wekb.Vendor; de.laser.AlternativeName; de.laser.RefdataCategory;" %>
<laser:serviceInjection/>

<table id="${tableID ?: ''}" class="ui sortable celled la-js-responsive-table la-table table ${fixedHeader ?: ''}">
    <thead>
        <tr>
            <g:if test="${tmplShowCheckbox}">
                <th>
                    <g:if test="${vendorList}">
                        <g:checkBox name="vendorListToggler" id="vendorListToggler" checked="${allChecked ? 'true' : 'false'}"/>
                    </g:if>
                </th>
            </g:if>
            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">
                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                    <th>${message(code: 'default.shortname.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(o.name)" params="${request.getParameterMap()}"/>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                    <th>${message(code: 'org.isWekbCurated.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                    <th>${message(code: 'default.status.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                    <th class="la-th-wrap">${message(code: 'org.subscriptions.label')}</th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                    <th>${message(code: 'platform')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                    <th class="center aligned">
                        <ui:markerIcon type="WEKB_CHANGES" />
                    </th>
                </g:if>
                %{--<g:if test="${tmplConfigItem == 'surveyVendorsComments'}">
                    <th>
                        <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                            ${message(code: 'surveyResult.participantComment')}
                        </g:if>
                        <g:else>
                            ${message(code: 'surveyResult.commentParticipant')}
                            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </g:else>
                    </th>
                    <th>
                        <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                            ${message(code: 'surveyResult.commentOnlyForOwner')}
                            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </g:if>
                        <g:else>
                            ${message(code: 'surveyResult.commentOnlyForParticipant')}
                            <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                  data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                                <i class="${Icon.TOOLTIP.HELP}"></i>
                            </span>
                        </g:else>
                    </th>
                </g:if>--}%
                <g:if test="${tmplConfigItem == 'linkSurveyVendor' || tmplConfigItem == 'unLinkSurveyVendor' || tmplConfigItem == 'selectSurveyVendorResult'}">
                    <th class="center aligned">
                        <ui:optionsIcon />
                    </th>
                </g:if>

            </g:each>
        </tr>
    </thead>
    <tbody>
        <g:each in="${vendorList}" var="vendor" status="i">
            <tr <g:if test="${tmplShowCheckbox && currVenSharedLinks.get(vendor.id) == true}">class="disabled"</g:if>>
            <g:if test="${tmplShowCheckbox}">
                <td>
                    <g:if test="${currVenSharedLinks.get(vendor.id) == true}">
                        <i class="${Icon.SIG.SHARED_OBJECT_ON}"></i>
                    </g:if>
                    <g:elseif test="${'linkVendors' in tmplConfigShow || 'linkSurveyVendor' in tmplConfigShow && (!selectedVendorIdList || !(vendor.id in selectedVendorIdList)) || 'unLinkSurveyVendor' in tmplConfigShow && (selectedVendorIdList && (vendor.id in selectedVendorIdList))}">
                        <g:checkBox id="selectedVendors_${vendor.id}" name="selectedVendors" value="${vendor.id}" checked="${vendor.id in currVendors ? 'true' : 'false'}"/>
                    </g:elseif>
                </td>
            </g:if>

            <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <td class="center aligned">
                        ${(params.int('offset') ?: 0) + i + 1}<br />
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('sortname')}">
                    <td>
                        ${vendor.sortname}
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <th scope="row" class="la-th-column la-main-object">
                        <div class="la-flexbox">
                            <g:link controller="vendor" action="show" id="${vendor.id}">
                                ${vendor.name}
                            </g:link>
                        </div>
                    </th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('isWekbCurated')}">
                    <td class="center aligned">
                        <g:if test="${vendor.gokbId}">
                            <ui:wekbButtonLink type="vendor" gokbId="${vendor.gokbId}" />
                        </g:if>
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('status')}">
                    <td class="center aligned">
                        <g:if test="${vendor.status == RDStore.VENDOR_STATUS_CURRENT}">
                            <span class="la-popup-tooltip" data-position="top right">
                                <i class="${Icon.SYM.CIRCLE} green"></i>
                            </span>
                        </g:if>
                        <g:if test="${vendor.status == RDStore.VENDOR_STATUS_RETIRED}">
                            <span class="la-popup-tooltip" data-position="top right" <g:if test="${vendor.retirementDate}">data-content="<g:message code="org.retirementDate.label"/>: <g:formatDate format="${message(code: 'default.date.format.notime')}" date="${vendor.retirementDate}"/>"</g:if>>
                                <i class="${Icon.SYM.CIRCLE} yellow"></i>
                            </span>
                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('currentSubscriptions')}">
                    <td>
                        <g:if test="${currentSubscriptions}">

                                <g:each in="${currentSubscriptions.get(vendor.id)}" var="sub">
                                    <g:if test="${!sub.instanceOf || (sub.instanceOf && !(sub.instanceOf in currentSubscriptions.get(vendor.id)))}">
                                        <div class="la-flexbox">
                                            <g:if test="${currentSubscriptions.get(vendor.id).size() > 1}">
                                                <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                                            </g:if>
                                            <g:link controller="subscription" action="show" id="${sub.id}">
                                                ${sub}
                                                <g:if test="${sub.instanceOf}">
                                                    <br>(${sub.getSubscriberRespConsortia()})
                                                </g:if>
                                            </g:link>
                                        </div>
                                    </g:if>
                                </g:each>

                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('platform')}">
                    <td>
                        <g:each in="${vendorService.getSubscribedPlatforms(vendor, institution)}" var="platform">
                            <g:if test="${platform.gokbId != null}">
                                <ui:wekbIconLink type="platform" gokbId="${platform.gokbId}" />
                            </g:if>
                            <g:link controller="platform" action="show" id="${platform.id}">${platform.name}</g:link>
                            <br />
                        </g:each>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('marker')}">
                    <td class="center aligned">
                        <g:if test="${vendor.isMarked(contextService.getUser(), Marker.TYPE.WEKB_CHANGES)}">
                            <ui:cbItemMarkerAction vendor="${vendor}" type="${Marker.TYPE.WEKB_CHANGES}" simple="true"/>
                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('isMyX')}">
                    <td class="center aligned">
                        <g:if test="${currentVendorIdList && (vendor.id in currentVendorIdList)}">
                            <span class="la-popup-tooltip" data-content="${message(code: 'menu.my.vendors')}"><i class="${Icon.SIG.MY_OBJECT} yellow"></i></span>
                        </g:if>
                    </td>
                </g:if>

                <g:if test="${tmplConfigItem == 'linkSurveyVendor'}">
                    <td class="right aligned">
                        <g:if test="${editable}">
                            <g:if test="${editable && !(SurveyConfigVendor.findByVendorAndSurveyConfig(vendor, surveyConfig))}">
                                <g:link type="button" class="${Btn.ICON.SIMPLE}" controller="survey" action="processLinkSurveyVendor" id="${params.id}"
                                        params="[addVendor: vendor.id, surveyConfigID: surveyConfig.id]"><g:message
                                        code="surveyVendors.linkVendor"/></g:link>

                            </g:if>
                            <g:else>
                                <g:link type="button" class="${Btn.NEGATIVE}" controller="survey" action="processLinkSurveyVendor" id="${params.id}"
                                        params="[removeVendor: vendor.id, surveyConfigID: surveyConfig.id]"><g:message
                                        code="surveyVendors.unlinkVendor"/></g:link>

                            </g:else>
                        </g:if>
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem == 'unLinkSurveyVendor'}">
                    <td class="right aligned">
                        <g:if test="${editable && (SurveyConfigVendor.findByVendorAndSurveyConfig(vendor, surveyConfig))}">
                            <g:link type="button" class="${Btn.NEGATIVE}" controller="survey" action="processLinkSurveyVendor" id="${params.id}"
                                    params="[removeVendor: vendor.id, surveyConfigID: surveyConfig.id]"><g:message
                                    code="surveyVendors.unlinkVendor"/></g:link>

                        </g:if>
                    </td>
                </g:if>
                <g:if test="${tmplConfigItem == 'selectSurveyVendorResult'}">
                    <td class="center aligned">
                        <g:set var="surveyVendor" value="${SurveyVendorResult.findBySurveyConfigAndParticipantAndVendor(surveyConfig, participant, vendor)}"/>
                        <g:if test="${editable}">
                            <g:if test="${surveyVendor}">
                                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}"
                                        params="${parame + [viewTab: 'vendorSurvey', actionsForSurveyVendors: 'removeSurveyVendor', vendorId: vendor.id]}">
                                    <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:link controller="${controllerName}" action="${actionName}" id="${params.id}"
                                        params="${parame + [viewTab: 'vendorSurvey', actionsForSurveyVendors: 'addSurveyVendor', vendorId: vendor.id]}">
                                    <i class="${Icon.SYM.CHECKBOX} large"></i>
                                </g:link>
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:if test="${surveyVendor}">
                                <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                            </g:if>
                        </g:else>
                    </td>
                </g:if>

         %{--       <g:if test="${tmplConfigItem == 'surveyVendorsComments'}">
                    <g:set var="surveyVendorResult"
                           value="${SurveyVendorResult.findByParticipantAndSurveyConfigAndVendor(participant, surveyConfig, vendor)}"/>
                    <g:if test="${surveyVendorResult}">
                        <td>
                            <ui:xEditable owner="${surveyVendorResult}" type="textarea" field="comment"/>
                        </td>
                        <td>
                            <g:if test="${contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_PRO)}">
                                <ui:xEditable owner="${surveyVendorResult}" type="textarea" field="ownerComment"/>
                            </g:if>
                            <g:else>
                                <ui:xEditable owner="${surveyVendorResult}" type="textarea" field="participantComment"/>
                            </g:else>
                        </td>
                    </g:if>
                    <g:else>
                        <td></td>
                        <td></td>
                    </g:else>
                </g:if>--}%

            </g:each><!-- tmplConfigShow -->
            </tr>
        </g:each><!-- vendorList -->
    </tbody>
</table>

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#vendorListToggler').change(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedVendors]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedVendors]").prop('checked', false)
            }
        })
    </laser:script>

</g:if>