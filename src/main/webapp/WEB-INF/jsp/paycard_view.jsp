<%--
    Document   : paycard_view
    Author     : Andrey Svininykh (svininykh@gmail.com)
    Copyright  : Nord Trading Network
    License    : Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)
--%>

<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<stripes:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
                       title="Card Payment"
                       pageid="CardPayment">

    <stripes:layout-component name="buttons_left">
        <sdynattr:link href="/Welcome.action"
                       class="ui-btn ui-corner-all ui-icon-home ui-btn-icon-notext">
            <fmt:message key="label.home" />
        </sdynattr:link>                 
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <fmt:message key="label.CardPayment" />
    </stripes:layout-component>

    <stripes:layout-component name="buttons_right">
    </stripes:layout-component>

    <stripes:layout-component name="content">
        <stripes:form action="/OrderPost.action">
            <div>                
                <stripes:hidden name="payment.type" value="${actionBean.payment.type}"/>
                <stripes:hidden name="payment.amount" value="${actionBean.payment.amount}"/>
            </div>
            <ul data-role="listview" data-inset="true">                
                <li class="ui-field-contain">
                    <stripes:label name="label.card.number" for="cardNumber" />
                    <input name="cardNumber" id="cardNumber" type="text"
                           placeholder="<fmt:message key='label.CardNumber.enter' />"
                           data-clear-btn="true">
                </li>
                <li class="ui-field-contain">
                    <stripes:label name="label.card.expirationMonth" for="cardExpMonth" />
                    <input name="cardExpMonth" id="cardExpMonth" type="text"
                           placeholder="<fmt:message key='label.cardExpMonth.enter' />"
                           data-clear-btn="true">
                </li>
                <li class="ui-field-contain">
                    <stripes:label name="label.card.expirationYear" for="cardExpYear" />
                    <input name="cardExpYear" id="cardExpYear"  type="text"
                           placeholder="<fmt:message key='label.cardExpYear.enter' />"
                           data-clear-btn="true">
                </li>
                <li class="ui-body ui-body-b">
                    <fieldset class="ui-grid-a">
                        <div class="ui-block-a">
                            <sdynattr:reset name="clear" data-theme="b"/>
                        </div>
                        <div class="ui-block-b">
                            <sdynattr:submit name="accept" data-theme="a"/>
                        </div>
                    </fieldset>
                </li>
            </ul>        
        </stripes:form>
    </stripes:layout-component>

    <stripes:layout-component name="footer">

    </stripes:layout-component>
</stripes:layout-render>
