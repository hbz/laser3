<%@ page import="de.laser.*;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants;de.laser.FormService" %>

<laser:htmlStart text="${message(code: 'subscription.details.addMembers.label', args: memberType)}" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions"
                 text="${message(code: 'myinst.currentSubscriptions.label')}"/>
    <ui:crumb controller="subscription" action="show" id="${subscription.id}"
                 text="${subscription.name}"/>
    <ui:crumb class="active"
                 text="${message(code: 'subscription.details.addMembers.label',args:memberType)}"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon floated="true">
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'subscription.details.addMembers.label', args:memberType)}</h2>

<g:if test="${consortialView}">

    <ui:filter>
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : [['name'], ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup'], ['property&value']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="subscription" method="post" class="ui form">

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
            <div class="ui two fields">
                <div class="field">
                    <label for="subStatus"><g:message code="myinst.copySubscription"/></label>

                    <g:set value="${RefdataCategory.getByDesc(RDConstants.SUBSCRIPTION_STATUS)}" var="rdcSubStatus"/>

                    <br />
                    <br />

                    <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown"
                              optionKey="id"
                              optionValue="${{ it.getI10n('value') }}"
                              name="subStatus"
                              id="subStatus"
                              value="${Subscription.get(params.id).status?.id.toString()}"/>
                </div>

                <div class="field">
                    <label><g:message code="myinst.copyLicense"/></label>
                    <g:if test="${memberLicenses}">
                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics" value="no">
                            <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                        </div>
                        <br />
                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics" value="all" checked="checked">
                            <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_all')}</label>
                        </div>
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-content="${message(code:'myinst.separate_lics_all.expl')}">
                            <i class="question circle icon la-popup"></i>
                        </span>
                        <br />
                        <div class="ui radio checkbox">
                            <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics" value="partial">
                            <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_partial')}</label>
                        </div>
                        <div class="generateSlavedLicsReference-wrapper hidden">
                            <br />
                            <g:select from="${memberLicenses}"
                                      class="ui fluid search multiple dropdown hide"
                                      optionKey="${{ License.class.name + ':' + it.id }}"
                                      optionValue="${{ it.reference }}"
                                      noSelection="${['' : message(code:'default.select.all.label')]}"
                                      name="generateSlavedLicsReference"/>
                        </div>
                        <laser:script file="${this.getGroovyPageFileName()}">
                            $('*[name=generateSlavedLics]').change(function () {
                                $('*[name=generateSlavedLics][value=partial]').prop('checked') ? $('.generateSlavedLicsReference-wrapper').removeClass('hidden') : $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                            })
                            $('*[name=generateSlavedLics]').trigger('change')
                        </laser:script>
                    </g:if>
                    <g:else>
                        <ui:msg class="info" message="myinst.noSubscriptionOwner"/>
                    </g:else>
                </div>
            </div>
            <div class="two fields">
                <div class="field">
                    <label><g:message code="myinst.addMembers.linkPackages"/></label>
                    <div class="field">
                        <g:if test="${validPackages}">
                            <div class="ui checkbox">
                                <input type="checkbox" id="linkAllPackages" name="linkAllPackages">
                                <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages" args="${superOrgType}"/></label>
                            </div>
                            <div class="ui checkbox">
                                <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements">
                                <label for="linkWithEntitlements"><g:message code="myinst.addMembers.withEntitlements"/></label>
                            </div>
                            <g:select class="ui search multiple dropdown"
                                      optionKey="id" optionValue="${{ it.getPackageName() }}"
                                      from="${validPackages}" name="packageSelection" value=""
                                      noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                        </g:if>
                        <g:else>
                            <g:message code="subscriptionsManagement.noValidPackages" args="${superOrgType}"/>
                        </g:else>
                    </div>
                </div>
                <div class="field">
                    <ui:datepicker label="subscription.startDate.label" id="valid_from" name="valid_from" value="" />

                    <ui:datepicker label="subscription.endDate.label" id="valid_to" name="valid_to" value="" />
                </div>
            </div>
            <div class="two fields">
                <div class="field">
                    <label><g:message code="myinst.currentSubscriptions.subscription.runTime"/></label>
                    <div class="ui checkbox">
                            <input type="checkbox" id="checkSubRunTimeMultiYear" name="checkSubRunTimeMultiYear">
                            <label for="checkSubRunTimeMultiYear"><g:message code="subscription.isMultiYear.label"/></label>
                    </div>
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

    <g:if test="${accessService.checkPermAffiliation("ORG_CONSORTIUM","INST_EDITOR")}">
        <hr />

            <ui:msg class="info" header="${message(code: 'myinst.noMembers.cons.header')}" noClose="true">
                <g:message code="myinst.noMembers.body" args="${[createLink(controller:'myInstitution', action:'manageMembers'),message(code:'consortium.member.plural')]}"/>
            </ui:msg>
    </g:if>
</g:if>

<laser:htmlEnd />
