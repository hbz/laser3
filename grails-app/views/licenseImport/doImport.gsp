<g:set var="entityName" value="${message(code: 'onixplLicense.license.label')}" />
<laser:htmlStart text="${message(code:"default.import.label", args:[entityName])}" />

        <g:unless test="${validationResult?.success}">
            <semui:h1HeaderWithIcon message="onix.import.license">
                <g:if test="${license}"> ${message(code:'onix.import.for_license', args:[license.reference])}</g:if>
                <g:else> ${message(code:'onix.import.unspec')}</g:else>
            </semui:h1HeaderWithIcon>
            <br />
        </g:unless>


    <semui:messages data="${flash}" />

    <semui:errors bean="${packageInstance}" />

    <%-- Show summary --%>
    <g:if test="${validationResult}">

        <g:if test="${validationResult.messages!=null}">
            <g:each in="${validationResult.messages}" var="msg">
                <div class="alert alert-info">${msg}</div>
            </g:each>
        </g:if>

        <g:if test="${validationResult.errors!=null}">
            <g:each in="${validationResult.errors}" var="msg">
                <div class="alert alert-error">${msg}</div>
            </g:each>
        </g:if>

        <g:if test="${validationResult.success==true}">
            <div class="alert alert-success">
                      <h2 class="ui header">${message(code:'onix.import.success')}</h2>

                      ${message(code:'onix.import.file', args:[upload_filename,upload_mime_type])}</strong>
            <g:if test="${validationResult.license}">
                ${message(code:'onix.import.assoc')}
                <g:link action="show"
                        controller="license"
                        class="ui button"
                        id="${validationResult.license.id}">
                    ${message(code:'license.label')} ${validationResult.license.id}
                </g:link>
                <strong>${validationResult.license.reference}.</strong>
            </g:if>
            <g:else>
                <br />
                ${message(code:'onix.import.existing_licenses')}
            </g:else>
        </g:if>
    <%-- Show the form if no OPL has been created --%>
        <g:else>
            <g:form action="doImport" method="post" enctype="multipart/form-data">
                <g:hiddenField name="license_id" value="${params.license_id!=""?params.license_id:license_id}" />
            <%-- Show overwrite option if there is an existing OPL --%>
                <g:if test="${existing_opl}">
                    ${message(code:'onix.import.dupe')}:
                    <div>
                        <g:link action="index"
                                controller="onixplLicense"
                                id="${existing_opl.id}">
                            ${existing_opl.title}
                        </g:link>
                    </div>
                    ${message(code:'onix.import.replace')}
                    <br />
                    <br />
                    <button name="replace_opl" id="replace_opl" value="replace"
                            type="submit" class="ui negative button">${message(code:'default.button.replace.label')}</button>
                    <button name="replace_opl" id="replace_opl" value="create"
                            type="submit" class="ui button">${message(code:'default.button.create_new.label')}</button>

                    <g:hiddenField name="upload_title" value="${upload_title}" />
                    <g:hiddenField name="uploaded_file" value="${uploaded_file}" />
                    <g:hiddenField name="description" value="${description}" />

                    <g:hiddenField name="upload_filename" value="${upload_filename}" />
                    <g:hiddenField name="upload_mime_type" value="${upload_mime_type}" />
                    <g:hiddenField name="existing_opl_id" value="${existing_opl.id}" />

                </g:if>
            <%-- Show  default options is there is no existing OPL and one has not been created --%>
                <g:else>
                <%--Upload File:--%>
                    <br />
                    <input type="file" id="import_file" name="import_file" value="${import_file}"/>
                    <br />
                    <br />
                    <button type="submit" class="ui button">${message(code:'onix.import.import')}</button>
                </g:else>
            </g:form>
        </g:else>


        <g:if test="${validationResult.termStatuses}">
            <h2 class="ui header">Usage terms summary</h2>
            <ul>
                <g:each in="${validationResult.termStatuses}" var="ts">
                    <li>${ts.value} &times ${ts.key}</li>
                </g:each>
            </ul>
        </g:if>

        <br />
        <g:if test="${validationResult.onixpl_license}">

            <%-- Show link to ONIX-PL display if no associated license specified, or multiple ones --%>
                <g:link action="index"
                        style="margin-top:10px;"
                        controller="onixplLicense"
                        class="ui button"
                        id="${validationResult.onixpl_license.id}">
                    ${message(code:'onix.import.view', args:[(validationResult.replace ? message(code:'onix.import.view.updated') : message(code:'onix.import.view.new'))])}</g:link>
        </g:if>
        </div>
    </g:if>

<laser:htmlEnd />
