<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
        	<img class="brand-img" alt="LAS:eR" src="${resource(dir: 'images', file: 'laser-logo-1.png')}"/>
            <ul class="nav">
                <g:if test="${active.equals("home")}">
                    <li id="home" class="active">
                </g:if>
                <g:else><li id="home"></g:else>
                <a href="${createLink(uri: '/')}">${message(code:'default.home.label', default:'Home')}</a>
            </li>
                <g:if test="${active.equals("about")}">
                    <li id="about" class="active">
                </g:if>
                <g:else>
                    <li id="about">
                </g:else>
                <a href="${createLink(uri: '/about')}">${message(code:'public.nav.about.label', default:'About LAS:eR')}</a>
            </li>
                <g:if test="${active.equals("signup")}">
                    <li id="signup" class="active">

                </g:if>
                <g:else>
                    <li id="signup">

                </g:else>

                <a href="${createLink(uri: '/signup')}">${message(code:'public.nav.signUp.label', default:'Sign Up')}</a>
            </li>
               
          <li class="${active.equals('publicExport')?'active':''}">
                            <a href="${createLink(uri: '/publicExport')}">${message(code:'public.nav.exports.label', default:'Exports')}</a>

            </li>
                <g:if test="${active.equals("contact")}">
                    <li id="contact" class="active">

                </g:if>
                <g:else>
                    <li id="contact">

                </g:else>

                <a href="${createLink(uri: '/contact-us')}">${message(code:'public.nav.contact.label', default:'Contact Us')}</a>
            </li>
            </ul>
        </div>
    </div>
</div>


