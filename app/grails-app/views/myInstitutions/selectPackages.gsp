<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'myinst.selectPackages.label', default:'Package Planning - Select Candidate Packages')}</title>
  </head>

  <body>
    <div class="container">
      ${message(code:'myinst.selectPackages.note', args:[subscriptionInstance,titles_in_this_sub])}
      <ul><g:each in="${subscriptionInstance.packages}" var="p">
        <li>${p?.pkg?.name}</li>
      </g:each></ul>
      <table>
        <tr>
          <th>${message(code:'package.content_provider', default:'Content Provider')}</th>
          <th>${message(code:'myinst.selectPackages.candidates', default:'Candidate Packages')}</th>
          <th>${message(code:'myinst.selectPackages.pkg_titles', default:'Titles in Package')}</th>
          <th>${message(code:'myinst.selectPackages.titles_no', default:'# Matching Titles')}</th>
          <th>${message(code:'tipp.platform', default:'Platform')}</th>
          <th>${message(code:'myinst.selectPackages.overlap', default:'Overlap')}</th>
          <th>${message(code:'default.select.label', default:'Select')}</th>
        </tr>
        <g:each in="${candidates}" var="c">
          <tr>
            <td>${c.value.pkg.contentProvider?.name}</td>
            <td>${c.value.pkg.name}</td>
            <td>${c.value.pkg_title_count}</td>
            <td>${c.value.titlematch}</td>
            <td>${c.value.platform.name}</td>
            <td>${c.value.titlematch/titles_in_this_sub*100}</td>
            <td><input type="checkbox"/></td>
          </tr>
        </g:each>
      </table>
    </div>
  </body>
</html>
