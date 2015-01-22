/**
 * Copyright (c) 2012-2014 Nord Trading Network.
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

import java.sql.SQLException;
import java.util.UUID;
import mobi.nordpos.dao.factory.PaymentPersist;
import mobi.nordpos.dao.factory.ReceiptPersist;
import mobi.nordpos.dao.factory.TaxLinePersist;
import mobi.nordpos.dao.factory.TicketLinePersist;
import mobi.nordpos.dao.model.Receipt;
import mobi.nordpos.dao.model.Ticket;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class TicketViewActionBean extends TicketBaseActionBean {

    private static final String TICKET_VIEW = "/WEB-INF/jsp/ticket_view.jsp";

    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(TICKET_VIEW);
    }

    @Override
    public void setTicket(Ticket ticket) {
        super.setTicket(ticket);
    }

    @ValidationMethod
    public void validateTicketIsAvalaible(ValidationErrors errors) {
        TicketLinePersist ticketLinePersist = new TicketLinePersist();
        ReceiptPersist receiptPersist = new ReceiptPersist();
        TaxLinePersist taxLinePersist = new TaxLinePersist();
        PaymentPersist paymentPersist = new PaymentPersist();
        try {
            ticketPersist.init(getDataBaseConnection());
            ticketLinePersist.init(getDataBaseConnection());
            receiptPersist.init(getDataBaseConnection());
            taxLinePersist.init(getDataBaseConnection());
            paymentPersist.init(getDataBaseConnection());

            Ticket ticket = ticketPersist.read(getTicket().getId());
            if (ticket != null) {
                ticket.setTicketLineList(ticketLinePersist.readTicketLineList(ticket));
                Receipt receipt = receiptPersist.read(UUID.fromString(ticket.getId()));
                receipt.setTaxList(taxLinePersist.readTaxLineList(receipt));
                receipt.setPaymentList(paymentPersist.readPaymentList(receipt));
                ticket.setReceipt(receipt);
                setTicket(ticket);
            }
            
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
        }
    }

    @ValidationMethod(priority = 1)
    public void validateAccessToTicket(ValidationErrors errors) {
        Ticket ticket = getTicket();
        ticket.getCustomer().equals(getContext().getCustomer());
        ticket.getUser().equals(getUser());
    }

}
