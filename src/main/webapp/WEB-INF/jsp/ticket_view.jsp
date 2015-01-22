<%--
    Document   : info
    Author     : Andrey Svininykh (svininykh@gmail.com)
    Copyright  : Nord Trading Network
    License    : Apache License, Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0.html)
--%>

<%@include file="/WEB-INF/jsp/common/taglibs.jsp"%>
<stripes:layout-render name="/WEB-INF/jsp/common/layout_main.jsp"
                       title="Ticket View"
                       pageid="TicketView">

    <stripes:layout-component name="buttons_left">
        <sdynattr:link href="/Welcome.action"
                       class="ui-btn ui-corner-all ui-icon-home ui-btn-icon-notext">
            <fmt:message key="label.home" />
        </sdynattr:link>        
    </stripes:layout-component>

    <stripes:layout-component name="title">
        <fmt:message key="label.Ticket.number" />&nbsp;<c:out value="${actionBean.ticket.number}"/>
    </stripes:layout-component>

    <stripes:layout-component name="content">
        <div class="ui-body ui-body-a ui-corner-all">
            <div class="ui-grid-a ui-responsive">
                <div class="ui-block-a">
                    <div class="ui-body ui-body-c">
                        <fmt:message key="label.Ticket.date" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-c">
                        <fmt:formatDate value="${actionBean.ticket.receipt.date}"
                                        type="BOTH"/>
                    </div>
                </div>
            </div>
            <div class="ui-grid-a ui-responsive">
                <div class="ui-block-a">
                    <div class="ui-body ui-body-c">
                        <fmt:message key="label.Ticket.subValue" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-c">
                        <fmt:formatNumber value="${actionBean.ticket.subValue}"
                                          type="CURRENCY"
                                          pattern="#0.00 ¤"                                                  
                                          maxFractionDigits="2" 
                                          minFractionDigits="2"/>
                    </div>
                </div>
            </div>
            <div class="ui-grid-a ui-responsive">
                <div class="ui-block-a">
                    <div class="ui-body ui-body-c">
                        <fmt:message key="label.Ticket.taxAmount" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-c">
                        <fmt:formatNumber value="${actionBean.ticket.taxAmount}"
                                          type="CURRENCY"
                                          pattern="#0.00 ¤"                                                  
                                          maxFractionDigits="2" 
                                          minFractionDigits="2"/>
                    </div>
                </div>
            </div>
            <div class="ui-grid-a ui-responsive">
                <div class="ui-block-a">
                    <div class="ui-body ui-body-c">
                        <fmt:message key="label.Ticket.totalValue" />
                    </div>
                </div>
                <div class="ui-block-b">
                    <div class="ui-body ui-body-c">
                        <fmt:formatNumber value="${actionBean.ticket.totalValue}"
                                          type="CURRENCY"
                                          pattern="#0.00 ¤"                                                  
                                          maxFractionDigits="2" 
                                          minFractionDigits="2"/>
                    </div>
                </div>
            </div>            
        </div>

        <table data-role="table" 
               id="ticket-table" 
               data-mode="columntoggle" 
               class="ui-body-d ui-shadow table-stripe ui-responsive"
               data-column-btn-theme="b" 
               data-column-btn-text="..." 
               data-column-popup-theme="a"
               cellspacing="0" cellpadding="0">
            <thead>
                <tr class="ui-bar-b">
                    <th data-priority="persist" style="width: 10%"><fmt:message key="label.line.number" /></th>
                    <th data-priority="persist" style="width: 50%"><fmt:message key="label.line.name" /></th>                    
                    <th data-priority="persist" style="width: 10%"><fmt:message key="label.line.unit" /></th>
                    <th data-priority="3" style="width: 15%"><fmt:message key="label.line.value" /></th>
                </tr>
            </thead>
            <tbody>
                <c:forEach items="${actionBean.ticket.ticketLineList}" var="line">
                    <tr>
                        <th style="text-align: center;"><c:out value="${line.number + 1}" /></th>
                        <td><c:out value="${line.product.name}" /></td>
                        <td style="text-align: center;"><fmt:formatNumber value="${line.unit}"
                                          type="NUMBER"                                   
                                          maxFractionDigits="3"/></td>
                        <td style="text-align: right;"><fmt:formatNumber value="${line.taxPrice}"
                                          type="CURRENCY"
                                          pattern="#0.00 ¤"                                                  
                                          maxFractionDigits="2" 
                                          minFractionDigits="2"/></td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>

    </stripes:layout-component>

    <stripes:layout-component name="footer">

    </stripes:layout-component>
</stripes:layout-render>
