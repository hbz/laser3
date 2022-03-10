<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'myinst.selectPackages.label')}</title>
  </head>

  <body>
    <div>
      ${message(code:'myinst.selectPackages.note', args:[subscription,titles_in_this_sub])}
      <ul><g:each in="${subscription.packages}" var="p">
        <li>${p.pkg.name}</li>
      </g:each></ul>
       <table class="ui celled la-js-responsive-table la-table table">
        <tr>
          <th>${message(code:'package.content_provider')}</th>
          <th>${message(code:'myinst.selectPackages.candidates')}</th>
          <th>${message(code:'myinst.selectPackages.pkg_titles')}</th>
          <th>${message(code:'myinst.selectPackages.titles_no')}</th>
          <th>${message(code:'tipp.platform')}</th>
          <th>${message(code:'myinst.selectPackages.overlap')}</th>
          <th>${message(code:'default.select.label')}</th>
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
