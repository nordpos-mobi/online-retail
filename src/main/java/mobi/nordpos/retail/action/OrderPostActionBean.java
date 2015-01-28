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
import mobi.nordpos.dao.model.Location;
import mobi.nordpos.dao.model.Product;
import mobi.nordpos.dao.model.StockCurrent;
import mobi.nordpos.dao.model.StockDiary.MovementReasonType;
import mobi.nordpos.dao.model.TicketLine;
import net.authorize.DeviceType;
import net.authorize.Environment;
import net.authorize.Merchant;
import net.authorize.TransactionType;
import net.authorize.aim.Result;
import net.authorize.aim.Transaction;
import net.authorize.data.Customer;
import net.authorize.data.Order;
import net.authorize.data.creditcard.CreditCard;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class OrderPostActionBean extends OrderBaseActionBean {

    private ClosedCash openCash;
    private Result<Transaction> result;

    private String cardNumber;
    private String cardExpMonth;
    private String cardExpYear;

    private static final String PAYCARD_VIEW = "/WEB-INF/jsp/paycard_view.jsp";
    private static final String API_LOGIN_ID = "authorize.net.api.login.id";
    private static final String TRANSACTION_KEY = "authorize.net.transaction.key";

    public Resolution card() {
        return new ForwardResolution(PAYCARD_VIEW);
    }

    public Resolution accept() {

        TaxLinePersist taxLinePersist = new TaxLinePersist();
        PaymentPersist paymentPersist = new PaymentPersist();
        SharedTicket order = getContext().getOrder();

        Ticket ticket;
        Payment payment = getPayment();

        if (payment.getType().equals(PaymentType.valueOf("CARD").getKey())) {
            if (result.isApproved()) {
//            out.println("Approved!</br>");
//            out.println("Transaction Id: " + result.getTarget().getTransactionId());
                payment.setTransactionId(result.getTarget().getTransactionId());
            } else if (result.isDeclined()) {
//            out.println("Declined.</br>");
//            out.println(result.getReasonResponseCode() + " : " + result.getResponseText());
                return getContext().getSourcePageResolution();
            } else {
//            out.println("Error.</br>");
//            out.println(result.getReasonResponseCode() + " : " + result.getResponseText());
                return getContext().getSourcePageResolution();
            }
        }

        try {
            sharedTicketPersist.init(getDataBaseConnection());
            taxLinePersist.init(getDataBaseConnection());
            paymentPersist.init(getDataBaseConnection());

            Receipt receipt = getPostedReceipt();
            taxLinePersist.addTaxLineList(order, receipt);

            ticket = getPostedTicket(order, receipt);

            payment.setReceipt(receipt);

            if (payment.getType().equals(PaymentType.valueOf("CASH").getKey())) {
                BigDecimal changeAmount = payment.getAmount().subtract(order.getTotalValue());
                if (changeAmount.doubleValue() > 0.0) {
                    getContext().getMessages().add(
                            new SimpleMessage(getLocalizationKey("message.Ticket.paid.cash"), order.getTotalValue().toString(), ticket.getNumber(), changeAmount.toString())
                    );
                } else {
                    getContext().getMessages().add(
                            new SimpleMessage(getLocalizationKey("message.Ticket.paid.other"), order.getTotalValue().toString(), ticket.getNumber().toString())
                    );
                }
            } else {
                getContext().getMessages().add(
                        new SimpleMessage(getLocalizationKey("message.Thanks"))
                );
            }

            payment.setAmount(order.getTotalValue());
            paymentPersist.add(payment);

            sharedTicketPersist.delete(order.getId());
            getContext().setOrder(null);

        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
            return getContext().getSourcePageResolution();
        }
        return new ForwardResolution(TicketViewActionBean.class).addParameter("ticket.id", ticket.getId());
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
        ticket.setId(receipt.getId().toString());
        ticket.setType(TicketType.SELL.getCode());
        ticket.setCustomer(getContext().getCustomer());
        User user = getUser();
        ticket.setUser(user);

        TicketNumber number = getCurrentTicketNumber();
        if (number != null) {
            ticket.setNumber(number.getId());
        }

        ticket = ticketPersist.add(ticket);
        ticketLinePersist.addTicketLineList(order, ticket);

        ticket = ticketPersist.read(ticket.getId());
        Location location = getLocation();
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

    private TicketNumber getCurrentTicketNumber() throws SQLException {
        TicketNumberPersist ticketNumberPersist = new TicketNumberPersist();
        ticketNumberPersist.init(getDataBaseConnection());
        TicketNumber number = ticketNumberPersist.readList().get(0);
        if (getDataBaseConnection().getDatabaseType().getDatabaseName().equals("Derby Client/Server")) {
            if (ticketNumberPersist.delete(number.getId())) {
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

    public Result<Transaction> getResult() {
        return result;
    }

    public void setResult(Result<Transaction> result) {
        this.result = result;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardExpMonth() {
        return cardExpMonth;
    }

    public void setCardExpMonth(String cardExpMonth) {
        this.cardExpMonth = cardExpMonth;
    }

    public String getCardExpYear() {
        return cardExpYear;
    }

    public void setCardExpYear(String cardExpYear) {
        this.cardExpYear = cardExpYear;
    }

    @ValidationMethod(on = "accept")
    public void validateClosedCashIsOpen(ValidationErrors errors) throws UnknownHostException {
        String hostName = getHostName();
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

    @ValidationMethod(on = "accept")
    public void validateAuthorizeNet(ValidationErrors errors) {
        if (getPayment().getType().equals(PaymentType.valueOf("CARD").getKey())) {
            String apiLoginID = getContext().getServletContext().getInitParameter(API_LOGIN_ID);
            String transactionKey = getContext().getServletContext().getInitParameter(TRANSACTION_KEY);

            Merchant merchant = Merchant.createMerchant(Environment.SANDBOX, apiLoginID,
                    transactionKey);
            merchant.setDeviceType(DeviceType.WEBSITE);

            // Create credit card
            CreditCard creditCard = CreditCard.createCreditCard();
            creditCard.setCreditCardNumber(getCardNumber());
            creditCard.setExpirationMonth(getCardExpMonth());
            creditCard.setExpirationYear(getCardExpYear());

            // Create transaction
            Transaction authCaptureTransaction = merchant.createAIMTransaction(
                    TransactionType.AUTH_CAPTURE, getPayment().getAmount());
            authCaptureTransaction.setCreditCard(creditCard);

            Customer customer = Customer.createCustomer();
            customer.setCustomerId(getContext().getCustomer().getId().toString());
            authCaptureTransaction.setCustomer(customer);
            Order order = Order.createOrder();
            order.setTotalAmount(getContext().getOrder().getTotalValue());
            authCaptureTransaction.setOrder(order);
            result = (Result<Transaction>) merchant.postTransaction(
                    authCaptureTransaction);
        }
    }

    @Override
    public void setPayment(Payment payment) {
        super.setPayment(payment);
    }

}
