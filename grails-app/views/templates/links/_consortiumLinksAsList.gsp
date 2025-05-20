<%@ page import="de.laser.addressbook.Person; de.laser.addressbook.Contact; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.addressbook.PersonRole; de.laser.RefdataValue; de.laser.storage.RDConstants; de.laser.storage.RDStore" %>
<laser:serviceInjection/>
<g:if test="${consortium}">
    <table class="ui compact table">
        <tr>
            <td>
                <span class="la-flexbox la-minor-object">
                    <i class="${Icon.AUTH.ORG_CONSORTIUM} la-list-icon la-popup-tooltip la-delay" data-content="${message(code: 'consortium')}"></i>
                    <g:link controller="organisation" action="show" id="${consortium.id}">
                        ${consortium.name}
                    </g:link>
                </span>
            </td>
        </tr>
        <g:if test="${(Person.getPublicByOrgAndFunc(consortium, 'General contact person') || (Person.getPublicByOrgAndFunc(consortium, 'Technical Support')) || (Person.getPublicByOrgAndFunc(consortium, 'Service Support')) || (Person.getPublicByOrgAndFunc(consortium, 'Metadata Contact')) ||
                Person.getPublicByOrgAndObjectResp(consortium, roleObject, roleRespValue) ||
                Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'General contact person') ||
                Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Technical Support') ||
                Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Service Support') ||
                Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Metadata Contact') ||
                Person.getPrivateByOrgAndObjectRespFromAddressbook(consortium, roleObject, roleRespValue))}">
            <tr>
                <td colspan="3" style="padding-bottom:0;">
                <%-- public --%>
                    <g:if test="${Person.getPublicByOrgAndFunc(consortium, 'General contact person') ||
                            Person.getPublicByOrgAndFunc(consortium, 'Technical Support') ||
                            Person.getPublicByOrgAndFunc(consortium, 'Service Support') ||
                            Person.getPublicByOrgAndFunc(consortium, 'Metadata Contact') ||
                            Person.getPublicByOrgAndObjectResp(consortium, roleObject, roleRespValue)}">
                        <div class="ui segment la-timeLineSegment-contact">
                            <div class="la-timeLineGrid">
                                <div class="ui grid">
                                    <g:each in="${Person.getPublicByOrgAndFunc(consortium, 'General contact person')}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PUBLIC} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.public')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentType(
                                                        func,
                                                        RDStore.CCT_EMAIL
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                <%--<g:if test="${roleObject instanceof de.laser.Package}">--%>
                                    <%
                                        Set<Person> techSupports = Person.getPublicByOrgAndFunc(consortium, 'Technical Support'), serviceSupports = Person.getPublicByOrgAndFunc(consortium, 'Service Support'), metadataContacts = Person.getPublicByOrgAndFunc(consortium, 'Metadata Contact')
                                    %>
                                    <g:each in="${techSupports}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PUBLIC} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.public')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                    <g:each in="${serviceSupports}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PUBLIC} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.public')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                    <g:each in="${metadataContacts}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PUBLIC} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.public')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentType(
                                                        func,
                                                        RDStore.CCT_EMAIL
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                <%--</g:if>--%>
                                    <g:each in="${Person.getPublicByOrgAndObjectResp(consortium, roleObject, roleRespValue)}" var="resp">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PUBLIC} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.public')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${resp}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        resp,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </g:if>
                <%-- public --%>

                <%-- private --%>
                    <g:if test="${Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'General contact person') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Technical Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Service Support') ||
                            Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Metadata Contact') ||
                            Person.getPrivateByOrgAndObjectRespFromAddressbook(consortium, roleObject, roleRespValue)}">
                        <div class="ui segment la-timeLineSegment-contact">
                            <div class="la-timeLineGrid">
                                <div class="ui grid">
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'General contact person')}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PRIVATE} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.private')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_GENERAL_CONTACT_PRS.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                <%--<g:if test="${roleObject instanceof de.laser.Package}">--%>
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Technical Support')}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PRIVATE} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.private')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_TECHNICAL_SUPPORT.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Service Support')}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PRIVATE} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.private')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${RDStore.PRS_FUNC_SERVICE_SUPPORT.getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                    <g:each in="${Person.getPrivateByOrgAndFuncFromAddressbook(consortium, 'Metadata Contact')}" var="func">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PRIVATE} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.private')}"></i>
                                            </div>

                                            <div class="thirteen wide column">
                                                <div class="ui label">
                                                    ${(RefdataValue.getByValueAndCategory('Metadata Contact', RDConstants.PERSON_FUNCTION)).getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${func}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        func,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                <%--</g:if>--%>
                                    <g:each in="${Person.getPrivateByOrgAndObjectRespFromAddressbook(consortium, roleObject, roleRespValue)}"
                                            var="resp">
                                        <div class="row">
                                            <div class="two wide column">
                                                <i class="circular large ${Icon.ACP_PRIVATE} la-timeLineIcon la-timeLineIcon-contact la-popup-tooltip la-delay"
                                                   data-content="${message(code: 'address.private')}"></i>
                                            </div>

                                            <div class="twelve wide column">
                                                <div class="ui label">
                                                    ${(RefdataValue.getByValue(roleRespValue)).getI10n('value')}
                                                </div>

                                                <div class="ui header">
                                                    ${resp}
                                                </div>
                                                <g:each in="${Contact.findAllByPrsAndContentTypeInList(
                                                        resp,
                                                        [RDStore.CCT_EMAIL, RDStore.CCT_URL]
                                                )}" var="contact">
                                                    <laser:render template="/addressbook/contact" model="${[
                                                            contact             : contact,
                                                            tmplShowDeleteButton: false,
                                                            overwriteEditable   : false
                                                    ]}"/>
                                                </g:each>
                                            </div>
                                        </div>
                                    </g:each>
                                </div>
                            </div>
                        </div>
                    </g:if>
                <%-- private --%>
                </td>
            </tr>
        </g:if>
    </table>
</g:if>



