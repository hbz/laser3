<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'onixplLicense.license.label',
            default: 'ONIX-PL License')}" />
    <title><g:message code="default.import.label" args="[entityName]" /></title>
</head>
<body>
<div class="container">

    <div class="page-header">
        <g:unless test="${validationResult?.success}">
            <h1>${message(code:'onix.import.license', default:'Import ONIX-PL License')}
            <g:if test="${license}"> ${message(code:'onix.import.for_license', args:[license.reference])}</g:if>
            <g:else> ${message(code:'onix.import.unspec', default:'for unspecified license')}</g:else>
            </h1>
        </g:unless>
    </div>

    <g:if test="${flash.message}">
        <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
    </g:if>

    <g:if test="${flash.error}">
        <bootstrap:alert class="alert-info">${flash.error}</bootstrap:alert>
    </g:if>

    <g:hasErrors bean="${packageInstance}">
        <bootstrap:alert class="alert-error">
            <ul>
                <g:eachError bean="${packageInstance}" var="error">
                    <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
                </g:eachError>
            </ul>
        </bootstrap:alert>
    </g:hasErrors>

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
                      <h2>${message(code:'onix.import.success', default:'Upload successful')}</h2>

                      ${message(code:'onix.import.file', args:[upload_filename,upload_mime_type])}</b>
            <g:if test="${validationResult.license}">
                ${message(code:'onix.import.assoc', default:'and associated with')}
                <g:link action="index"
                        controller="licenseDetails"
                        class="btn btn-info"
                        id="${validationResult.license.id}">
                    ${message(code:'license.label')} ${validationResult.license.id}
                </g:link>
                <b>${validationResult.license.reference}.</b>
            </g:if>
            <g:else>
                <br/>
                ${message(code:'onix.import.existing_licenses', default:'Existing associations with LAS:eR licenses were maintained.')}
            </g:else>
        </g:if>
    <%-- Show the form if no OPL has been created --%>
        <g:else>
            <g:form action="doImport" method="post" enctype="multipart/form-data">
                <g:hiddenField name="license_id" value="${params.license_id!=""?params.license_id:license_id}" />
            <%-- Show overwrite option if there is an existing OPL --%>
                <g:if test="${existing_opl}">
                    ${message(code:'onix.import.dupe', default:'This ONIX-PL document appears to describe an existing ONIX-PL license')}:
                    <div class="well">
                        <g:link action="index"
                                controller="onixplLicenseDetails"
                                id="${existing_opl.id}">
                            ${existing_opl.title}
                        </g:link>
                    </div>
                    ${message(code:'onix.import.replace', default:'Would you like to replace the existing ONIX-PL license or create a new record?')}
                    <br/>
                    <br/>
                    <button name="replace_opl" id="replace_opl" value="replace"
                            type="submit" class="ui negative button">${message(code:'default.button.replace.label', default:'Replace')}</button>
                    <button name="replace_opl" id="replace_opl" value="create"
                            type="submit" class="ui primary button">${message(code:'default.button.create_new.label', default:'Create New')}</button>

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
                    <br/>
                    <input type="file" id="import_file" name="import_file" value="${import_file}"/>
                    <br/>
                    <br/>
                    <button type="submit" class="ui primary button">${message(code:'onix.import.import', default:'Import license')}</button>
                </g:else>
            </g:form>
        </g:else>


        <g:if test="${validationResult.termStatuses}">
            <h2>Usage terms summary</h2>
            <ul>
                <g:each in="${validationResult.termStatuses}" var="ts">
                    <li>${ts.value} &times ${ts.key}</li>
                </g:each>
            </ul>
        </g:if>

        <br/>
        <g:if test="${validationResult.onixpl_license}">

            <%-- Show link to ONIX-PL display if no associated license specified, or multiple ones --%>
                <g:link action="index"
                        style="margin-top:10px;"
                        controller="onixplLicenseDetails"
                        class="btn btn-info"
                        id="${validationResult.onixpl_license.id}">
                    ${message(code:'onix.import.view', args:[(validationResult.replace ? message(code:'onix.import.view.updated', default:'updated') : message(code:'onix.import.view.new', default:'new'))])}</g:link>
        </g:if>
        </div>
    </g:if>

</div>
</body>
</html>
