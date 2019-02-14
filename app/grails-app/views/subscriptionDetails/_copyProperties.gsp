<%@ page import="com.k_int.properties.PropertyDefinition; com.k_int.kbplus.Person; com.k_int.kbplus.Subscription" %>
<%@ page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<semui:form>
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}" params="[workFlowPart: 4]" method="post" class="ui form newLicence">

        <g:hiddenField name="baseSubscription" value="${params.id}" />
        <g:hiddenField name="workFlowPartNext" value="${workFlowPartNext}" />
        <div class="five wide column">
            <label>${message(code: 'subscription.details.copyElementsIntoSubscription.sourceSubscription.name')}: </label>
            <g:select class="ui search dropdown"
                      name="id"
                      from="${allSubscriptions_readRights}"
                      optionValue="name"
                      optionKey="id"
                      value="${subscription?.id}"
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
    <g:form action="copyElementsIntoSubscription" controller="subscriptionDetails" id="${params.id}"
            params="[workFlowPart: 4, targetSubscription: newSub?.id]" method="post" class="ui form newLicence">
        <table class="ui celled table">
            <tbody>
                <tr>
                    <th>${message(code: 'default.select.label')}</th>
                    <td>Quelle:
                    <g:if test="${subscription}"><g:link controller="subscriptionDetails" action="show" id="${subscription?.id}">${subscription?.name}</g:link></g:if>
                    <g:else>(keine Lizenz gewählt)</g:else>
                    </td>
                    <td>Ziel:
                        <g:if test="${newSub}"><g:link controller="subscriptionDetails" action="show" id="${newSub?.id}">${newSub?.name}</g:link></g:if>
                        <g:else>(keine Lizenz gewählt)</g:else>
                    </td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takeCustomProperties" /></th>
                    <td>
                        <g:if test="${subscription}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/custom" model="${[
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: subscription,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${newSub}">
                            ${message(code: 'subscription.takeCustomProperties')}
                            <g:render template="/templates/properties/custom" model="${[
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: newSub,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                </tr>
                <tr>
                    <th><g:checkBox name="subscription.takePrivateProperties" /></th>
                    <td>
                        <g:if test="${subscription}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/private" model="${[
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: subscription,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${newSub}">
                            ${message(code: 'subscription.takePrivateProperties')}
                            <g:render template="/templates/properties/private" model="${[
                                prop_desc: PropertyDefinition.SUB_PROP,
                                ownobj: newSub,
                                custom_props_div: "custom_props_div_${contextOrg.id}",
                                tenant: contextOrg]}"/>
                        </g:if>
                    </td>
                </tr>
            </tbody>
        </table>
        <input type="submit" class="ui button js-click-control" value="Ausgewählte Eigenschaften kopieren/überschreiben" />
    </g:form>
</semui:form>
