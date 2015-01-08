<%--
    Document   : user_view
    Author     : Andrey Svininykh (svininykh@gmail.com)
    Copyright  : Nord Trading Network
    License    : Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)
--%>

<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<stripes:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
                       title="User"
                       pageid="User">

    <stripes:layout-component name="buttons_left">
        <sdynattr:link href="/Welcome.action"
                       class="ui-btn ui-corner-all ui-icon-home ui-btn-icon-notext">
            <fmt:message key="label.home" />
        </sdynattr:link>        
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <fmt:message key="label.CustomerLogIn" />
    </stripes:layout-component>

    <stripes:layout-component name="buttons_right">
        <a href="#customer_logout" 
           data-rel="popup" 
           data-position-to="window" 
           data-transition="pop" 
           class="ui-btn ui-corner-all ui-icon-delete ui-btn-icon-left ui-btn-b ui-shadow">
            <fmt:message key="label.CustomerLogOut" />
        </a>
        <div data-role="popup" id="customer_logout" data-overlay-theme="b" data-theme="b" data-dismissible="false" style="max-width:400px;">
            <div data-role="header" data-theme="a">
                <h1><fmt:message key="label.dialog.CustomerLogOut" /></h1>
            </div>
            <div role="main" class="ui-content">
                <h3 class="ui-title"><fmt:message key="label.ask.CustomerLogOut" /></h3>
                <fieldset class="ui-grid-a">
                    <div class="ui-block-a">
                        <a href="#" 
                           class="ui-btn ui-corner-all ui-icon-forbidden ui-btn-icon-left ui-btn-b ui-shadow" 
                           data-rel="back" 
                           data-transition="flow">
                            <fmt:message key="no" />
                        </a>
                    </div>
                    <div class="ui-block-b">
                        <sdynattr:link href="/CustomerAuthorization.action"
                                       event="logout"
                                       class="ui-btn ui-corner-all ui-icon-check ui-btn-icon-left ui-btn-a ui-shadow">
                            <fmt:message key="yes" />
                        </sdynattr:link>
                    </div>
                </fieldset>
            </div>
        </div>
    </stripes:layout-component>

    <stripes:layout-component name="content">
        <div class="ui-body ui-body-a ui-corner-all" data-inset="true">
            <div class="ui-grid-a ui-responsive">
                <div class="ui-block-a">
                    <div class="ui-body ui-body-d">
                        <fmt:message key="label.Customer.name" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-d">
                        <c:out value="${customer.name}"/>
                    </div>                    
                </div>
                <div class="ui-block-a">
                    <div class="ui-body ui-body-d">
                        <fmt:message key="label.Customer.nickname" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-d">
                        <c:out value="${customer.searchkey}"/>
                    </div>                    
                </div>
            </div>
        </div>        
    </stripes:layout-component>

    <stripes:layout-component name="footer">

    </stripes:layout-component>
</stripes:layout-render>
