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

        <div class="ui card">
            <div class="content">

                <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro() && surveyOrg}">
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
                                                 overwriteEditable="${editable && contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/>
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
                                                 overwriteEditable="${editable && contextOrg?.id == surveyConfig.surveyInfo.owner.id && controllerName == 'survey' && actionName == 'show'}"/></dd>

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

                    <ui:card message="surveyconfig.url.plural.label" href="#surveyUrls" editable="${editable}">
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
                            <laser:render template="/survey/surveyUrlsModal"/>
                        </g:if>
                    </ui:card>

                    <br/>

                    <div class="ui la-tab-with-js">
                        <g:form action="setSurveyConfigComment" controller="survey" method="post"
                                params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]">
                            <div class="ui top attached tabular menu">
                                <a class="item ${commentTab != 'commentForNewParticipants' ? 'active' : ''}" data-tab="comment">
                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-content="${message(code: "surveyconfig.comment.comment")}">
                                        ${message(code: 'surveyconfig.comment.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </a>
                                <a class="item ${commentTab == 'commentForNewParticipants' ? 'active' : ''}" data-tab="commentForNewParticipants">
                                    <div class="ui icon la-popup-tooltip la-delay"
                                         data-content="${message(code: "surveyconfig.commentForNewParticipants.comment")}">
                                        ${message(code: 'surveyconfig.commentForNewParticipants.label')}
                                        <i class="question small circular inverted icon"></i>
                                    </div>
                                </a>
                            </div>

                            <div class="ui bottom attached tab segment ${commentTab != 'commentForNewParticipants' ? 'active' : ''}" data-tab="comment">
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
                                            class="ui button" value="comment" name="commentTab">${message(code: 'default.button.save_changes')}</button>
                                </div>
                            </div>

                            <div class="ui bottom attached tab segment ${commentTab == 'commentForNewParticipants' ? 'active' : ''}" data-tab="commentForNewParticipants">
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
                                            class="ui button" value="commentForNewParticipants" name="commentTab">${message(code: 'default.button.save_changes')}</button>
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
                                <ui:xEditable owner="${surveyUrl}" field="url" type="text" overwriteEditable="${false}"/>

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
                                            <i class="ui angle double down icon"></i>
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
                                params="${[q: surveyConfig.subscription.name, consortia: "${surveyInfo.owner.class.name}:${surveyInfo.owner.id}"]}">
                            GASCO-Monitor
                        </g:link>
                        <h2 class="ui icon header">
                            <g:link controller="public" action="gasco"
                                    params="${[q: surveyConfig.subscription.name, consortia: "${surveyInfo.owner.class.name}:${surveyInfo.owner.id}"]}">
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
                                    <i class="ui angle double down icon"></i>
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
            <g:if test="${subscriptionService.areStatsAvailable(subscription)}">
                <div class="ui card">
                    <div class="content">
                        <div id="statsInfos" class="ui accordion la-accordion-showMore js-subscription-info-accordion">
                            <div class="item">
                                <div class="title">
                                    <div class="ui button icon blue la-modern-button la-delay right floated ">
                                        <i class="ui angle double down icon"></i>
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
                <laser:render template="/survey/surveyLinkCard"/>
            </div>

            <g:if test="${controllerName == 'survey' && actionName == 'show'}">
                <div id="container-notes">
                    <laser:render template="/templates/notes/card"
                                  model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '', editable: contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_PRO)]}"/>
                </div>
                <div id="container-tasks">
                    <laser:render template="/templates/tasks/card"
                              model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
                </div>
            </g:if>

            <div id="container-documents">
                <laser:render template="/survey/cardDocuments"
                          model="${[ownobj: surveyConfig, owntp: 'surveyConfig', css_class: '']}"/>
            </div>

        </div>
    </aside>

</div><!-- .grid -->

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION.id]}">
    <g:set var="costItemSurveys"
           value="${surveyOrg ? CostItem.findAllBySurveyOrg(surveyOrg) : null}"/>

    <% Set<RefdataValue> costItemElementsNotInSurveyCostItems = [] %>

    <g:if test="${surveyInfo.owner.id != institution.id && ((costItemSums && costItemSums.subscrCosts) || costItemSurveys)}">

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
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

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

                                    ${costItem.billingCurrency?.getI10n('value')}
                                </td>
                                <td>
                                    <g:if test="${costItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                        ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                    </g:if>
                                    <g:elseif test="${costItem.taxKey}">
                                        ${costItem.taxKey.taxType?.getI10n("value") + " (" + costItem.taxKey.taxRate + "%)"}
                                    </g:elseif>
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItem.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${costItem.billingCurrency?.getI10n('value')}

                                    <g:if test="${costItem.startDate || costItem.endDate}">
                                        <br/>(${formatDate(date: costItem.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItem.endDate, format: message(code: 'default.date.format.notime'))})
                                    </g:if>
                                </td>

                                <g:set var="surveyCostItems" scope="request"
                                       value="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElement(surveyOrg, RDStore.COST_ITEM_DELETED, costItem.costItemElement)}"/>


                                <g:if test="${surveyCostItems && !(costItem.costItemElement in (costItemElementsNotInSurveyCostItems))}">
                                    <g:each in="${surveyCostItems}"
                                            var="costItemSurvey">
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

                                            ${costItemSurvey.billingCurrency?.getI10n('value')}
                                        </td>
                                        <td>
                                            <g:if test="${costItemSurvey.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                                ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                            </g:if>
                                            <g:elseif test="${costItemSurvey.taxKey}">
                                                ${costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)"}
                                            </g:elseif>
                                        </td>
                                        <td>
                                            <strong>
                                                <g:formatNumber
                                                        number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                        minFractionDigits="2" maxFractionDigits="2"
                                                        type="number"/>
                                            </strong>

                                            ${costItemSurvey.billingCurrency?.getI10n('value')}


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
                                        </td>
                                    </g:each>
                                </g:if>
                                <g:else>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                </g:else>
                            </tr>

                            <% costItemElementsNotInSurveyCostItems <<  costItem.costItemElement %>
                        </g:each>
                    </g:if>

                    <g:set var="costItemsWithoutSubCostItems"
                           value="${surveyOrg && costItemElementsNotInSurveyCostItems ? CostItem.findAllBySurveyOrgAndCostItemElementNotInList(surveyOrg, costItemElementsNotInSurveyCostItems) : []}"/>
                    <g:if test="${costItemsWithoutSubCostItems}">
                        <g:each in="${costItemsWithoutSubCostItems}" var="costItemSurvey">
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

                                    ${costItemSurvey.billingCurrency?.getI10n('value')}
                                </td>
                                <td>
                                    <g:if test="${costItemSurvey.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                        ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                    </g:if>
                                    <g:elseif test="${costItemSurvey.taxKey}">
                                        ${costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)"}
                                    </g:elseif>
                                </td>
                                <td>
                                    <strong>
                                        <g:formatNumber
                                                number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                                minFractionDigits="2" maxFractionDigits="2"
                                                type="number"/>
                                    </strong>

                                    ${costItemSurvey.billingCurrency?.getI10n('value')}

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
                        </g:each>
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
                        <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.ownCosts')} </h2>
                        <laser:render template="/subscription/financials" model="[data: costItemSums.ownCosts]"/>
                    </g:if>
                </g:if>
                <g:if test="${costItemSums.consCosts}">
                    <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.consCosts')} ${message(code: 'surveyCostItem.info')}</h2>
                    <laser:render template="/subscription/financials" model="[data: costItemSums.consCosts]"/>
                </g:if>
            </div>
        </div>
    </g:if>
</g:if>

<laser:render template="/templates/survey/properties" model="${[surveyConfig: surveyConfig]}"/>

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
