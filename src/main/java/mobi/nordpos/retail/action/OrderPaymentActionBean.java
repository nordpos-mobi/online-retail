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

import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import mobi.nordpos.dao.model.ClosedCash;
import mobi.nordpos.dao.model.Customer;
import mobi.nordpos.dao.model.Product;
import mobi.nordpos.dao.model.Receipt;
import mobi.nordpos.dao.model.Tax;
import mobi.nordpos.dao.model.Ticket;
import mobi.nordpos.dao.model.Ticket.TicketType;
import mobi.nordpos.dao.model.TicketLine;
import mobi.nordpos.dao.model.TicketNumber;
import mobi.nordpos.dao.model.User;
import mobi.nordpos.dao.ormlite.ClosedCashPersist;
import mobi.nordpos.dao.ormlite.CustomerPersist;
import mobi.nordpos.dao.ormlite.ReceiptPersist;
import mobi.nordpos.dao.ormlite.TicketLinePersist;
import mobi.nordpos.dao.ormlite.TicketNumberPersist;
import mobi.nordpos.dao.ormlite.TicketPersist;
import mobi.nordpos.dao.ormlite.UserPersist;
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

    @Validate(on = {"post"}, required = true, expression = "${(paymentType == 'cash' && paymentAmount >= total) || (paymentType == 'magcard' && paymentAmount == total) }")
    private BigDecimal paymentAmount;
    @Validate(on = {"post"}, required = true, expression = "${paymentType == 'cash' || paymentType == 'magcard'}")
    private String paymentType;

    private ClosedCash openCash;

    public Resolution view() {
        return new ForwardResolution(PAYMENT_VIEW);
    }

    public Resolution post() {
        TicketLinePersist ticketLinePersist = new TicketLinePersist();

        try {
            sharedTicketPersist.init(getDataBaseConnection());
            ticketLinePersist.init(getDataBaseConnection());
            Receipt receipt = getPostedReceipt();
            Ticket ticket = getPostedTicket(receipt);
            
            ticketLinePersist.addTicketLineList(getContext().getOrder(), ticket);
            
            sharedTicketPersist.delete(getContext().getOrder().getId());
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

    private Ticket getPostedTicket(Receipt receipt) throws SQLException {
        TicketPersist ticketPersist = new TicketPersist();
        ticketPersist.init(getDataBaseConnection());
        Ticket ticket = new Ticket();
        ticket.setId(receipt.getId());
        ticket.setType(TicketType.SELL.getTicketType());
        ticket.setCustomer(getContext().getCustomer());
        ticket.setUser(getAssignUser(DEFAULT_USER_ID));

        TicketNumber number = getCurrentTicketNumber();
        if (number != null) {
            ticket.setNumber(number.getId());
        }

        return ticketPersist.add(ticket);
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

//    public Ticket saveTicket(OpenTicket openTicket) throws SQLException {
//        try {
//            connection = new JdbcConnectionSource(getDataBaseURL(), getDataBaseUser(), getDataBasePassword());
//            ReceiptDAO receiptDao = new ReceiptPersist(connection);
//            TicketDAO ticketDao = new TicketPersist(connection);
//            TicketNumberDAO ticketNumberDao = new TicketNumberPersist(connection);
//
//            if (!openTicket.getTicketLineList().isEmpty()) {
//                Receipt receipt = receiptDao.createNew(getOpenCash());
//                Ticket ticket = ticketDao.createNew(new Ticket(
//                        receipt.getId(),
//                        TicketType.SELL.getTicketType(),
//                        ticketNumberDao.generate(),
//                        receipt,
//                        openTicket.getUser(),
//                        openTicket.getFiscalNumber()));
//                ticket.setTicketLineCollection(openTicket.getTicketLineList());
//
//                ForeignCollection<TicketLine> ticketLineCollection = ticket.getTicketLineCollection();
//                CloseableIterator<TicketLine> ticketLineIterator = ticketLineCollection.closeableIterator();
//
//                ForeignCollection<TaxLine> taxLineCollection = receipt.getTaxLineCollection();
//
//                try {
//                    while (ticketLineIterator.hasNext()) {
//                        TicketLine line = ticketLineIterator.next();
//                        Tax selectTax = line.getTax();
//                        CloseableIterator<TaxLine> taxLineIterator = taxLineCollection.closeableIterator();
//                        TaxLine newTaxLine = new TaxLine(
//                                receipt,
//                                selectTax,
//                                line.getSubTotal().doubleValue(),
//                                line.getTaxAmountSubTotal().doubleValue());
//
//                        boolean f = true;
//                        try {
//                            while (taxLineIterator.hasNext()) {
//                                TaxLine taxLine = taxLineIterator.next();
//                                if (taxLine.getTax().getId().equals(selectTax.getId())) {
//                                    taxLine.setBase(taxLine.getBase() + line.getSubTotal().doubleValue());
//                                    taxLine.setAmount(taxLine.getAmount() + line.getTaxAmountSubTotal().doubleValue());
//                                    taxLineCollection.update(taxLine);
//                                    f = false;
//                                    break;
//                                }
//                            }
//                        } finally {
//                            taxLineIterator.close();
//                        }
//
//                        if (f) {
//                            taxLineCollection.add(newTaxLine);
//                        }
//                    }
//                } finally {
//                    ticketLineIterator.close();
//                }
//
//                ForeignCollection<Payment> paymentCollection = receipt.getPaymentCollection();
//                BigDecimal total = openTicket.getTaxTotal();
//                paymentCollection.add(new Payment(receipt, paymentType, payAmount));
//                if (paymentType.equals(PaymentType.valueOf("CASH").getPaymentType()) && payAmount > total.doubleValue()) {
//                    paymentCollection.add(new Payment(receipt, PaymentType.valueOf("CHANGE").getPaymentType(), BigDecimal.valueOf(payAmount).subtract(total).doubleValue()));
//                }
//
//                getContext().setOpenTicket(new OpenTicket(getContext().getUser()));
//
//                return ticket;
//
//            } else {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        } finally {
//            if (connection != null) {
//                connection.close();
//            }
//        }
//    }
}
