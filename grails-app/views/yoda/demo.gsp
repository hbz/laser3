<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Application Security</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Application Security" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header la-noMargin-top">debug only</h1>

<div class="ui raised segment">
    <div class="ui medium header">&lt;semui:link generateElementId="true" &gt;</div>

    <semui:link generateElementId="true" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>
    <semui:link generateElementId="true" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>
    <semui:link generateElementId="true" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>

    <semui:link elementId="aaa-logout" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>

    <g:link elementId="bbb-logout" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</g:link>

    <semui:link generateElementId="true" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>
    <semui:link generateElementId="true" class="item" controller="logout" action="index">${message(code:'menu.user.logout')}</semui:link>
</div>

<div class="ui raised segment">
    <div class="ui medium header">Ajax @ de.laser.ajax</div>

    <p><g:link controller="ajaxJson" action="test" id="111" target="_blank">controller="ajaxJson" &rArr; ajax/json/test</g:link></p>

    <p><g:link controller="ajaxHtml" action="test" id="222" target="_blank">controller="ajaxHtml" &rArr; ajax/html/test</g:link></p>

    <p><a href="../ajax/json/test?id=333">native href &rArr; ajax/json/test</a></p>

    <p><a href="../ajax/html/test?id=444">native href &rArr; ajax/html/test</a></p>
</div>

<pre>numberOfActiveUsers : ${numberOfActiveUsers}</pre>

<h3 class="ui header">TEST: SwissKnife.checkMessageKey()</h3>

<semui:msg class="warning" header="${message(code: 'message.information')}" message="x.myinst.addressBook.visible" />
<semui:msg class="warning" header="${message(code: 'message.information')}" message="${message(code: 'myinst.addressBook.visible')}" />
<semui:msg class="warning" header="${message(code: 'message.information')}" message="myinst.addressBook.visible" />


<laser:remoteLink controller="ajaxHtml"
                  action="readNote"
                  id="1"
                  xyz="xyz"
                  abc="abc"
                  update="#test123"
                  data-before="alert('data-before')" data-done="alert('data-done')" data-always="alert('data-always')">
    Click here @ div#test123
</laser:remoteLink>

<br /><br />

<div id="test123"> div#test123 </div>


<%--
com.k_int.kbplus.SystemTicket.findAll() <br />
${com.k_int.kbplus.SystemTicket.findAll()}

<hr />

com.k_int.kbplus.SystemTicket.findById(1) <br />
${com.k_int.kbplus.SystemTicket.findById(1)}

<hr />

com.k_int.kbplus.SystemTicket.findById(3) <br />
${com.k_int.kbplus.SystemTicket.findById(3)}

<hr />

com.k_int.kbplus.SystemTicket.executeQuery('select st from SystemTicket st') <br />
${com.k_int.kbplus.SystemTicket.executeQuery('select st from SystemTicket st') }

<hr />

com.k_int.kbplus.SystemTicket.executeQuery('select st from SystemTicket st join st.status ') <br />
${com.k_int.kbplus.SystemTicket.executeQuery('select st from SystemTicket st join st.status ') }

<hr />

de.laser.RefdataValue.executeQuery('select st from RefdataValue rdv, SystemTicket st where st.status = rdv') <br />
${de.laser.RefdataValue.executeQuery('select st from RefdataValue rdv, SystemTicket st where st.status = rdv')}

<hr />

<br />
<br />
<br />
<br />

de.laser.Subscription.findAll() <br />
${de.laser.Subscription.findAll()}

<hr />

de.laser.Subscription.executeQuery('select sub.id from Subscription sub join sub.status ') <br />
${de.laser.Subscription.executeQuery('select sub.id from Subscription sub join sub.status ') }

<hr />

de.laser.Subscription.executeQuery('select sub.id from Subscription sub join sub.ids ') <br />
${de.laser.Subscription.executeQuery('select sub.id from Subscription sub join sub.ids ') }

<hr />

de.laser.Subscription.executeQuery('select io.id from Subscription subsc join subsc.ids io ') <br />
${de.laser.Subscription.executeQuery('select io.id from Subscription subsc join subsc.ids io ') }
--%>

<%--
<hr />

<br />
<br />
<br />
<br />


<br />'select u from User u where u.accountLocked = true and u.id < 4'
<br />${q1}

<hr />

<br />'select u from User u where u.accountLocked != true and u.id < 4'
<br />${q2}

<hr />

<br />'select u from User u where u.accountLocked = false and u.id < 4'
<br />${q3}

<hr />

<br />'select u from User u where u.accountLocked != false and u.id < 4'
<br />${q4}

<hr />

<br />'select u from User u where u.accountLocked is not null and u.id < 4')
<br />${q6}

<hr />
--%>
--


<pre>
user: ${user}

roles: <g:each in="${roles}" var="role"><br />${role}</g:each>

affiliations: <g:each in="${affiliations}" var="aff"><br />${aff}</g:each>

${check1}

${check2}

${check3}

${check4}
</pre>

</body>
</html>
