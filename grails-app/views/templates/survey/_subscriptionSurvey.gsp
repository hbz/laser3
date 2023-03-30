<%@ page import="de.laser.CustomerTypeService; de.laser.utils.DateUtils; java.text.SimpleDateFormat; java.text.DateFormat; de.laser.storage.PropertyStore; de.laser.survey.SurveyConfigProperties; de.laser.SubscriptionPackage; de.laser.survey.SurveyOrg; de.laser.survey.SurveyConfig; de.laser.DocContext; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.properties.PropertyDefinition; de.laser.Subscription; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataCategory; de.laser.Platform; de.laser.SubscriptionPackage; de.laser.Org" %>
<laser:serviceInjection/>
<g:set var="surveyOrg"
       value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)}"/>

<div class="ui stackable grid">
    <div class="eleven wide column">
        <g:if test="${controllerName == 'survey' && actionName == 'show'}">

            <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

            <g:link class="ui icon button right floated" controller="subscription" action="members"
                    id="${subscription.id}">
                <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

                <div class="ui blue circular label">
                    ${countParticipants.subMembers}
                </div>
            </g:link>

            <g:link class="ui icon button right floated" controller="survey" action="surveyParticipants"
                    id="${surveyConfig.surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id]">
                <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

                <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
            </g:link>

            <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                ( ${countParticipants.subMembersWithMultiYear}
                ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
            </g:if>
            <br><br><br>
        </g:if>

        <div class="ui card ">
            <div class="content">

                <g:if test="${accessService.checkCtxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO) && surveyOrg}">
                    <dl>
                        <dt class="control-label">
                            ${message(code: 'surveyOrg.ownerComment.label', args: [institution.sortname])}
                        </dt>
                        <dd><ui:xEditable owner="${surveyOrg}" field="ownerComment" type="textarea"/></dd>

                    </dl>
                </g:if>

                <g:if test="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-content="${message(code: "surveyconfig.scheduledStartDate.comment")}">
                                    ${message(code: 'surveyconfig.scheduledStartDate.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><ui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                                                 overwriteEditable="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-content="${message(code: "surveyconfig.scheduledEndDate.comment")}">
                                    ${message(code: 'surveyconfig.scheduledEndDate.label')}
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </dt>
                            <dd><ui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                                                 overwriteEditable="${contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/></dd>

                        </dl>
                    </g:if>
                    <dl>
                        <dt class="control-label">
                            <div class="ui icon la-popup-tooltip la-delay"
                                 data-content="${message(code: "surveyconfig.internalComment.comment")}">
                                ${message(code: 'surveyconfig.internalComment.label')}
                                <i class="question small circular inverted icon"></i>
                            </div>
                        </dt>
                        <dd><ui:xEditable owner="${surveyConfig}" field="internalComment" type="textarea"/></dd>

                    </dl>

                    <ui:card message="${message(code: 'surveyconfig.url.plural.label')}" href="#surveyUrls"
                             editable="${editable}">
                        <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                            <dl>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.url.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                    <g:if test="${surveyUrl.url}">
                                        <ui:linkWithIcon href="${surveyUrl.url}"/>
                                    </g:if>

                                </dd>
                                <dt class="control-label">
                                    ${message(code: 'surveyconfig.urlComment.label', args: [i+1])}
                                </dt>
                                <dd>
                                    <ui:xEditable owner="${surveyUrl}" field="urlComment" type="textarea"/>
                                </dd>

                                <div class="right aligned">
                                    <g:if test="${editable}">
                                        <span class="la-popup-tooltip la-delay"
                                              data-content="${message(code: 'default.button.delete.label')}">
                                            <g:link class="ui negative icon button la-modern-button  la-selectable-button js-open-confirm-modal"
                                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.generic", args: [message(code: 'surveyconfig.url.label', args: [i+1])])}"
                                                    data-confirm-term-how="delete"
                                                    controller="survey" action="addSurveyUrl"
                                                    params="${[deleteSurveyUrl: surveyUrl.id, surveyConfigID: surveyConfig.id, id: surveyInfo.id]}"
                                                    role="button"
                                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                                <i class="trash alternate outline icon"></i>
                                            </g:link>
                                        </span>
                                    </g:if>
                                </div>

                            </dl>
                        </g:each>

                        <g:if test="${editable}">
                            <g:render template="/survey/surveyUrlsModal"/>
                        </g:if>
                    </ui:card>

                    <br/>

                    <div class="ui la-tab-with-js">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                            <div class="ui top attached tabular menu">
                                <a class="item active" data-tab="comment">
                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-content="${message(code: "surveyconfig.comment.comment")}">
                                        ${message(code: 'surveyconfig.comment.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </a>
                                <a class="item" data-tab="commentForNewParticipants">
                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-content="${message(code: "surveyconfig.commentForNewParticipants.comment")}">
                                        ${message(code: 'surveyconfig.commentForNewParticipants.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </a>
                            </div>

                            <div class="ui bottom attached tab segment active" data-tab="comment">
                                <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                    <div id="commentDiv">
                                        <div id="comment">${raw(surveyConfig.comment)}</div>

                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            wysiwyg.initEditor('#commentDiv #comment');
                                        </laser:script>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui form">
                                        <div class="field">
                                            <textarea class="la-textarea-resize-vertical" name="comment"
                                                      rows="15">${surveyConfig.comment}</textarea>
                                        </div>
                                    </div>
                                </g:else>
                                <br>

                                <div class="left aligned">
                                    <button type="submit"
                                            class="ui button">${message(code: 'default.button.save_changes')}</button>
                                </div>
                            </div>

                            <div class="ui bottom attached tab segment" data-tab="commentForNewParticipants">
                                <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                    <div id="commentForNewParticipantsDiv">
                                        <div id="commentForNewParticipants">${raw(surveyConfig.commentForNewParticipants)}</div>

                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            wysiwyg.initEditor('#commentForNewParticipantsDiv #commentForNewParticipants');
                                        </laser:script>
                                    </div>
                                </g:if>
                                <g:else>
                                    <div class="ui form">
                                        <div class="field">
                                            <textarea class="la-textarea-resize-vertical" name="commentForNewParticipants"
                                                      rows="15">${surveyConfig.commentForNewParticipants}</textarea>
                                        </div>
                                    </div>
                                </g:else>
                                <br>

                                <div class="left aligned">
                                    <button type="submit"
                                            class="ui button">${message(code: 'default.button.save_changes')}</button>
                                </div>

                            </div>
                        </g:form>

                    </div>

                </g:if>
                <g:else>
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.scheduledStartDate.label')}
                            </dt>
                            <dd><ui:xEditable owner="${surveyConfig}" field="scheduledStartDate" type="date"
                                                 overwriteEditable="${false}"/>
                            </dd>
                        </dl>
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.scheduledEndDate.label')}
                            </dt>
                            <dd><ui:xEditable owner="${surveyConfig}" field="scheduledEndDate" type="date"
                                                 overwriteEditable="${false}"/></dd>

                        </dl>
                    </g:if>

                    <g:each in="${surveyConfig.surveyUrls}" var="surveyUrl" status="i">
                        <dl>
                            <dt class="control-label">
                                ${message(code: 'surveyconfig.url.label', args: [i+1])}
                            </dt>
                            <dd>
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text"/>
                                overwriteEditable="${false}"/>

                                <g:if test="${surveyUrl.urlComment}">
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${surveyUrl.urlComment}">
                                        <i class="info circle icon"></i>
                                    </span>
                                </g:if>
                                <ui:linkWithIcon href="${surveyUrl.url}"/>
                            </dd>
                        </dl>
                    </g:each>

                    <g:if test="${subscription}">
                        <div class="ui form la-padding-left-07em">
                            <div class="field">
                                <label>
                                    <g:message code="surveyConfigsInfo.comment"/>
                                </label>
                                <g:if test="${surveyConfig.comment}">
                                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                        <div id="comment-wrapper-${surveyConfig.id}">
                                            <article id="comment-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                                ${raw(surveyConfig.comment)}
                                            </article>
                                            <laser:script file="${this.getGroovyPageFileName()}">
                                                wysiwyg.analyzeNote_TMP( $("#comment-${surveyConfig.id}"), $("#comment-wrapper-${surveyConfig.id}"), true );
                                            </laser:script>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <textarea class="la-textarea-resize-vertical" readonly="readonly"
                                              rows="1">${surveyConfig.comment}</textarea>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:message code="surveyConfigsInfo.comment.noComment"/>
                                </g:else>
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="ui form la-padding-left-07em">
                            <div class="field">
                                <label>
                                    <g:message code="surveyConfigsInfo.comment"/>
                                </label>
                                <g:if test="${surveyConfig.commentForNewParticipants}">
                                    <g:if test="${surveyConfig.dateCreated > DateUtils.getSDF_yyyyMMdd().parse('2023-01-12')}">
                                        <div id="commentForNewParticipants-wrapper-${surveyConfig.id}">
                                            <article id="commentForNewParticipants-${surveyConfig.id}" class="ui segment trumbowyg-editor trumbowyg-reset-css" style="margin:0; padding:0.5em 1em; box-shadow:none;">
                                                ${raw(surveyConfig.commentForNewParticipants)}
                                            </article>
                                            <laser:script file="${this.getGroovyPageFileName()}">
                                                wysiwyg.analyzeNote_TMP( $("#commentForNewParticipants-${surveyConfig.id}"), $("#commentForNewParticipants-wrapper-${surveyConfig.id}"), true );
                                            </laser:script>
                                        </div>
                                    </g:if>
                                    <g:else>
                                        <textarea class="la-textarea-resize-vertical" readonly="readonly"
                                              rows="1">${surveyConfig.commentForNewParticipants}</textarea>
                                    </g:else>
                                </g:if>
                                <g:else>
                                    <g:message code="surveyConfigsInfo.comment.noComment"/>
                                </g:else>
                            </div>
                        </div>
                    </g:else>
                </g:else>

                <br/>

            </div>
        </div>

        <g:if test="${customProperties}">
                    <div class="ui card">
                        <div class="content">
                            <div class="ui accordion la-accordion-showMore js-propertiesCompareInfo-accordion">
                                <div class="item">
                                    <div class="title">
                                        <div
                                                class="ui button icon blue la-modern-button la-popup-tooltip la-delay right floated "
                                                data-content="<g:message code="survey.subscription.propertiesChange.show"/>">
                                            <i class="ui angle double down large icon"></i>
                                        </div>
                                        <laser:script file="${this.getGroovyPageFileName()}">
                                            $('.js-propertiesCompareInfo-accordion')
                                              .accordion({
                                                onOpen: function() {
                                                  $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                                code="survey.subscription.propertiesChange.hide"/> ')
                                                    },
                                                    onClose: function() {
                                                      $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                                code="survey.subscription.propertiesChange.show"/> ')
                                                    }
                                                  })
                                                ;
                                        </laser:script>
                                        <div class="content">
                                            <h2 class="ui header">
                                                ${message(code: 'survey.subscription.propertiesChange')}
                                            </h2>
                                        </div>
                                    </div>

                                    <div class="content" id="propertiesCompareInfo">
                                        <div class="ui stackable grid container">
                                            <laser:render template="/templates/survey/propertiesCompareInfo"
                                                      model="[customProperties: customProperties]"/>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
            </g:if>



        <div class="ui card">
            <div class="content">
                <g:if test="${!subscription}">
                    <div>%{-- needed for css --}%
                        <ui:headerTitleIcon type="Subscription"/>
                        <g:link class="ui button right floated" controller="public" action="gasco"
                                params="${[q: '"' + surveyConfig.subscription.name + '"']}">
                            GASCO-Monitor
                        </g:link>
                        <h2 class="ui icon header">
                            <g:link controller="public" action="gasco"
                                    params="${[q: '"' + surveyConfig.subscription.name + '"']}">
                                ${surveyConfig.subscription.name}
                            </g:link>
                        </h2>
                    </div>
                </g:if>
                <g:else>
                    <div class="ui accordion la-accordion-showMore js-subscription-info-accordion">
                        <div class="item">
                            <div class="title">
                                <div
                                        class="ui button icon blue la-modern-button la-delay right floated">
                                    <i class="ui angle double down large icon"></i>
                                </div>
                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('.js-subscription-info-accordion')
                                      .accordion({
                                        onOpen: function() {
                                          $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                        code="surveyConfigsInfo.subscriptionInfo.hide"/> ')
                                        },
                                        onClose: function() {
                                          $(this).siblings('.title').children('.button').attr('data-content','<g:message
                                        code="surveyConfigsInfo.subscriptionInfo.show"/> ')
                                        }
                                      })
                                    ;
                                </laser:script>

                                <g:if test="${subscription}">
                                    <ui:headerTitleIcon type="Subscription"/>
                                    <h2 class="ui icon header">
                                        <g:link controller="subscription" action="show" id="${subscription.id}">
                                            <g:message code="surveyConfigsInfo.subscriptionInfo.show"/>
                                        </g:link>
                                    </h2>
                                    <ui:auditInfo auditable="[subscription, 'name']"/>
                                </g:if>
                            </div>

                            <div class="content" id="subscription-info">
                                <div class="ui stackable grid container">
                                    <div class="ten wide column ">
                                        <dl>
                                            <dt class="control-label">${message(code: 'default.status.label')}</dt>
                                            <dd>${subscription.status.getI10n('value')}</dd>
                                            <dd><ui:auditInfo auditable="[subscription, 'status']"/></dd>
                                        </dl>
                                        <dl>
                                            <dt class="control-label">${message(code: 'subscription.kind.label')}</dt>
                                            <dd>${subscription.kind?.getI10n('value')}</dd>
                                            <dd><ui:auditInfo auditable="[subscription, 'kind']"/></dd>
                                        </dl>
                                        <dl>
                                            <dt class="control-label">${message(code: 'subscription.form.label')}</dt>
                                            <dd>${subscription.form?.getI10n('value')}</dd>
                                            <dd><ui:auditInfo auditable="[subscription, 'form']"/></dd>
                                        </dl>
                                        <dl>
                                            <dt class="control-label">${message(code: 'subscription.resource.label')}</dt>
                                            <dd>${subscription.resource?.getI10n('value')}</dd>
                                            <dd><ui:auditInfo auditable="[subscription, 'resource']"/></dd>
                                        </dl>
                                        <dl>
                                            <dt class="control-label">${message(code: 'subscription.hasPerpetualAccess.label')}</dt>
                                            <dd>${subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')}</dd>
                                            <dd><ui:auditInfo auditable="[subscription, 'hasPerpetualAccess']"/></dd>
                                        </dl>
                                        <dl>
                                            <dt class="control-label">
                                                <g:message code="default.identifiers.label"/>
                                            </dt>
                                            <dd>

                                                <div class="ui list ">
                                                <g:each in="${subscription.ids?.sort { it.ns.ns }}"
                                                        var="id">
                                                    <div class="item js-copyTriggerParent">
                                                       <span class="ui small basic image label js-copyTrigger la-popup-tooltip la-delay"
                                                             data-position="top center" data-content="${message(code: 'tooltip.clickToCopySimple')}">
                                                           <i class="la-copy grey icon la-js-copyTriggerIcon" aria-hidden="true"></i>
                                                        ${id.ns.ns}: <div class="detail js-copyTopic">${id.value}</div>
                                                    </span>
                                                    </div>
                                                </g:each>
                                                </div>
                                            </dd>
                                        </dl>
                                    </div>


                                    <div class="six wide column">
                                        %{--<g:if test="${subscription.packages}">
                                            <table class="ui three column la-selectable table">
                                                <g:each in="${subscription.packages.sort { it.pkg.name }}" var="sp">
                                                    <tr>
                                                        <th scope="row"
                                                            class="control-label la-js-dont-hide-this-card">${message(code: 'subscription.packages.label')}</th>
                                                        <td>
                                                            <g:link controller="package" action="show"
                                                                    id="${sp.pkg.id}">${sp.pkg.name}</g:link>

                                                            <g:if test="${sp.pkg.contentProvider}">
                                                                (${sp.pkg.contentProvider.name})
                                                            </g:if>
                                                        </td>
                                                        <td class="right aligned">
                                                        </td>

                                                    </tr>
                                                </g:each>
                                            </table>
                                        </g:if>--}%

                                        <g:if test="${visibleOrgRelations}">

                                            <laser:render template="/templates/links/orgLinksAsList"
                                                      model="${[roleLinks    : visibleOrgRelations,
                                                                roleObject   : subscription,
                                                                roleRespValue: 'Specific subscription editor',
                                                                editmode     : false,
                                                                showPersons  : false
                                                      ]}"/>

                                        </g:if>
                                    </div>
                                </div>


                                <br/>

                                <div class="ui form">

                                    <g:set var="oldEditable" value="${editable}"/>
                                    <div id="subscription-properties" style="margin: 1em 0">
                                        <g:set var="editable" value="${false}" scope="request"/>
                                        <g:set var="editable" value="${false}" scope="page"/>
                                        <laser:render template="/subscription/properties" model="${[
                                                subscription: subscription, calledFromSurvey: true
                                        ]}"/>

                                    </div>

                                    <g:set var="editable" value="${oldEditable ?: false}" scope="page"/>
                                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                                </div>

                                <div class="ui card">
                                    <div class="content">
                                        <h2 class="ui header">
                                            <g:message code="license.plural"/>
                                        </h2>
                                        <g:if test="${links && links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}">
                                            <table class="ui fixed table">
                                                <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}"
                                                        var="link">
                                                    <tr><g:set var="pair" value="${link.getOther(subscription)}"/>
                                                        <th scope="row"
                                                            class="control-label la-js-dont-hide-this-card">${pair.licenseCategory?.getI10n("value")}</th>
                                                        <td>
                                                            <g:link controller="license" action="show" id="${pair.id}">
                                                                ${pair.reference} (${pair.status.getI10n("value")})
                                                            </g:link>
                                                            <g:formatDate date="${pair.startDate}"
                                                                          format="${message(code: 'default.date.format.notime')}"/>-<g:formatDate
                                                                date="${pair.endDate}"
                                                                format="${message(code: 'default.date.format.notime')}"/><br/>
                                                            <g:set var="comment"
                                                                   value="${DocContext.findByLink(link)}"/>
                                                            <g:if test="${comment}">
                                                                <em>${comment.owner.content}</em>
                                                            </g:if>
                                                        </td>
                                                        <td class="right aligned">
                                                            <g:if test="${pair.propertySet}">
                                                                <div id="derived-license-properties-toggle${link.id}"
                                                                        class="ui icon blue button la-modern-button la-js-dont-hide-button la-popup-tooltip la-delay"
                                                                        data-content="${message(code: 'subscription.details.viewLicenseProperties')}">
                                                                    <i class="ui angle double down icon"></i>
                                                                </div>
                                                                <laser:script file="${this.getGroovyPageFileName()}">
                                                                    $("#derived-license-properties-toggle${link.id}").on('click', function() {
                                                        $("#derived-license-properties${link.id}").transition('slide down');
                                                        //$("#derived-license-properties${link.id}").toggleClass('hidden');

                                                        if ($("#derived-license-properties${link.id}").hasClass('visible')) {
                                                            $(this).html('<i class="ui angle double down icon"></i>')
                                                        } else {
                                                            $(this).html('<i class="ui angle double up icon"></i>')
                                                        }
                                                    })
                                                                </laser:script>
                                                            </g:if>
                                                        </td>
                                                    </tr>
                                                    <g:if test="${pair.propertySet}">
                                                        <tr>
                                                            <td colspan="3"><div id="${link.id}Properties"></div></td>
                                                        </tr>
                                                    </g:if>
                                                </g:each>
                                            </table>
                                        </g:if>

                                    </div>%{-- .content --}%
                                </div>

                            </div>
                        </div>
                    </div>%{-- Accordion --}%
                </g:else>
            </div>
        </div>

        <g:if test="${subscription && subscription.packages}">
            <div id="packages" class="la-inline-lists"></div>
        </g:if>

        <g:if test="${subscription && subscription.packages}">
            <div id="ieInfos" class="la-inline-lists"></div>
        </g:if>

        <g:if test="${subscription && subscription.packages}">
            <%
                Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription])
                if(!subscribedPlatforms) {
                    subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription or ie.subscription = (select s.instanceOf from Subscription s where s = :subscription)", [subscription: subscription])
                }
                boolean areStatsAvailable = subscriptionService.areStatsAvailable(subscribedPlatforms)
            %>
            <g:if test="${subscribedPlatforms && areStatsAvailable}">
                <div class="ui card">
                    <div class="content">
                        <div id="statsInfos" class="ui accordion la-accordion-showMore js-subscription-info-accordion">
                            <div class="item">
                                <div class="title">
                                    <div
                                            class="ui button icon blue la-modern-button la-delay right floated ">
                                        <i class="ui angle double down large icon"></i>
                                    </div>

                                    <i aria-hidden="true" class="circular chart bar green outline inverted icon"></i>

                                    <h2 class="ui icon header la-clear-before la-noMargin-top">
                                        <g:link controller="subscription" action="stats" target="_blank"
                                                id="${subscription.id}"><g:message code="surveyConfigsInfo.stats.show"/></g:link>
                                    </h2>
                                </div>
                                <div class="content">
                                    <g:link controller="subscription" action="stats" target="_blank"
                                            id="${subscription.id}" class="ui button">
                                        <g:message code="renewEntitlementsWithSurvey.stats.button"/>
                                    </g:link>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>
            </g:if>
        </g:if>

    </div>

    <aside class="five wide column la-sidekick">
        <div class="ui one cards">

            <div id="container-documents">
                <g:render template="/survey/surveyLinkCard"/>
            </div>

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-tasks">
                    <laser:render template="/templates/tasks/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>

                </div>

                <div id="container-notes">
                    <laser:render template="/templates/notes/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: accessService.checkCtxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_EDITOR')]}"/>
                </div>

            </g:if>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>

        </div>
    </aside>

</div><!-- .grid -->

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
    <g:set var="costItemSurvey"
           value="${surveyOrg ? CostItem.findBySurveyOrg(surveyOrg) : null}"/>

    <g:if test="${surveyInfo.owner.id != institution.id && ((costItemSums && costItemSums.subscrCosts) || costItemSurvey)}">
        <g:set var="showCostItemSurvey" value="${true}"/>

        <div class="ui card la-time-card">

            <div class="content">
                <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
            </div>

            <div class="content">
                <%
                    def elementSign = 'notSet'
                    String icon = ''
                    String dataTooltip = ""
                %>

                <table class="ui celled compact la-js-responsive-table la-table-inCard table">
                    <thead>
                    <tr>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.oldPrice"/>
                        </th>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.newPrice"/>
                        </th>
                        <th rowspan="2">Diff.</th>
                    </tr>
                    <tr>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.costInBillingCurrency"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.taxRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.costInBillingCurrencyAfterTax"/></th>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.costInBillingCurrency"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.taxRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.costInBillingCurrencyAfterTax"/></th>

                    </tr>
                    </thead>


                    <tbody class="top aligned">
                    <g:if test="${costItemSums && costItemSums.subscrCosts}">
                        <g:each in="${costItemSums.subscrCosts.sort { it.costItemElement }}" var="costItem">
                            <tr>
                                <td>
                                    <%
                                        elementSign = 'notSet'
                                        icon = ''
                                        dataTooltip = ""
                                        if (costItem.costItemElementConfiguration) {
                                            elementSign = costItem.costItemElementConfiguration
                                        }
                                        switch (elementSign) {
                                            case RDStore.CIEC_POSITIVE:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                                icon = '<i class="plus green circle icon"></i>'
                                                break
                                            case RDStore.CIEC_NEGATIVE:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                                icon = '<i class="minus red circle icon"></i>'
                                                break
                                            case RDStore.CIEC_NEUTRAL:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                                icon = '<i class="circle yellow icon"></i>'
                                                break
                                            default:
                                                dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                                icon = '<i class="question circle icon"></i>'
                                                break
                                        }
                                    %>
                                    <span class="la-popup-tooltip la-delay" data-position="right center"
                                          data-content="${dataTooltip}">${raw(icon)}</span>

                                    ${costItem.costItemElement?.getI10n('value')}
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrency}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${(costItem.billingCurrency?.getI10n('value').split('-')).first()}
                                </td>
                                <td>${costItem.taxKey ? costItem.taxKey.taxType?.getI10n("value") + " (" + costItem.taxKey.taxRate + "%)" : ''}</td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${(costItem.billingCurrency?.getI10n('value').split('-')).first()}

                                    <g:if test="${costItem.startDate || costItem.endDate}">
                                        <br/>(${formatDate(date: costItem.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItem.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>
                                </td>

                                <g:if test="${costItemSurvey && costItemSurvey.costItemElement == costItem.costItemElement}">
                                    <g:set var="showCostItemSurvey" value="${false}"/>
                                    <td>
                                        <%
                                            elementSign = 'notSet'
                                            icon = ''
                                            dataTooltip = ""
                                            if (costItemSurvey.costItemElementConfiguration) {
                                                elementSign = costItemSurvey.costItemElementConfiguration
                                            }
                                            switch (elementSign) {
                                                case RDStore.CIEC_POSITIVE:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                                    icon = '<i class="plus green circle icon"></i>'
                                                    break
                                                case RDStore.CIEC_NEGATIVE:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                                    icon = '<i class="minus red circle icon"></i>'
                                                    break
                                                case RDStore.CIEC_NEUTRAL:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                                    icon = '<i class="circle yellow icon"></i>'
                                                    break
                                                default:
                                                    dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                                    icon = '<i class="question circle icon"></i>'
                                                    break
                                            }
                                        %>
                                        <span class="la-popup-tooltip la-delay" data-position="right center"
                                              data-content="${dataTooltip}">${raw(icon)}</span>

                                        ${costItemSurvey.costItemElement?.getI10n('value')}
                                    </td>
                                    <td>
                                        <strong>
                                            <g:formatNumber
                                                    number="${costItemSurvey.costInBillingCurrency}"
                                                    minFractionDigits="2" maxFractionDigits="2"
                                                    type="number"/>
                                        </strong>

                                        ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}
                                    </td>
                                    <td>${costItemSurvey.taxKey ? costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)" : ''}</td>
                                    <td>
                                        <strong>
                                            <g:formatNumber
                                                    number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                    minFractionDigits="2" maxFractionDigits="2"
                                                    type="number"/>
                                        </strong>

                                        ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}


                                        <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                            <br/>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                        </g:if>

                                        <g:if test="${costItemSurvey.costDescription}">
                                            <br/>

                                            <div class="ui icon la-popup-tooltip la-delay"
                                                 data-position="right center"
                                                 data-variation="tiny"
                                                 data-content="${costItemSurvey.costDescription}">
                                                <i class="question small circular inverted icon"></i>
                                            </div>
                                        </g:if>
                                    </td>

                                </g:if>
                                <g:else>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </g:else>

                                <td>
                                    <g:if test="${costItemSurvey && costItemSurvey.costItemElement == costItem.costItemElement}">

                                        <g:set var="oldCostItem"
                                               value="${costItem.costInBillingCurrency ?: 0.0}"/>

                                        <g:set var="newCostItem"
                                               value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                        <strong><g:formatNumber
                                                number="${(newCostItem - oldCostItem)}"
                                                minFractionDigits="2" maxFractionDigits="2" type="number"/>
                                            <br/>
                                            (<g:formatNumber
                                                    number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/>%)</strong>
                                    </g:if>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>

                    <g:if test="${showCostItemSurvey && costItemSurvey}">
                        <tr>

                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>

                            <td>
                                <%
                                    elementSign = 'notSet'
                                    icon = ''
                                    dataTooltip = ""
                                    if (costItemSurvey.costItemElementConfiguration) {
                                        elementSign = costItemSurvey.costItemElementConfiguration
                                    }
                                    switch (elementSign) {
                                        case RDStore.CIEC_POSITIVE:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                            icon = '<i class="plus green circle icon"></i>'
                                            break
                                        case RDStore.CIEC_NEGATIVE:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                            icon = '<i class="minus red circle icon"></i>'
                                            break
                                        case RDStore.CIEC_NEUTRAL:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                            icon = '<i class="circle yellow icon"></i>'
                                            break
                                        default:
                                            dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                            icon = '<i class="question circle icon"></i>'
                                            break
                                    }
                                %>

                                <span class="la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${dataTooltip}">${raw(icon)}</span>


                                ${costItemSurvey.costItemElement?.getI10n('value')}

                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItemSurvey.costInBillingCurrency}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}
                            </td>
                            <td>${costItemSurvey.taxKey ? costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)" : ''}</td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${(costItemSurvey.billingCurrency?.getI10n('value').split('-')).first()}

                                <g:set var="newCostItem"
                                       value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                    <br/>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                                </g:if>

                                <g:if test="${costItemSurvey.costDescription}">
                                    <br/>

                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-position="right center"
                                         data-variation="tiny"
                                         data-content="${costItemSurvey.costDescription}">
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </g:if>
                            </td>

                            <td>
                            </td>
                        </tr>
                    </g:if>

                    </tbody>
                </table>
            </div>
        </div>
    </g:if>

    <g:if test="${surveyInfo.owner.id == institution.id && costItemSums.consCosts}">
        <div class="ui card la-dl-no-table">
            <div class="content">
                <g:if test="${costItemSums.ownCosts}">
                    <g:if test="${(contextOrg.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                        <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.ownCosts')}</h2>
                        <laser:render template="/subscription/financials" model="[data: costItemSums.ownCosts]"/>
                    </g:if>
                </g:if>
                <g:if test="${costItemSums.consCosts}">
                    <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.consCosts')}</h2>
                    <laser:render template="/subscription/financials" model="[data: costItemSums.consCosts]"/>
                </g:if>
            </div>
        </div>
    </g:if>
</g:if>

<g:if test="${controllerName == 'survey' && actionName == 'show'}">
    <g:set var="surveyProperties" value="${surveyConfig.getSortedSurveyConfigProperties()}"/>

    <ui:greySegment>

        <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'surveyProperty.selected.label')} <ui:totalNumber
                total="${surveyProperties.size()}"/></h4>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.name')}</th>
                <th>${message(code: 'surveyProperty.expl.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyProperty.mandatoryProperty')}</th>
                <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING && surveyProperties}">
                    <th>${message(code: 'default.actions.label')}</th>
                </g:if>
            </tr>
            </thead>

            <tbody>
            <g:each in="${surveyProperties}" var="surveyPropertyConfig" status="i">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyPropertyConfig.surveyProperty.getI10n('name')}

                        <g:if test="${surveyPropertyConfig.surveyProperty.tenant?.id == contextService.getOrg().id}">
                            <i class='shield alternate icon'></i>
                        </g:if>

                        <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                    </td>

                    <td>
                        <g:if test="${surveyPropertyConfig.surveyProperty.getI10n('expl')}">
                            ${surveyPropertyConfig.surveyProperty.getI10n('expl')}
                        </g:if>
                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyPropertyConfig.surveyProperty.type)}
                        <g:if test="${surveyPropertyConfig.surveyProperty.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyPropertyConfig.surveyProperty.refdataCategory)}"
                                    var="refdataValue">
                                <g:set var="refdataValues"
                                       value="${refdataValues + refdataValue?.getI10n('value')}"/>
                            </g:each>
                            <br/>
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>

                    <td>
                        <g:set var="surveyPropertyMandatoryEditable"
                               value="${(editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                                       (surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL || (surveyInfo.type == RDStore.SURVEY_TYPE_RENEWAL && surveyPropertyConfig.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION)))}"/>
                        <g:form action="setSurveyPropertyMandatory" method="post" class="ui form"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveyConfigProperties: surveyPropertyConfig.id]">

                            <div class="ui checkbox">
                                <input type="checkbox"
                                       onchange="${surveyPropertyMandatoryEditable ? 'this.form.submit()' : ''}" ${!surveyPropertyMandatoryEditable ? 'readonly="readonly" disabled="true"' : ''}
                                       name="mandatoryProperty" ${surveyPropertyConfig.mandatoryProperty ? 'checked' : ''}>
                            </div>
                        </g:form>
                    </td>
                    <g:if test="${editable && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING &&
                            SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyConfig, surveyPropertyConfig.surveyProperty)
                            && ((PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id != surveyPropertyConfig.surveyProperty.id) || surveyInfo.type != RDStore.SURVEY_TYPE_RENEWAL)}">
                        <td>
                            <g:link class="ui icon negative button la-modern-button js-open-confirm-modal"
                                    data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.surveyElements", args: [surveyPropertyConfig.surveyProperty.getI10n('name')])}"
                                    data-confirm-term-how="delete"
                                    controller="survey" action="deleteSurveyPropFromConfig"
                                    id="${surveyPropertyConfig.id}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </td>
                    </g:if>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <g:if test="${editable && properties && surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
                    <td colspan="6">
                        <g:form action="addSurveyPropToConfig" controller="survey" method="post" class="ui form">
                            <g:hiddenField name="id" value="${surveyInfo.id}"/>
                            <g:hiddenField name="surveyConfigID" value="${surveyConfig.id}"/>

                            <div class="field required">
                                <label>${message(code: 'surveyConfigs.property')} <g:message
                                        code="messageRequiredField"/></label>
                                <ui:dropdown name="selectedProperty"

                                                class="la-filterPropDef"
                                                from="${properties}"
                                                iconWhich="shield alternate"
                                                optionKey="${{ "${it.id}" }}"
                                                optionValue="${{ it.getI10n('name') }}"
                                                noSelection="${message(code: 'default.search_for.label', args: [message(code: 'surveyProperty.label')])}"
                                                required=""/>

                            </div>
                            <input type="submit" class="ui button"
                                   value="${message(code: 'surveyConfigsInfo.add.button')}"/>

                        </g:form>
                    </td>
                </g:if>
            </tr>
            </tfoot>

        </table>

    </ui:greySegment>
</g:if>

<g:if test="${surveyResults}">

    <ui:greySegment>
        <h3 class="ui header"><g:message code="surveyConfigsInfo.properties"/>
        <ui:totalNumber total="${surveyResults.size()}"/>
        </h3>

        <table class="ui celled sortable table la-js-responsive-table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'surveyProperty.label')}</th>
                <th>${message(code: 'default.type.label')}</th>
                <th>${message(code: 'surveyResult.result')}</th>
                <th>
                    <g:if test="${accessService.checkCtxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
                        ${message(code: 'surveyResult.participantComment')}
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
                <th>
                    <g:if test="${accessService.checkCtxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
                        ${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                    <g:else>
                        ${message(code: 'surveyResult.commentOnlyForParticipant')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForParticipant.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:else>
                </th>
            </tr>
            </thead>
            <g:each in="${surveyResults}" var="surveyResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${surveyResult.type.getI10n('name')}

                        <g:if test="${surveyResult.type.getI10n('expl')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${surveyResult.type.getI10n('expl')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:set var="surveyConfigProperties"
                               value="${SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(surveyResult.surveyConfig, surveyResult.type)}"/>
                        <g:if test="${surveyConfigProperties && surveyConfigProperties.mandatoryProperty}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                  data-content="${message(code: 'default.mandatory.tooltip')}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                    <td>
                        ${PropertyDefinition.getLocalizedValue(surveyResult.type.type)}
                        <g:if test="${surveyResult.type.isRefdataValueType()}">
                            <g:set var="refdataValues" value="${[]}"/>
                            <g:each in="${RefdataCategory.getAllRefdataValues(surveyResult.type.refdataCategory)}"
                                    var="refdataValue">
                                <g:if test="${refdataValue.getI10n('value')}">
                                    <g:set var="refdataValues"
                                           value="${refdataValues + refdataValue.getI10n('value')}"/>
                                </g:if>
                            </g:each>
                            <br/>
                            (${refdataValues.join('/')})
                        </g:if>
                    </td>
                    <g:set var="surveyOrg"
                           value="${SurveyOrg.findBySurveyConfigAndOrg(surveyResult.surveyConfig, institution)}"/>

                    <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyOrg && surveyOrg.existsMultiYearTerm()}">
                        <td>
                            <g:message code="surveyOrg.perennialTerm.available"/>
                        </td>
                        <td>

                        </td>
                        <td>

                        </td>
                    </g:if>
                    <g:else>
                        <td>
                            <g:if test="${surveyResult.type.isIntegerType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="intValue"/>
                            </g:if>
                            <g:elseif test="${surveyResult.type.isStringType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="stringValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isBigDecimalType()}">
                                <ui:xEditable owner="${surveyResult}" type="text" field="decValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isDateType()}">
                                <ui:xEditable owner="${surveyResult}" type="date" field="dateValue"/>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isURLType()}">
                                <ui:xEditable owner="${surveyResult}" type="url" field="urlValue"
                                                 overwriteEditable="${overwriteEditable}"
                                                 class="la-overflow la-ellipsis"/>
                                <g:if test="${surveyResult.urlValue}">
                                    <ui:linkWithIcon href="${surveyResult.urlValue}"/>
                                </g:if>
                            </g:elseif>
                            <g:elseif test="${surveyResult.type.isRefdataValueType()}">

                                <g:if test="${surveyResult.surveyConfig.subSurveyUseForTransfer && surveyResult.type == PropertyStore.SURVEY_PROPERTY_PARTICIPATION && surveyResult.owner?.id != contextService.getOrg().id}">
                                    <ui:xEditableRefData
                                            data_confirm_tokenMsg="${surveyOrg.orgInsertedItself ? message(code: 'survey.participationProperty.confirmation2') : message(code: 'survey.participationProperty.confirmation')}"
                                            data_confirm_term_how="ok"
                                            class="js-open-confirm-modal-xEditableRefData"
                                            data_confirm_value="${RefdataValue.class.name}:${RDStore.YN_NO.id}"
                                            owner="${surveyResult}"
                                            field="refValue" type="text"
                                            id="participation"
                                            config="${surveyResult.type.refdataCategory}"/>
                                </g:if>
                                <g:else>
                                    <ui:xEditableRefData owner="${surveyResult}" type="text" field="refValue"
                                                            config="${surveyResult.type.refdataCategory}"/>
                                </g:else>
                            </g:elseif>
                        </td>
                        <td>
                            <ui:xEditable owner="${surveyResult}" type="textarea" field="comment"/>
                        </td>
                        <td>
                            <g:if test="${accessService.checkCtxPermAffiliation(CustomerTypeService.ORG_CONSORTIUM_PRO, 'INST_USER')}">
                                <ui:xEditable owner="${surveyResult}" type="textarea" field="ownerComment"/>
                            </g:if>
                            <g:else>
                                <ui:xEditable owner="${surveyResult}" type="textarea" field="participantComment"/>
                            </g:else>
                        </td>
                    </g:else>

                </tr>
            </g:each>
        </table>
    </ui:greySegment>
    <br/>
</g:if>



<laser:script file="${this.getGroovyPageFileName()}">

    $('body #participation').editable('destroy').editable({
        tpl: '<select class="ui dropdown"></select>'
        }).on('shown', function() {
            r2d2.initDynamicUiStuff('body');
            $(".table").trigger('reflow');
            $('.ui.dropdown').dropdown({ clearable: true });
        }).on('hidden', function() {
            $(".table").trigger('reflow')
    });
    <g:if test="${links}">
        <g:each in="${links[genericOIDService.getOID(RDStore.LINKTYPE_LICENSE)]}" var="link">
            $.ajax({
                url: "<g:createLink controller="ajaxHtml" action="getLicensePropertiesForSubscription"/>",
                      data: {
                           loadFor: "${link.sourceLicense.id}",
                           linkId: ${link.id}
            }
        }).done(function(response) {
            $("#${link.id}Properties").html(response);
            }).fail();
        </g:each>
    </g:if>

    <g:if test="${subscription && subscription.packages}">
        JSPC.app.loadPackages = function () {
                  $.ajax({
                      url: "<g:createLink controller="ajaxHtml" action="getGeneralPackageData"/>",
                      data: {
                          subscription: "${subscription.id}"
                      }
                  }).done(function(response){
                      $("#packages").html(response);
                      r2d2.initDynamicUiStuff("#packages");
                  })
              }


        JSPC.app.loadPackages();
    </g:if>

    <g:if test="${subscription && subscription.packages}">
        JSPC.app.loadIEInfos = function () {
                  $.ajax({
                      url: "<g:createLink controller="ajaxHtml" action="getIeInfos"/>",
                      data: {
                          subscription: "${subscription.id}"
                      }
                  }).done(function(response){
                      $("#ieInfos").html(response);
                      r2d2.initDynamicUiStuff("#ieInfos");
                  })
              }


        JSPC.app.loadIEInfos();
    </g:if>

    $('textarea').each(function () {
        this.setAttribute('style', 'height:' + (this.scrollHeight) + 'px;overflow-y:hidden;');
    });

</laser:script>
