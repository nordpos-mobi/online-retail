<%--
    Document   : customer_login
    Author     : Andrey Svininykh (svininykh@gmail.com)
    Copyright  : Nord Trading Network
    License    : Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)
--%>

<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<stripes:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
                       title="Customer Authorization"
                       pageid="CudtomerAuthorization">

    <stripes:layout-component name="buttons_left">
        <sdynattr:link href="/Welcome.action"
                       class="ui-btn ui-corner-all ui-icon-home ui-btn-icon-notext">
            <fmt:message key="label.home" />
        </sdynattr:link>
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <fmt:message key="label.CustomerAuthorization" />
    </stripes:layout-component>

    <stripes:layout-component name="buttons_right">
        <sdynattr:link href="/CustomerRegistration.action"
                       class="ui-btn ui-corner-all ui-icon-lock ui-btn-icon-left"
                       role="button">
            <fmt:message key="label.register" />
        </sdynattr:link>
    </stripes:layout-component>

    <stripes:layout-component name="content">
        <div class="ui-body">            
            <a href="http://www.nordpos.mobi">
                <img src="<c:url value='/image/logo.png' />" alt="NORD POS mobi"/>
            </a>
            <stripes:form action="/CustomerAuthorization.action">
                <div class="ui-field-contain">
                    <stripes:label name="label.login.name"
                                   for="loginName" />
                    <input type="text"
                           name="customer.name"
                           id="loginName"
                           data-clear-btn="true"
                           placeholder="<fmt:message key='label.LoginName.enter' />"
                           value=""/>
                </div>
                <div class="ui-field-contain">
                    <stripes:label name="label.login.password"
                                   for="loginPassword" />
                    <input type="password"
                           name="enterPassword"
                           id="loginPassword"
                           data-clear-btn="true"
                           placeholder="<fmt:message key='label.LoginPassword.enter' />"
                           value=""/>
                </div>            
                <stripes:hidden name="targetUrl" />
                <stripes:submit name="login"/>
            </stripes:form>
        </div>
    </stripes:layout-component>

    <stripes:layout-component name="footer">

    </stripes:layout-component>
</stripes:layout-render>
