<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'subscription.details.renewalsConsortium.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>


<h1 class="ui header"><semui:headerIcon/>
${message(code: 'subscription.details.renewalsConsortium.label')}: ${subscription?.name}
</h1>

<semui:messages data="${flash}"/>

<h3>${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps', default: 'Steps: {0} of {1}', args: [workFlowPart, 3])}</h3>

<semui:form>
    <g:form action="renewSubscriptionConsortia" controller="subscriptionDetails" id="${params.id}"
            params="[workFlowPart: workFlowPart]" method="post" class="ui form newLicence">
        <g:hiddenField name="baseSubscription" value="${params.id}"/>
        <g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}"/>

        <g:if test="${workFlowPart >= 2}">
            <g:hiddenField name="newSubscription" value="${newSub?.id}"/>
        </g:if>
        <div class="field ">
            <label>${message(code: 'myinst.emptySubscription.name', default: 'New Subscription Name')}: ${subscription?.name}</label>
        %{--<input required type="text" name="sub_name" value="${subscription.name}" placeholder=""/>--}%

            <g:if test="${workFlowPart == 2}">
                <br><b>${message(code: 'subscription.renewSubscriptionConsortia.success', default: 'The license has been renewed. You can now apply more license properties to the extended license from the old license.')}</b><br>
            </g:if>
            <g:if test="${workFlowPart == 3}">
                <br><b>${message(code: 'subscription.renewSubscriptionConsortia.addMembers', default: 'You can now select the subscriber from the old license to be included in the extended license.')}</b><br>
            </g:if>
            <g:if test="${workFlowPart >= 2}">
                <br><g:link controller="subscriptionDetails" action="show" target="_blank"
                            id="${newSub?.id}">${message(code: 'myinst.emptySubscription.label')}: ${newSub?.name}</g:link>
            </g:if>
        </div>


        <hr>
        <table class="ui celled table">
            <tbody>
            <g:if test="${workFlowPart == 1}">
                <tr>
                    <th>${message(code: 'default.select.label', default: 'Select')}</th>
                    <th>${message(code: 'subscription.property', default: 'Subscription Properties')}</th>
                    <th>${message(code: 'default.value.label', default: 'Value')}</th>
                </tr>

                <tr>
                    <th><g:checkBox name="subscription.takeDates" value="${true}" disabled="${true}"/></th>
                    <th>${message(code: 'subscription.takeDates', default: 'Take all Dates from Subscription')}</th>
                    <td><g:formatDate date="${newStartDate}"
                                      format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"/>${newEndDate ? (' - ' + formatDate(date: newEndDate, format: message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))) : ''}</td>
                </tr>

                <tr>
                    <th><g:checkBox name="subscription.takeCustomProperties" value="${true}"/></th>
                    <th>${message(code: 'subscription.takeCustomProperties', default: 'Take Property from Subscription')}</th>
                    <td>${message(code: 'subscription.properties')}<br>
                    </td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takePrivateProperties" value="${true}"/></th>
                    <th>${message(code: 'subscription.takePrivateProperties', default: 'Take Property from Subscription')}</th>
                    <td>${message(code: 'subscription.properties.private')} ${contextOrg?.name}<br>
                    </td>
                </tr>

                <tr>
                    <th><g:checkBox name="subscription.takeLinks" value="${true}"/></th>
                    <th>${message(code: 'subscription.takeLinks', default: 'Take Links from Subscription')}</th>
                    <td>

                        <g:each in="${subscription.packages.sort { it.pkg.name }}" var="sp">
                            <b>${message(code: 'subscription.packages.label')}:</b>
                            <g:link controller="packageDetails" action="show" target="_blank"
                                    id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>

                            <g:if test="${sp.pkg?.contentProvider}">
                                (${sp.pkg?.contentProvider?.name})
                            </g:if><br>
                        </g:each>
                        <br>
                        <g:if test="${subscriptionInstance.owner}">
                            <b>${message(code: 'license')}:</b>
                            <g:link controller="licenseDetails" action="show" target="_blank"
                                    id="${subscriptionInstance.owner.id}">
                                ${subscriptionInstance.owner}
                            </g:link><br><br>
                        </g:if>

                        <g:each in="${visibleOrgRelations.sort { it.roleType?.getI10n("value") }}" var="role">
                            <g:if test="${role.org}">
                                <b>${role?.roleType?.getI10n("value")}:</b> <g:link controller="Organisations"
                                                                                    action="show" target="_blank"
                                                                                    id="${role.org.id}">${role?.org?.name}</g:link><br>
                            </g:if>
                        </g:each>
                    </td>
                </tr>

                <tr>
                    <th><g:checkBox name="subscription.takeEntitlements" value="${true}"/></th>
                    <th>${message(code: 'subscription.takeEntitlements', default: 'Take Current Entitlements from Subscription')}</th>
                    <td><b>${message(code: 'issueEntitlement.countSubscription')} ${subscription.issueEntitlements.findAll {
                        it.status != com.k_int.kbplus.RefdataCategory.lookupOrCreate('Entitlement Issue Status', 'Deleted')
                    }.size()}</b>

                        %{--                        <g:each in="${subscription.issueEntitlements.sort{it.tipp.title}}" var="ie">
                                                    <g:if test="${ie.status != com.k_int.kbplus.RefdataCategory.lookupOrCreate('Entitlement Issue Status', 'Deleted')}">
                                                    ${ie.tipp.title.title}
                                                    </g:if>
                                                </g:each>--}%
                    </td>
                </tr>

            </g:if>


            <g:if test="${workFlowPart == 2}">
                <tr>
                    <th>${message(code: 'default.select.label', default: 'Select')}</th>
                    <th>${message(code: 'subscription.property', default: 'Subscription Properties')}</th>
                    <th>${message(code: 'default.value.label', default: 'Value')}</th>
                </tr>

                <g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">
                        <tr>
                            <th><g:checkBox name="subscription.takeDocs" value="${docctx.id}" checked="${true}"/></th>
                            <th>${message(code: 'subscription.takeDocs', default: 'Take Documents from Subscription')}</th>
                            <td>
                                <g:link controller="docstore" id="${docctx.owner.uuid}">
                                    <g:if test="${docctx.owner?.title}">
                                        ${docctx.owner.title}
                                    </g:if>
                                    <g:else>
                                        <g:if test="${docctx.owner?.filename}">
                                            ${docctx.owner.filename}
                                        </g:if>
                                        <g:else>
                                            ${message(code: 'template.documents.missing', default: 'Missing title and filename')}
                                        </g:else>
                                    </g:else>

                                </g:link>(${docctx.owner.type.getI10n("value")})
                            </td>
                        </tr>
                    </g:if>
                </g:each>
                <tr></tr><tr></tr>
                <g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">
                    <g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">
                        <tr>
                            <th><g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}"
                                            checked="${true}"/></th>
                            <th>${message(code: 'subscription.takeAnnouncements', default: 'Take Notes from Subscription')}</th>
                            <td>
                                <g:if test="${docctx.owner.title}">
                                    <b>${docctx.owner.title}</b>
                                </g:if>
                                <g:else>
                                    <b>Ohne Titel</b>
                                </g:else>

                                (${message(code: 'template.notes.created')}
                                <g:formatDate
                                        format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"
                                        date="${docctx.owner.dateCreated}"/>)

                            </td></tr>
                    </g:if>
                </g:each>
                <tr></tr><tr></tr>
                <g:each in="${tasks}" var="tsk">
                    <tr>
                        <th><g:checkBox name="subscription.takeTasks" value="${tsk.id}" checked="${true}"/></th>
                        <th>${message(code: 'subscription.takeTasks', default: 'Take Tasks from Subscription')}</th>

                        <td>
                            <b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}
                        <g:formatDate format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"
                                      date="${tsk.endDate}"/>)

                        </td></tr>
                </g:each>
            </g:if>

            <g:if test="${workFlowPart == 3}">
                <g:if test="${validSubChilds}">

                    <g:each in="${[validSubChilds]}" status="i" var="outerLoop">

                        <table class="ui celled la-table table">
                            <thead>
                            <tr>
                                <th>
                                    <g:if test="${outerLoop}">
                                        <g:checkBox name="subListToggler" id="subListToggler" checked="false"/>
                                    </g:if>
                                </th>
                                <th>
                                    ${message(code: 'sidewide.number')}
                                </th>
                                <th>Sortiername</th>
                                <th>
                                    ${message(code: 'subscriptionDetails.members.members')}
                                </th>

                                <th>${message(code: 'default.startDate.label')}</th>
                                <th>${message(code: 'default.endDate.label')}</th>
                                <th>${message(code: 'subscription.details.status')}</th>
                            </tr>
                            </thead>
                            <tbody>
                            <g:each in="${outerLoop}" status="j" var="sub">
                                <tr>
                                    <g:each in="${sub.getAllSubscribers()}" var="subscr">

                                        <td>
                                            <g:checkBox type="text" name="selectedSubs" value="${sub.id}"
                                                        checked="false"/>
                                        </td>
                                        <td>${j + 1}</td>

                                        <td>${subscr.sortname}</td>
                                        <td>
                                            <g:link controller="organisations" action="show"
                                                    id="${subscr.id}">${subscr}</g:link>

                                            <g:set var="rdvGcp"
                                                   value="${RefdataValue.findByValue('General contact person')}"/>
                                            <g:set var="rdvSse"
                                                   value="${RefdataValue.findByValue('Specific subscription editor')}"/>

                                            <div class="ui list">

                                                <g:each in="${Person.getPublicByOrgAndFunc(subscr, 'General contact person')}"
                                                        var="gcp">
                                                    <div class="item">
                                                        <g:link controller="person" action="show"
                                                                id="${gcp.id}">${gcp}</g:link>
                                                        (${rdvGcp.getI10n('value')})
                                                    </div>
                                                </g:each>
                                                <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(subscr, 'General contact person', contextService.getOrg())}"
                                                        var="gcp">
                                                    <div class="item">
                                                        <g:link controller="person" action="show"
                                                                id="${gcp.id}">${gcp}</g:link>
                                                        (${rdvGcp.getI10n('value')} <i class="address book outline icon"
                                                                                       style="display:inline-block"></i>)
                                                    </div>
                                                </g:each>
                                                <g:each in="${Person.getPublicByOrgAndObjectResp(subscr, sub, 'Specific subscription editor')}"
                                                        var="sse">
                                                    <div class="item">
                                                        <g:link controller="person" action="show"
                                                                id="${sse.id}">${sse}</g:link>
                                                        (${rdvSse.getI10n('value')})
                                                    </div>
                                                </g:each>
                                                <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(subscr, sub, 'Specific subscription editor', contextService.getOrg())}"
                                                        var="sse">
                                                    <div class="item">
                                                        <g:link controller="person" action="show"
                                                                id="${sse.id}">${sse}</g:link>
                                                        (${rdvSse.getI10n('value')} <i class="address book outline icon"
                                                                                       style="display:inline-block"></i>)
                                                    </div>
                                                </g:each>

                                            </div>
                                        </td>

                                    </g:each>
                                    <g:if test="${!sub.getAllSubscribers()}">
                                        <td></td>
                                        <td></td>
                                    </g:if>

                                    <td>
                                        <g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/>
                                    </td>
                                    <td>
                                        <g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/>
                                    </td>
                                    <td>
                                        ${sub.status.getI10n('value')}
                                    </td>
                                </tr>
                            </g:each>
                            </tbody>
                        </table>
                    </g:each>

                    <script language="JavaScript">
                        $('#subListToggler').click(function () {
                            if ($(this).prop('checked')) {
                                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
                            }
                            else {
                                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
                            }
                        })
                    </script>
                </g:if>
                <g:else>
                    <br><strong><g:message code="subscription.renewSubscriptionConsortia.noMembers"
                                           default="The old license has no subscribers."/></strong>
                </g:else>
            </g:if>

            </tbody>
        </table>
        <g:if test="${workFlowPart >= 1 && workFlowPart <= 2}">
            <input type="submit" class="ui button js-click-control"
                   value="${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.nextStep', default: 'Select and go to the next step')}"/>
        </g:if>
        <g:if test="${workFlowPart == 3}">
        <input type="submit" class="ui button js-click-control"
               value="${message(code: 'subscription.renewSubscriptionConsortia.finish', default: 'Finish')}"/>
        </g:if>
    </g:form>
</semui:form>
</body>
</html>