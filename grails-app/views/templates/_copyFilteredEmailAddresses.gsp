<!-- _copyEmailAddresses.gsp -->
<%@ page import="de.laser.PersonRole; de.laser.Contact; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.Org; de.laser.Person" %>
<laser:serviceInjection />

<g:set var="modalID" value="${modalID ?: 'copyFilteredEmailAddresses_ajaxModal'}"/>

<ui:modal id="${modalID}" text="${orgList ? message(code:'menu.institutions.copy_emailaddresses', args:[orgList.size()?:0]) : message(code:'menu.institutions.copy_emailaddresses.button')}" hideSubmitButton="true">

    <div class="ui form">
        <%--<div class="field">
            <g:textArea id="filteredEmailAddressesTextArea" name="filteredEmailAddresses" readonly="false"
                        rows="5" cols="1" class="myTargetsNeu" style="width: 100%;" >${emailAddresses ? emailAddresses.join('; '): ''}</g:textArea>

        </div>--%>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToClipboard()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_clipboard')}
        </button>
        <button class="ui icon button right floated" onclick="JSPC.app.copyToEmailProgram()">
            ${message(code:'menu.institutions.copy_emailaddresses_to_emailclient')}
        </button>
        <br />
    </div>
    <table class="ui table">
        <thead>
        <tr>
            <th><g:checkBox name="copyMailToggler" id="copyMailToggler" checked="true"/></th>
            <th><g:message code="default.name.label"/></th>
            <th>${RDStore.CCT_EMAIL.getI10n('value')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${emailAddresses}" var="row">
            <g:set var="org" value="${row.getKey()}"/>
            <tr id="${org.id}">
                <td><g:checkBox id="toCopyMail_${org.id}" name="copyMail" class="orgSelector" value="${org.id}" checked="true"/></td>
                <td>${org.name} (${org.sortname})</td>
                <td><span class="address">${row.getValue().join('; ')}</span></td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.copyToEmailProgram = function () {
            let emailAdresses = $(".address:visible").map((i, el) => el.innerText.trim()).get().join('; ');
            window.location.href = "mailto:" + emailAdresses;
        }

        JSPC.app.copyToClipboard = function () {
            let textArea = document.createElement("textarea");
            textArea.value = $(".address:visible").map((i, el) => el.innerText.trim()).get().join('; ');
            $("body").append(textArea);
            textArea.select();
            document.execCommand("copy");
            textArea.remove();
        }

        $('.orgSelector').change(function() {
            $('#org'+$(this).val()+' span.address').toggle();
        });

        $('#copyMailToggler').change(function() {
            if ($(this).prop('checked')) {
                $(".orgSelector").prop('checked', true);
                $('.address').show();
            } else {
                $(".orgSelector").prop('checked', false);
                $('.address').hide();
            }
        });
    </laser:script>

</ui:modal>
<!-- _copyEmailAddresses.gsp -->
