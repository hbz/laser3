<%@ page import="de.laser.*;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants;de.laser.FormService;de.laser.utils.LocaleUtils" %>

<laser:htmlStart text="${message(code: 'subscription.details.addMembers.label', args: memberType)}" serviceInjection="true"/>
<%
    String lang = LocaleUtils.getCurrentLang()
    String getAllRefDataValuesForCategoryQuery = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv.value_" + lang
%>
<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="show" id="${subscription.id}" text="${subscription.name}"/>
    <ui:crumb class="active" text="${message(code: 'subscription.details.addMembers.label', args:memberType)}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon floated="true">
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'subscription.details.addMembers.label', args:memberType)}</h2>

<g:if test="${consortialView}">

    <ui:filter simple="true">
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : [['name'], ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup'], ['property&value']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="subscription" method="post" class="ui form addMembers">

        <laser:render template="/templates/filter/orgFilterTable"
                  model="[propList         : propList,
                          orgList          : members,
                          tmplDisableOrgIds: members_disabled,
                          subInstance      : subscription,
                          tmplShowCheckbox : true,
                          tmplConfigShow   : ['sortname', 'name', 'wibid', 'isil', 'region',
                                              'libraryNetwork', 'libraryType']
                  ]"/>

        <g:if test="${members}">
            <br/>
            <div class="ui grid">
                <div class="ui eight wide column">
                    <div class="field">
                        <label for="subStatus"><g:message code="subscription.status.label"/></label>
                        <g:set value="${RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}" var="rdcSubStatus"/>
                        <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown"
                                  optionKey="id"
                                  optionValue="${{ it.getI10n('value') }}"
                                  name="subStatus"
                                  id="subStatus"
                                  value="${Subscription.get(params.id).status?.id.toString()}"/>
                    </div>
                    <div class="field">
                        <label><g:message code="license.label"/></label>
                        <g:if test="${memberLicenses}">
                            <div class="grouped fields">
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics" value="no">
                                        <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                                    </div>
                                </div>
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics" value="all" checked="checked">
                                        <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_all')}</label>
                                    </div>
                                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'myinst.separate_lics_all.expl')}">
                                        <i class="question circle icon la-popup"></i>
                                    </span>
                                </div>
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics" value="partial">
                                        <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_partial')}</label>
                                    </div>
                                    <div class="generateSlavedLicsReference-wrapper hidden">
                                        <g:select from="${memberLicenses}"
                                                  class="ui fluid search multiple dropdown hide"
                                                  optionKey="${{ License.class.name + ':' + it.id }}"
                                                  optionValue="${{ it.reference }}"
                                                  noSelection="${['' : message(code:'default.select.all.label')]}"
                                                  name="generateSlavedLicsReference"/>
                                    </div>
                                </div>
                            </div>
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('*[name=generateSlavedLics]').change(function () {
                                    $('*[name=generateSlavedLics][value=partial]').prop('checked') ? $('.generateSlavedLicsReference-wrapper').removeClass('hidden') : $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                                })
                                $('*[name=generateSlavedLics]').trigger('change')
                            </laser:script>
                        </g:if>
                        <g:else>
                            <g:message code="myinst.noSubscriptionOwner"/>
                        </g:else>
                    </div>
                    <div class="field">
                        <label><g:message code="myinst.addMembers.linkPackages"/></label>
                        <div class="field">
                            <g:if test="${validPackages}">
                                <div class="grouped fields">
                                    <div class="field">
                                        <div class="ui checkbox">
                                            <input type="checkbox" id="linkAllPackages" name="linkAllPackages">
                                            <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages"/></label>
                                        </div>
                                    </div>
                                    <div class="field">
                                        <div class="ui checkbox">
                                            <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements">
                                            <label for="linkWithEntitlements"><g:message code="myinst.addMembers.withEntitlements"/></label>
                                        </div>
                                        <g:select class="ui search multiple dropdown"
                                                  optionKey="id" optionValue="${{ it.getPackageName() }}"
                                                  from="${validPackages}" name="packageSelection" value=""
                                                  noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                                    </div>
                                </div>
                            </g:if>
                            <g:else>
                                <g:message code="subscriptionsManagement.noValidPackages"/>
                            </g:else>
                        </div>
                    </div>
                    <div class="field">
                        <ui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from" value="" />
                        <ui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" value="" />
                    </div>
                    <div class="field">
                        <label><g:message code="myinst.currentSubscriptions.subscription.runTime"/></label>
                        <div class="ui checkbox">
                            <input type="checkbox" id="checkSubRunTimeMultiYear" name="checkSubRunTimeMultiYear">
                            <label for="checkSubRunTimeMultiYear"><g:message code="subscription.isMultiYear.label"/></label>
                        </div>
                    </div>
                </div>
                <div class="ui field eight wide column">
                    <g:if test="${memberProperties}">
                        <label><g:message code="subscription.properties.consortium"/></label>
                        <table class="ui table striped">
                            <g:each in="${memberProperties}" var="prop" status="i">
                                <tr>
                                    <g:hiddenField name="propRow" value="${i}"/>
                                    <g:hiddenField name="propId${i}" value="${prop.id}"/>
                                    <td>${prop.getI10n('name')}</td>
                                    <td>
                                        <g:if test="${prop.isIntegerType()}">
                                            <input type="number" name="propValue${i}" class="memberProperty" placeholder="${message(code:'default.value.label')}"/>
                                        </g:if>
                                        <g:elseif test="${prop.isBigDecimalType()}">
                                            <input type="number" name="propValue${i}" class="memberProperty" step="0.001" placeholder="${message(code:'default.value.label')}"/>
                                        </g:elseif>
                                        <g:elseif test="${prop.isDateType()}">
                                            <input type="date" name="propValue${i}" class="memberProperty" placeholder="${message(code:'default.value.label')}"/>
                                        </g:elseif>
                                        <g:elseif test="${prop.isURLType()}">
                                            <input type="url" name="propValue${i}" class="memberProperty" placeholder="${message(code:'default.value.label')}"/>
                                        </g:elseif>
                                        <g:elseif test="${prop.isRefdataValueType()}">
                                            <ui:select class="ui dropdown search memberPropertyDropdown" name="propValue${i}"
                                                       from="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: prop.refdataCategory])}"
                                                       optionKey="id"
                                                       optionValue="value"
                                                       noSelection="${['':message(code:'default.value.label')]}"/>
                                        </g:elseif>
                                        <g:else>
                                            <g:textField name="propValue${i}" class="memberProperty" placeholder="${message(code:'default.value.label')}" size="10"/>
                                        </g:else>
                                    </td>
                                    <td><g:textArea name="propNote${i}" class="memberProperty" placeholder="${message(code:'property.table.notes')}"/></td>
                                </tr>
                            </g:each>
                        </table>
                    </g:if>
                    <g:if test="${validPackages}">
                        <label for="customerIdentifier">
                            <g:message code="org.customerIdentifier"/>
                        </label>
                        <g:textField name="customerIdentifier"/>
                        <label for="requestorKey">
                            <g:message code="org.requestorKey"/>
                        </label>
                        <g:textField name="requestorKey"/>
                    </g:if>
                </div>
            </div>
        </g:if>

        <br />
        <g:if test="${members}">
            <div class="field la-field-right-aligned">
                <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label')}"/>
            </div>
            <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        </g:if>
    </g:form>

    <g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <hr />

            <ui:msg class="info" header="${message(code: 'myinst.noMembers.cons.header')}" noClose="true">
                <g:message code="myinst.noMembers.body" args="${[createLink(controller:'myInstitution', action:'manageMembers'),message(code:'consortium.member.plural')]}"/>
            </ui:msg>
    </g:if>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $.fn.form.settings.rules.memberAssignal = function() {
        let isUnique = false;
        if($("[name='selectedOrgs']:checked").length > 1) {
            if($(".memberProperty").length > 0) {
                $(".memberProperty").each( function(i) {
                    isUnique = $(this).val().length === 0;
                    if(!isUnique) {
                        return;
                    }
                });
                if(!isUnique)
                    return false;
            }
            if($(".memberPropertyDropdown").length) {
                $(".memberPropertyDropdown").each( function(i) {
                    isUnique = $(this).dropdown('get value').length === 0;
                    if(!isUnique) {
                        return;
                    }
                });
                if(!isUnique)
                    return false;
            }
            return $('#customerIdentifier').val().length === 0 && $('#requestorId').val().length === 0
        }
        else return true;
    }
    $('.addMembers').form({
        on: 'blur',
        inline: true,
        fields: {
            <g:each in="${memberProperties}" var="prop" status="i">
                propValue${i}: {
                    identifier: 'propValue${i}',
                        rules: [
                            {
                            type: 'memberAssignal',
                            prompt: '<g:message code="validation.memberAssignal"/>'
                        }
                    ]
                },
            </g:each>
            customerIdentifier: {
                identifier: 'customerIdentifier',
                    rules: [
                        {
                        type: 'memberAssignal',
                        prompt: '<g:message code="validation.memberAssignal"/>'
                    }
                ]
            },
            requestorKey: {
                identifier: 'requestorKey',
                rules: [
                    {
                        type: 'memberAssignal',
                        prompt: '<g:message code="validation.memberAssignal"/>'
                    }
                ]
            }
        }
    });
</laser:script>

<laser:htmlEnd />
