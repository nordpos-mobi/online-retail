/**
 * Copyright (c) 2012-2015 Nord Trading Network.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package mobi.nordpos.retail.action;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import mobi.nordpos.dao.model.ClosedCash;
import mobi.nordpos.dao.model.Payment;
import mobi.nordpos.dao.model.Payment.PaymentType;
import mobi.nordpos.dao.model.Receipt;
import mobi.nordpos.dao.model.SharedTicket;
import mobi.nordpos.dao.model.Ticket;
import mobi.nordpos.dao.model.Ticket.TicketType;
import mobi.nordpos.dao.model.TicketNumber;
import mobi.nordpos.dao.model.User;
import mobi.nordpos.dao.factory.ClosedCashPersist;
import mobi.nordpos.dao.factory.PaymentPersist;
import mobi.nordpos.dao.factory.ReceiptPersist;
import mobi.nordpos.dao.factory.StockCurrentPersist;
import mobi.nordpos.dao.factory.StockDiaryPersist;
import mobi.nordpos.dao.factory.TaxLinePersist;
import mobi.nordpos.dao.factory.TicketLinePersist;
import mobi.nordpos.dao.factory.TicketNumberPersist;
import mobi.nordpos.dao.factory.TicketPersist;
import mobi.nordpos.dao.factory.UserPersist;
import mobi.nordpos.dao.model.Location;
import mobi.nordpos.dao.model.Product;
import mobi.nordpos.dao.model.StockCurrent;
import mobi.nordpos.dao.model.StockDiary.MovementReasonType;
import mobi.nordpos.dao.model.TicketLine;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class OrderPaymentActionBean extends OrderBaseActionBean {

    private static final String PAYMENT_VIEW = "/WEB-INF/jsp/order_payment.jsp";

    private static final String DEFAULT_USER_ID = "3";
    private static final String DEFAULT_LOCATION_ID = "0";

    @Validate(on = {"post"}, required = true, expression = "${(paymentType == 'cash' && paymentAmount >= total) || (paymentType == 'magcard' && paymentAmount == total) }")
    private BigDecimal paymentAmount;
    @Validate(on = {"post"}, required = true, expression = "${paymentType == 'cash' || paymentType == 'magcard'}")
    private String paymentType;

    private ClosedCash openCash;

    public Resolution view() {
        return new ForwardResolution(PAYMENT_VIEW);
    }

    public Resolution post() {

        TaxLinePersist taxLinePersist = new TaxLinePersist();
        PaymentPersist paymentPersist = new PaymentPersist();
        SharedTicket order = getContext().getOrder();

        try {
            sharedTicketPersist.init(getDataBaseConnection());
            taxLinePersist.init(getDataBaseConnection());
            paymentPersist.init(getDataBaseConnection());

            Receipt receipt = getPostedReceipt();
            taxLinePersist.addTaxLineList(order, receipt);

            Payment payment = new Payment();
            payment.setReceipt(receipt);
            payment.setType(paymentType);
            payment.setAmount(paymentAmount);
            paymentPersist.add(payment);

            if (paymentType.equals(PaymentType.valueOf("CASH").getKey())) {
                BigDecimal changeAmount = paymentAmount.subtract(order.getTotalValue());
                if (changeAmount.doubleValue() > 0.0) {
                    payment = new Payment();
                    payment.setReceipt(receipt);
                    payment.setType(PaymentType.valueOf("CHANGE").getKey());
                    payment.setAmount(changeAmount);
                    paymentPersist.add(payment);
                }
            }

            Ticket ticket = getPostedTicket(order, receipt);

            sharedTicketPersist.delete(order.getId());
            getContext().setOrder(null);
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
            return getContext().getSourcePageResolution();
        }
        return new ForwardResolution(WelcomeActionBean.class);
    }

    private Receipt getPostedReceipt() throws SQLException {
        ReceiptPersist receiptPersist = new ReceiptPersist();
        receiptPersist.init(getDataBaseConnection());
        Receipt receipt = new Receipt();
        receipt.setClosedCash(openCash);
        receipt.setDate(new Date());
        return receiptPersist.add(receipt);
    }

    private Ticket getPostedTicket(SharedTicket order, Receipt receipt) throws SQLException {
        TicketPersist ticketPersist = new TicketPersist();
        TicketLinePersist ticketLinePersist = new TicketLinePersist();
        StockDiaryPersist stockDiaryPersist = new StockDiaryPersist();

        ticketPersist.init(getDataBaseConnection());
        ticketLinePersist.init(getDataBaseConnection());
        stockDiaryPersist.init(getDataBaseConnection());

        Ticket ticket = new Ticket();
        ticket.setId(receipt.getId());
        ticket.setType(TicketType.SELL.getCode());
        ticket.setCustomer(getContext().getCustomer());
        ticket.setUser(getAssignUser(DEFAULT_USER_ID));

        TicketNumber number = getCurrentTicketNumber();
        if (number != null) {
            ticket.setNumber(number.getId());
        }

        ticket = ticketPersist.add(ticket);
        ticketLinePersist.addTicketLineList(order, ticket);   
        
        ticket = ticketPersist.read(ticket.getId());        
        Location location = new Location();
        location.setId(DEFAULT_LOCATION_ID);
        List<TicketLine> ticketLineList = ticketPersist.readTicketLineList(ticket);
        stockDiaryPersist.addStockDiaryList(MovementReasonType.OUT_SALE.getValue(), location, receipt, ticketLineList);
        updateStockCurrentSale(location, ticketLineList);

        return ticket;
    }

    private void updateStockCurrentSale(Location location, List<TicketLine> ticketLineList) throws SQLException {
        StockCurrentPersist stockCurrentPersist = new StockCurrentPersist();
        stockCurrentPersist.init(getDataBaseConnection());
        for (TicketLine line : ticketLineList) {
            Product product = line.getProduct();
            StockCurrent stock = stockCurrentPersist.read(location, product);
            if (stock == null) {
                stock = new StockCurrent();
                stock.setLocation(location);
                stock.setProduct(product);
                stock.setUnit(line.getUnit().negate());
                stockCurrentPersist.add(stock);
            } else {
                stock.setUnit(stock.getUnit().subtract(line.getUnit()));
                stockCurrentPersist.change(stock);
            }
        }
    }

    private User getAssignUser(String id) throws SQLException {
        UserPersist userPersist = new UserPersist();
        userPersist.init(getDataBaseConnection());
        return userPersist.find(User.ID, id);
    }

    private TicketNumber getCurrentTicketNumber() throws SQLException {
        TicketNumberPersist ticketNumberPersist = new TicketNumberPersist();
        ticketNumberPersist.init(getDataBaseConnection());
        TicketNumber number = ticketNumberPersist.readList().get(0);
        if (getDataBaseConnection().getDatabaseType().getDatabaseName().equals("Derby Client/Server")) {
            if (ticketNumberPersist.delete(number)) {
                return ticketNumberPersist.add(new TicketNumber());
            } else {
                return null;
            }
        } else {
            if (ticketNumberPersist.change(number)) {
                return ticketNumberPersist.readList().get(0);
            } else {
                return null;
            }
        }
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public BigDecimal getTotal() {
        return getContext().getOrder().getTotalValue();
    }

    @ValidationMethod(on = "post")
    public void validateClosedCashIsOpen(ValidationErrors errors) throws UnknownHostException {
        String hostName = java.net.InetAddress.getLocalHost().getHostName();
        ClosedCashPersist closedCashPersist = new ClosedCashPersist();
        try {
            closedCashPersist.init(getDataBaseConnection());
            openCash = closedCashPersist.readOpen(hostName);
            if (openCash == null) {
                openCash = new ClosedCash();
                openCash.setHost(hostName);
                openCash.setHostSequence(1);
                openCash.setDateStart(new Date());
                openCash = closedCashPersist.add(openCash);
            }
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
        }

    }

}
