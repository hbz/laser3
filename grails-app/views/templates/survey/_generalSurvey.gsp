<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.DateUtils; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.survey.SurveyOrg; de.laser.properties.PropertyDefinition; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.RefdataValue" %>

<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant)}"/>

        <div class="ui card">
            <div class="content">
                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i+1])}
                            </dt>
                            <dd>
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text" overwriteEditable="${false}"/>

                                <g:if test="${surveyUrl.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
                                          data-content="${surveyUrl.urlComment}">
                                        <i class="${Icon.TOOLTIP.INFO}"></i>
                                    </span>
                                </g:if>
                                <ui:linkWithIcon href="${surveyUrl.url}"/>
                            </dd>
                        </dl>
                    </g:each>
            </div>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'License', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>

        <div class="ui card">
            <laser:render template="/survey/linksProviderOrLicense"
                      model="[linkType: 'Provider', surveyInfo: surveyInfo, editable: editable, surveyConfig  : surveyConfig]"/>
        </div>


