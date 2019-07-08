<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI">
    <title>${message(code:'laser', default:'LAS:eR')} : Application Security</title>
</head>
<body>

<semui:breadcrumbs>
    <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
    <semui:crumb text="Application Security" class="active"/>
</semui:breadcrumbs>

<h1 class="ui header">debug only</h1>


<pre>numberOfActiveUsers : ${numberOfActiveUsers}</pre>


<laser:remoteLink controller="ajax" action="readNote" id="1" xyz="xyz" abc="abc" update="#test123"
                  data-before="alert('data-before')" data-done="alert('data-done')" data-always="alert('data-always')">
    Click here @ div#test123
</laser:remoteLink>

<br/><br/>

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

com.k_int.kbplus.RefdataValue.executeQuery('select st from RefdataValue rdv, SystemTicket st where st.status = rdv') <br />
${com.k_int.kbplus.RefdataValue.executeQuery('select st from RefdataValue rdv, SystemTicket st where st.status = rdv')}

<hr />

<br />
<br />
<br />
<br />

com.k_int.kbplus.Subscription.findAll() <br />
${com.k_int.kbplus.Subscription.findAll()}

<hr />

com.k_int.kbplus.Subscription.executeQuery('select sub.id from Subscription sub join sub.status ') <br />
${com.k_int.kbplus.Subscription.executeQuery('select sub.id from Subscription sub join sub.status ') }

<hr />

com.k_int.kbplus.Subscription.executeQuery('select sub.id from Subscription sub join sub.ids ') <br />
${com.k_int.kbplus.Subscription.executeQuery('select sub.id from Subscription sub join sub.ids ') }

<hr />

<strong>FAIL</strong> <br />
com.k_int.kbplus.IdentifierOccurrence.executeQuery('select subsc.id from IdentifierOccurrence io join io.sub subsc') <br />
${com.k_int.kbplus.IdentifierOccurrence.executeQuery('select subsc.id from IdentifierOccurrence io join io.sub subsc') }

<hr />

com.k_int.kbplus.Subscription.executeQuery('select io.id from Subscription subsc join subsc.ids io ') <br />
${com.k_int.kbplus.Subscription.executeQuery('select io.id from Subscription subsc join subsc.ids io ') }
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
