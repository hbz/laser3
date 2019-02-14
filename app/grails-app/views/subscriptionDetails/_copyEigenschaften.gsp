<%@ page import="com.k_int.properties.PropertyDefinition; de.laser.helper.RDStore; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<%workFlowPart = 1%>
<%newSub = newSub ?: Subscription.get(400)%>
<semui:form>
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails"
            params="[workFlowPart: workFlowPart]" method="post" class="ui form newLicence">

        <g:hiddenField name="baseSubscription" value="${params.id}" />
        <g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}" />
        <div class="five wide column">
            <label>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}: </label>
            <g:select class="ui search dropdown"
                      name="id"
                      from="${allSubscriptions_readRights}"
                      optionValue="name"
                      optionKey="id"
                      />
                      %{--disabled="${(subscription)? true : false}"/>--}%
            <label>${message(code: 'subscription.details.copyElementsIntoSubscription.targetSubscription.name')}: </label>
            <g:select class="ui search dropdown"
                      name="targetSubscription"
                      from="${allSubscriptions_writeRights}"
                      optionValue="name"
                      optionKey="id"
                      value="${newSub?.id}"
                      noSelection="${[null: message(code: 'default.select.choose.label')]}"/>
            <input type="submit" class="ui button" value="Lizenz(en) auswählen" />
        </div>
    </g:form>
    <hr>
%{--</semui:form>--}%
%{--<semui:form>--}%
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"
            params="[workFlowPart: workFlowPart, targetSubscription: newSub?.id]" method="post" class="ui form newLicence">
            %{--<g:if test="${workFlowPart == 2}">--}%
                %{--<br><b>${message(code: 'subscription.renewSubscriptionConsortia.success', default: 'The license has been renewed. You can now apply more license properties to the extended license from the old license.')}</b><br>--}%
            %{--</g:if>--}%
        %{--</div>--}%
        <table class="ui celled table">
            <tbody>
%{--------------------------------------------------------------------------------------------------------------------}%
            <g:if test="${workFlowPart == 1}">
                <tr>
                    <th>${message(code: 'default.select.label')}</th>
                    <th>${message(code: 'subscription.property')}</th>
                    %{--<th>${message(code: 'default.value.label')}</th>--}%
                    <th>Quelle: ${subscription?.name}</th>
                    <th><i class="ui icon angle double right"></i></th>
                    <th>Ziel: ${newSub?.name?: "(keine Lizenz gewählt)"}</th>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takeDates" /></th>
                    <td>${message(code: 'subscription.takeDates')}</td>
                    <td><g:formatDate date="${subscription.startDate}"
                                      format="${message(code: 'default.date.format.notime')}"/>
                        ${subscription?.endDate ? (' - ' + formatDate(date: subscription.endDate, format: message(code: 'default.date.format.notime'))) : ''}</td>
                    <td><i class="ui icon angle double right"></i></td>
                    <td><g:formatDate date="${newSub?.startDate}"
                                      format="${message(code: 'default.date.format.notime')}"/>
                        ${newSub?.endDate ? (' - ' + formatDate(date: newSub?.endDate, format: message(code: 'default.date.format.notime'))) : ''}</td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takeCustomProperties" /></th>
                    <td>${message(code: 'subscription.takeCustomProperties')}</td>
                    <td>
                    <g:render template="/templates/properties/custom" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: subscription,
                            custom_props_div: "custom_props_div_${contextOrg.id}",
                            tenant: contextOrg]}"/>
                    </td>
                    <td><i class="ui icon angle double right"></i></td>
                    <td>
                    <g:render template="/templates/properties/custom" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: newSub,
                            custom_props_div: "custom_props_div_${contextOrg.id}",
                            tenant: contextOrg]}"/>
                    </td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takePrivateProperties" /></th>
                    <td>${message(code: 'subscription.takePrivateProperties')}</td>
                    <td>
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: subscription,
                            custom_props_div: "custom_props_div_${contextOrg.id}",
                            tenant: contextOrg]}"/>
                    </td>
                    <td><i class="ui icon angle double right"></i></td>
                    <td>
                    <g:render template="/templates/properties/private" model="${[
                            prop_desc: PropertyDefinition.SUB_PROP,
                            ownobj: newSub,
                            custom_props_div: "custom_props_div_${contextOrg.id}",
                            tenant: contextOrg]}"/>
                    </td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takeLinks" /></th>
                    <td>${message(code: 'subscription.takeLinks')}</td>
                    <td>
                        <g:each in="${subscription.packages.sort { it.pkg.name }}" var="sp">
                            <b>${message(code: 'subscription.packages.label')}:</b>
                            <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>
                            <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                            <br>
                        </g:each>
                        <br>
                        <g:if test="${subscriptionInstance.owner}">
                            <b>${message(code: 'license')}:</b>
                            <g:link controller="licenseDetails" action="show" target="_blank" id="${subscriptionInstance.owner.id}">
                                ${subscriptionInstance.owner}
                            </g:link>
                            <br><br>
                        </g:if>
                        <g:each in="${visibleOrgRelations.sort { it.roleType?.getI10n("value") }}" var="role">
                            <g:if test="${role.org}">
                                <b>${role?.roleType?.getI10n("value")}:</b>
                                <g:link controller="Organisations" action="show" target="_blank" id="${role.org.id}">
                                    ${role?.org?.name}
                                </g:link><br>
                            </g:if>
                        </g:each>
                    </td>
                    <td><i class="ui icon angle double right"></i></td>
                    <td>
                        ÜBERPRÜFEN:<br>
                        <g:each in="${newSub?.packages?.sort { it.pkg.name }}" var="sp">
                            <b>${message(code: 'subscription.packages.label')}:</b>
                            <g:link controller="packageDetails" action="show" target="_blank" id="${sp.pkg.id}">${sp?.pkg?.name}</g:link>
                            <g:if test="${sp.pkg?.contentProvider}">(${sp.pkg?.contentProvider?.name})</g:if>
                            <br>
                        </g:each>
                        <br>
                        <g:if test="${newSub?.owner}">
                            <b>${message(code: 'license')}:</b>
                            <g:link controller="licenseDetails" action="show" target="_blank" id="${newSub?.owner?.id}">
                                ${newSub?.owner}
                            </g:link>
                            <br><br>
                        </g:if>
<%
    def target_visibleOrgRelations = []
    newSub?.orgRelations?.each { or ->
        if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
            target_visibleOrgRelations << or
        }
    }
%>
                        <g:each in="${target_visibleOrgRelations.sort { it.roleType?.getI10n("value") }}" var="role">
                            <g:if test="${role.org}">
                                <b>${role?.roleType?.getI10n("value")}:</b>
                                <g:link controller="Organisations" action="show" target="_blank" id="${role.org.id}">
                                    ${role?.org?.name}
                                </g:link><br>
                            </g:if>
                        </g:each>
                    </td>
                </tr>

                <tr>
                    <th><g:checkBox name="subscription.takeEntitlements" /></th>
                    <td>${message(code: 'subscription.takeEntitlements')}</td>
                    <% def sourceIECount = subscription.issueEntitlements?.findAll { it.status != RDStore.IE_DELETED }?.size() %>
                    <td><g:if test="${sourceIECount}"><b>${message(code: 'issueEntitlement.countSubscription')} </b>
                        ${sourceIECount}</g:if>
                    </td>
                    <td><i class="ui icon angle double right"></i></td>
                    <% def targetIECount = newSub?.issueEntitlements?.findAll { it.status != RDStore.IE_DELETED }?.size() %>
                    <td><g:if test="${targetIECount}"> <b>${message(code: 'issueEntitlement.countSubscription')}: </b>
                        ${targetIECount}</g:if>
                    </td>
                </tr>
            </g:if>
%{--------------------------------------------------------------------------------------------------------------------}%
            %{--<g:if test="${workFlowPart == 2}">--}%
                %{--<tr>--}%
                    %{--<th>${message(code: 'default.select.label', default: 'Select')}</th>--}%
                    %{--<th>${message(code: 'subscription.property', default: 'Subscription Properties')}</th>--}%
                    %{--<th>${message(code: 'default.value.label', default: 'Value')}</th>--}%
                %{--</tr>--}%

                %{--<g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">--}%
                    %{--<g:if test="${(((docctx.owner?.contentType == 1) || (docctx.owner?.contentType == 3)) && (docctx.status?.value != 'Deleted'))}">--}%
                        %{--<tr>--}%
                            %{--<th><g:checkBox name="subscription.takeDocs" value="${docctx.id}" checked="${true}"/></th>--}%
                            %{--<th>${message(code: 'subscription.takeDocs', default: 'Take Documents from Subscription')}</th>--}%
                            %{--<td>--}%
                                %{--<g:link controller="docstore" id="${docctx.owner.uuid}">--}%
                                    %{--<g:if test="${docctx.owner?.title}">--}%
                                        %{--${docctx.owner.title}--}%
                                    %{--</g:if>--}%
                                    %{--<g:else>--}%
                                        %{--<g:if test="${docctx.owner?.filename}">--}%
                                            %{--${docctx.owner.filename}--}%
                                        %{--</g:if>--}%
                                        %{--<g:else>--}%
                                            %{--${message(code: 'template.documents.missing', default: 'Missing title and filename')}--}%
                                        %{--</g:else>--}%
                                    %{--</g:else>--}%

                                %{--</g:link>(${docctx.owner.type.getI10n("value")})--}%
                            %{--</td>--}%
                        %{--</tr>--}%
                    %{--</g:if>--}%
                %{--</g:each>--}%
                %{--<tr></tr><tr></tr>--}%
                %{--<g:each in="${subscription.documents.sort { it.owner?.title }}" var="docctx">--}%
                    %{--<g:if test="${((docctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(docctx.domain) && (docctx.status?.value != 'Deleted'))}">--}%
                        %{--<tr>--}%
                            %{--<th><g:checkBox name="subscription.takeAnnouncements" value="${docctx.id}"--}%
                                            %{--checked="${true}"/></th>--}%
                            %{--<th>${message(code: 'subscription.takeAnnouncements', default: 'Take Notes from Subscription')}</th>--}%
                            %{--<td>--}%
                                %{--<g:if test="${docctx.owner.title}">--}%
                                    %{--<b>${docctx.owner.title}</b>--}%
                                %{--</g:if>--}%
                                %{--<g:else>--}%
                                    %{--<b>Ohne Titel</b>--}%
                                %{--</g:else>--}%

                                %{--(${message(code: 'template.notes.created')}--}%
                                %{--<g:formatDate--}%
                                        %{--format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"--}%
                                        %{--date="${docctx.owner.dateCreated}"/>)--}%

                            %{--</td></tr>--}%
                    %{--</g:if>--}%
                %{--</g:each>--}%
                %{--<tr></tr><tr></tr>--}%
                %{--<g:each in="${tasks}" var="tsk">--}%
                    %{--<tr>--}%
                        %{--<th><g:checkBox name="subscription.takeTasks" value="${tsk.id}" checked="${true}"/></th>--}%
                        %{--<th>${message(code: 'subscription.takeTasks', default: 'Take Tasks from Subscription')}</th>--}%

                        %{--<td>--}%
                            %{--<b>${tsk?.title}</b> (${message(code: 'task.endDate.label')}--}%
                        %{--<g:formatDate format="${message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')}"--}%
                                      %{--date="${tsk.endDate}"/>)--}%

                        %{--</td></tr>--}%
                %{--</g:each>--}%
            %{--</g:if>--}%
%{--------------------------------------------------------------------------------------------------------------------}%
            </tbody>
        </table>
        <input type="submit" class="ui button js-click-control"
               value="Ausgewählte Eigenschaften kopieren/überschreiben" />
    </g:form>
</semui:form>
