<%--
    Document   : order_payment
    Author     : Andrey Svininykh (svininykh@gmail.com)
    Copyright  : Nord Trading Network.
--%>

<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<stripes:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
                       title="Payment Select"
                       pageid="PaymentSelect">

    <stripes:layout-component name="buttons_left">
        <sdynattr:link href="/Welcome.action"
                       class="ui-btn ui-corner-all ui-icon-home ui-btn-icon-notext">
            <fmt:message key="label.home" />
        </sdynattr:link>
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <fmt:message key="label.PaymentSelect"/>
    </stripes:layout-component>

    <stripes:layout-component name="content">
        <stripes:form action="/OrderPayment.action">
            <ul data-role="listview" data-inset="true">
                <li class="ui-field-contain">
                    <stripes:label name="label.payment.amount" for="payAmount" />
                    <input type="text"
                           name="paymentAmount" id="payAmount"
                           value="${actionBean.total}" 
                           data-clear-btn="true"
                           placeholder="<fmt:message key="label.PayAmount.enter" />"/>
                </li>
                <li class="ui-field-contain">
                    <stripes:label name="label.payment.type" for="paymentType" />
                    <sdynattr:select name="paymentType" id="paymentType">
                        <stripes:option value="cash">
                            <fmt:message key="label.payment.cash" />
                        </stripes:option>
                        <stripes:option value="magcard">
                           <fmt:message key="label.payment.magcard" />
                        </stripes:option>
                    </sdynattr:select>
                </li>
                <li class="ui-body ui-body-b">
                    <fieldset class="ui-grid-a">
                        <div class="ui-block-a">
                            <sdynattr:reset name="clear" data-theme="b"/>
                        </div>
                        <div class="ui-block-b">
                            <sdynattr:submit name="post" data-theme="a"/>
                        </div>                        
                    </fieldset>
                </li>
            </ul>
        </stripes:form>

    </stripes:layout-component>

    <stripes:layout-component name="footer">

    </stripes:layout-component>
</stripes:layout-render>
