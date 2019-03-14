<%@page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'default.documents.label')}</title>
  </head>

  <body>
    <%
      List notAvailable = [RefdataValue.getByValueAndCategory('ONIX-PL License','Document Type'),
                           RefdataValue.getByValueAndCategory('Note','Document Type'),
                           RefdataValue.getByValueAndCategory('Announcement','Document Type')]
      List documentTypes = RefdataCategory.getAllRefdataValues("Document Type")-notAvailable
    %>
    <semui:breadcrumbs>
      <semui:crumb controller="myInstitution" action="dashboard" text="${org.getDesignation()}" />
      <semui:crumb message="default.documents.label" class="active"/>
    </semui:breadcrumbs>

    <semui:controlButtons>
      <g:render template="actions" model="[org:org]" />
    </semui:controlButtons>
    <semui:messages data="${flash}" />

    <h1 class="ui left aligned icon header"><semui:headerIcon />${org.name}</h1>

    <semui:messages data="${flash}" />

    <semui:filter>
      <g:form class="ui form" controller="myInstitution" action="documents">
        <div class="two fields">
          <div class="field">
            <label for="docTitle">${message(code:'license.docs.table.title', default:'Title')}</label>
            <input type="text" id="docTitle" name="docTitle" value="${params.docTitle}">
          </div>
          <div class="field">
            <label for="docFilename">${message(code:'license.docs.table.fileName', default:'File Name')}</label>
            <input type="text" id="docFilename" name="docFilename" value="${params.docFilename}">
          </div>
        </div>
        <div class="two fields">
          <div class="field">
            <label for="docCreator">${message(code:'license.docs.table.creator', default:'Creator')}</label>
            <g:select class="ui fluid search dropdown" name="docCreator" from="${availableUsers}" optionKey="id" optionValue="display" value="${params.docCreator}" noSelection="['':'']"/>
          </div>
          <div class="field">
            <label for="docType">${message(code:'license.docs.table.type', default:'Type')}</label>
            <g:select class="ui fluid search dropdown" name="docType" from="${documentTypes}" optionKey="id" optionValue="${{it.getI10n("value")}}" value="${params.docType}" noSelection="['':'']"/>
          </div>
        </div>
        <div class="two fields">
          <div class="field">
            <label for="docOwnerOrg">${message(code:'org.docs.table.ownerOrg')}</label>
            <g:select class="ui fluid search dropdown" name="docOwnerOrg" from="${Org.executeQuery("select o from Org o order by o.name")}" optionKey="id" optionValue="name" value="${params.docOwnerOrg}" noSelection="['':'']"/>
          </div>
          <div class="field">
            <label for="docTargetOrg">${message(code:'org.docs.table.targetOrg')}</label>
            <g:select class="ui fluid search dropdown" name="docTargetOrg" from="${Org.executeQuery("select o from Org o order by o.name")}" optionKey="id" optionValue="name" value="${params.docTargetOrg}" noSelection="['':'']"/>
          </div>
        </div>
        <div class="two fields">
          <div class="field">
            <label for="docShareConf">${message(code:'template.addDocument.shareConf')}</label>
            <laser:select name="docShareConf" class="ui dropdown fluid" value="${params.docShareConf}" noSelection="['':'']"
                          from="${RefdataValue.executeQuery("select rdv from RefdataValue rdv where rdv.owner.desc = 'Share Configuration' order by rdv.order asc")}" optionKey="id" optionValue="value"/>
          </div>
          <div class="field la-field-right-aligned">
            <a href="${request.forwardURI}" class="ui reset primary primary button">${message(code:'default.button.reset.label')}</a>

            <input type="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}" />
          </div>
        </div>
      </g:form>
    </semui:filter>

    <g:render template="/templates/documents/table" model="${[instance:org, context:'documents', redirect:'documents', consortiaMembers: consortiaMembers]}"/>
  </body>
</html>
