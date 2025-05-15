<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.*;de.laser.interfaces.CalculatedType;de.laser.storage.RDConstants;de.laser.FormService;de.laser.utils.LocaleUtils" %>

<laser:htmlStart text="${message(code: 'subscription.details.addMembers.label', args: memberType)}" />
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

<ui:h1HeaderWithIcon referenceYear="${subscription.referenceYear}" visibleProviders="${providerRoles}">
    <ui:xEditable owner="${subscription}" field="name"/>
</ui:h1HeaderWithIcon>

<ui:anualRings object="${subscription}" controller="subscription" action="addMembers" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
<br>

<h2 class="ui left floated aligned icon header la-clear-before">${message(code: 'subscription.details.addMembers.label', args:memberType)}</h2>

<g:if test="${consortialView}">

    <ui:filter simple="true">
        <g:form action="addMembers" method="get" params="[id: params.id]" class="ui form">
            <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : [['name'], ['country&region', 'libraryNetwork', 'libraryType', 'subjectGroup'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['property&value']],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </ui:filter>

    <g:form action="processAddMembers" params="${[id: params.id]}" controller="subscription" method="post" enctype="multipart/form-data" class="ui form addMembers">


        <div class="ui divider"></div>

        <div class="ui message la-clear-before">
            <div class="header">
                ${message(code: 'subscription.details.addMembers.option.selectMembersWithFile.info')}
            </div>
            ${message(code: 'subscription.details.addMembers.option.selectMembersWithFile.text')}

            <br>
            <g:link class="item" controller="public" action="manual" id="fileImport" target="_blank">${message(code: 'help.technicalHelp.fileImport.short')}</g:link>
            <br>

            <g:link controller="subscription" action="templateForMembersBulkWithUpload" params="[id: params.id]">
                <p>${message(code:'myinst.financeImport.template')}</p>
            </g:link>

            <div class="ui action input">
                <input type="text" readonly="readonly"
                       placeholder="${message(code: 'template.addDocument.selectFile')}">
                <input type="file" name="selectSubMembersWithImport" accept=".txt,.csv,.tsv,text/tab-separated-values,text/csv,text/plain"
                       style="display: none;">
                <div class="${Btn.ICON.SIMPLE}">
                    <i class="${Icon.CMD.ATTACHMENT}"></i>
                </div>
            </div>
            <g:if test="${members}">
                <div class="field la-field-right-aligned">
                    <input type="submit" class="ui button js-click-control" value="${message(code: 'default.button.create.label')}"/>
                </div>
            </g:if>
        </div>


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
                        <g:select from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS)}" class="ui dropdown clearable"
                                  optionKey="id"
                                  optionValue="${{ it.getI10n('value') }}"
                                  name="subStatus"
                                  id="subStatus"
                                  value="${Subscription.get(params.id).status?.id}"/>
                    </div>
                    <div class="field">
                        <label><g:message code="license.label"/></label>
                        <g:if test="${memberLicenses}">
                            <div class="grouped fields">
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics_${subscription.id}" value="no">
                                        <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                                    </div>
                                </div>
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics_${subscription.id}" value="all" checked="checked">
                                        <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_all')}</label>
                                    </div>
                                    <span class="la-long-tooltip la-popup-tooltip" data-content="${message(code:'myinst.separate_lics_all.expl')}">
                                        <i class="${Icon.TOOLTIP.HELP} la-popup"></i>
                                    </span>
                                </div>
                                <div class="field">
                                    <div class="ui radio checkbox">
                                        <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics_${subscription.id}" value="partial">
                                        <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_partial')}</label>
                                    </div>
                                    <div class="generateSlavedLicsReference-wrapper hidden">
                                        <g:select from="${memberLicenses}"
                                                  class="ui fluid search multiple dropdown hide"
                                                  optionKey="${{ License.class.name + ':' + it.id }}"
                                                  optionValue="${{ it.reference }}"
                                                  noSelection="${['' : message(code:'default.select.all.label')]}"
                                                  name="generateSlavedLicsReference_${subscription.id}"/>
                                    </div>
                                </div>
                            </div>
                            <laser:script file="${this.getGroovyPageFileName()}">
                                $('*[name=generateSlavedLics_${subscription.id}]').change(function () {
                                    $('*[name=generateSlavedLics_${subscription.id}][value=partial]').prop('checked') ? $('.generateSlavedLicsReference-wrapper').removeClass('hidden') : $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                                })
                                $('*[name=generateSlavedLics_${subscription.id}]').trigger('change')
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
                                    <g:if test="${auditService.getAuditConfig(subscription, 'holdingSelection')}">
                                        <g:hiddenField name="linkAllPackages_${subscription.id}" value="on"/>
                                        <g:message code="myinst.addMembers.packagesAutomaticallyLinked"/>
                                    </g:if>
                                    <g:else>
                                        <div class="field">
                                            <div class="ui checkbox">
                                                <input type="checkbox" id="linkAllPackages" name="linkAllPackages_${subscription.id}">
                                                <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages"/></label>
                                            </div>
                                        </div>
                                        <div class="field">
                                            <g:select class="ui search multiple dropdown"
                                                      optionKey="id" optionValue="${{ it.getPackageName() }}"
                                                      from="${validPackages}" name="packageSelection_${subscription.id}" value=""
                                                      noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                                        </div>
                                        <g:if test="${!auditService.getAuditConfig(subscription, 'holdingSelection')}">
                                            <div class="field">
                                                <div class="ui checkbox">
                                                    <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements_${subscription.id}">
                                                    <label for="linkWithEntitlements"><g:message code="myinst.addMembers.withEntitlements"/></label>
                                                </div>
                                            </div>
                                        </g:if>
                                    </g:else>
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
                        <ui:datepicker type="year" label="subscription.referenceYear.label" id="reference_year" name="reference_year" value="" />
                    </div>
                    <div class="field">
                        <label><g:message code="myinst.currentSubscriptions.subscription.runTime"/></label>
                        <div class="ui checkBoxSubRunTimeMultiYear checkbox">
                            <input type="checkbox" id="checkSubRunTimeMultiYear" name="checkSubRunTimeMultiYear">
                            <label for="checkSubRunTimeMultiYear"><g:message code="subscription.isMultiYear.label"/></label>
                        </div>
                    </div>


            <g:set var="nextSubs" value="${linksGenerationService.getSuccessionChain(subscription, 'destinationSubscription')}"/>

            <g:if test="${nextSubs}">
                <ui:greySegment>
                    <div class="grouped fields">
                        <label><g:message code="subscription.details.addMembers.option.addToSubWithMultiYear"/>:</label>

                        <div class="ui ordered list">

                            <g:each var="nextSub" in="${nextSubs}">
                                <g:set var="validPackagesNextSub" value="${nextSub.packages?.sort { it.pkg.name }}"/>
                                <g:set var="memberLicensesNextSub"
                                       value="${License.executeQuery("select l from License l where l.instanceOf in (select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType)", [subscription: nextSub, linkType: RDStore.LINKTYPE_LICENSE])}"/>

                                <div class="item">
                                    <div class="item field">
                                        <div class="ui checkboxAddToSubWithMultiYear addToSubWithMultiYear_${nextSub.id} checkbox">
                                            <input type="checkbox" name="addToSubWithMultiYear_${nextSub.id}">
                                            <label>${nextSub.getLabel()}</label>
                                        </div>

                                        <div class="ui list">
                                            <div class="item">
                                                <div class="field">
                                                    <label><g:message code="license.label"/></label>
                                                    <g:if test="${memberLicensesNextSub}">
                                                        <div class="grouped fields">
                                                            <div class="field">
                                                                <div class="ui checkboxLicAndPkg_${nextSub.id} radio checkbox">
                                                                    <input class="hidden" type="radio" id="generateSlavedLics_${nextSub.id}"
                                                                           name="generateSlavedLics_${nextSub.id}" value="no">
                                                                    <label for="generateSlavedLics_${nextSub.id}">${message(code: 'myinst.separate_lics_no')}</label>
                                                                </div>
                                                            </div>

                                                            <div class="field">
                                                                <div class="ui checkboxGenerateSlavedLics1_${nextSub.id} radio checkbox">
                                                                    <input class="hidden" type="radio" id="generateSlavedLics1_${nextSub.id}"
                                                                           name="generateSlavedLics_${nextSub.id}" value="all" checked="checked">
                                                                    <label for="generateSlavedLics1_${nextSub.id}">${message(code: 'myinst.separate_lics_all')}</label>
                                                                </div>
                                                                <span class="la-long-tooltip la-popup-tooltip"
                                                                      data-content="${message(code: 'myinst.separate_lics_all.expl')}">
                                                                    <i class="${Icon.TOOLTIP.HELP} la-popup"></i>
                                                                </span>
                                                            </div>

                                                            <div class="field">
                                                                <div class="ui checkboxLicAndPkg_${nextSub.id} radio checkbox">
                                                                    <input class="hidden" type="radio" id="generateSlavedLics2_${nextSub.id}"
                                                                           name="generateSlavedLics_${nextSub.id}" value="partial">
                                                                    <label for="generateSlavedLics2_${nextSub.id}">${message(code: 'myinst.separate_lics_partial')}</label>
                                                                </div>

                                                                <div class="generateSlavedLicsReference-wrapper_${nextSub.id} hidden">
                                                                    <g:select from="${memberLicensesNextSub}"
                                                                              class="ui fluid search multiple dropdown hide"
                                                                              optionKey="${{ License.class.name + ':' + it.id }}"
                                                                              optionValue="${{ it.reference }}"
                                                                              noSelection="${['': message(code: 'default.select.all.label')]}"
                                                                              name="generateSlavedLicsReference_${nextSub.id}"/>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <laser:script file="${this.getGroovyPageFileName()}">
                                                            $('*[name=generateSlavedLics_${nextSub.id}]').change(function () {
                                                    $('*[name=generateSlavedLics_${nextSub.id}][value=partial]').prop('checked') ? $('.generateSlavedLicsReference-wrapper_${nextSub.id}').removeClass('hidden') : $('.generateSlavedLicsReference-wrapper_${nextSub.id}').addClass('hidden');
                                                })
                                                $('*[name=generateSlavedLics_${nextSub.id}]').trigger('change')
                                                        </laser:script>
                                                    </g:if>
                                                    <g:else>
                                                        <g:message code="myinst.noSubscriptionOwner"/>
                                                    </g:else>
                                                </div>
                                            </div>

                                            <div class="item">
                                                <div class="field">
                                                    <label><g:message code="myinst.addMembers.linkPackages"/></label>

                                                    <div class="field">
                                                        <g:if test="${validPackagesNextSub}">
                                                            <div class="grouped fields">
                                                                <div class="field">
                                                                    <div class="ui checkboxLicAndPkg_${nextSub.id} checkbox">
                                                                        <input type="checkbox" id="linkAllPackages_${nextSub.id}"
                                                                               name="linkAllPackages_${nextSub.id}">
                                                                        <label for="linkAllPackages"><g:message
                                                                                code="myinst.addMembers.linkAllPackages"/></label>
                                                                    </div>
                                                                </div>

                                                                <div class="field">
                                                                    <g:if test="${nextSub.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_PARTIAL}">
                                                                        <div class="ui checkboxLicAndPkg_${nextSub.id} checkbox">
                                                                            <input type="checkbox" id="linkWithEntitlements_${nextSub.id}"
                                                                                   name="linkWithEntitlements_${nextSub.id}">
                                                                            <label for="linkWithEntitlements"><g:message
                                                                                    code="myinst.addMembers.withEntitlements"/></label>
                                                                        </div>
                                                                        <g:select class="ui search multiple dropdown"
                                                                                  optionKey="id" optionValue="${{ it.getPackageName() }}"
                                                                                  from="${validPackagesNextSub}" name="packageSelection_${nextSub.id}" value=""
                                                                                  noSelection='["": "${message(code: 'subscriptionsManagement.noSelection.package')}"]'/>
                                                                    </g:if>
                                                                </div>
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <g:message code="subscriptionsManagement.noValidPackages"/>
                                                        </g:else>
                                                    </div>
                                                </div>
                                            </div>

                                        </div>
                                    </div>
                                </div>

                                <laser:script file="${this.getGroovyPageFileName()}">
                                    $('.addToSubWithMultiYear_${nextSub.id}').checkbox({
                                        fireOnInit : true,
                                        onChecked: function() {
                                           var $childCheckbox  = $('.checkboxLicAndPkg_${nextSub.id}');
                                           $childCheckbox.checkbox('enable');
                                           var $childCheckbox2  = $('.checkboxGenerateSlavedLics1_${nextSub.id}');
                                           $childCheckbox2.checkbox('enable');
                                           $childCheckbox2.checkbox('check');

                                        },
                                        onUnchecked: function() {
                                           var $childCheckbox  = $('.checkboxLicAndPkg_${nextSub.id}');
                                           $childCheckbox.checkbox('uncheck');
                                           $childCheckbox.checkbox('disable');

                                            var $childCheckbox2  = $('.checkboxGenerateSlavedLics1_${nextSub.id}');
                                           $childCheckbox2.checkbox('uncheck');
                                           $childCheckbox2.checkbox('disable');

                                        }
                                    });
                                </laser:script>
                            </g:each>
                        </div>
                    </div>

                </ui:greySegment>
            </g:if>
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
                                        <g:if test="${prop.isLongType()}">
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
                                            <ui:select class="ui dropdown clearable search memberPropertyDropdown" name="propValue${i}"
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
                <input type="submit" class="${Btn.SIMPLE_CLICKCONTROL}" value="${message(code: 'default.button.create.label')}"/>
            </div>
            <input type="hidden" name="${FormService.FORM_SERVICE_TOKEN}" value="${formService.getNewToken()}"/>
        </g:if>
    </g:form>

    <g:if test="${contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
        <hr />

            <ui:msg class="info" header="${message(code: 'myinst.noMembers.cons.header')}" hideClose="true" message="myinst.noMembers.body" args="${[createLink(controller:'myInstitution', action:'manageMembers'),message(code:'consortium.member.plural')]}"/>
    </g:if>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.action .icon.button').click(function () {
         $(this).parent('.action').find('input:file').click();
     });

     $('input:file', '.ui.action.input').on('change', function (e) {
         var name = e.target.files[0].name;
         $('input:text', $(e.target).parent()).val(name);
     });

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

      $('.checkBoxSubRunTimeMultiYear').checkbox({
        fireOnInit : true,
        onChecked: function() {
          var $childCheckbox  = $('.checkboxAddToSubWithMultiYear');
          $childCheckbox.checkbox('enable');
        },
        onUnchecked: function() {
          var $childCheckbox  = $('.checkboxAddToSubWithMultiYear');
          $childCheckbox.checkbox('uncheck');
          $childCheckbox.checkbox('disable');

        }
  });
</laser:script>

<laser:htmlEnd />
