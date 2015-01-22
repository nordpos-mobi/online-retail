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

import mobi.nordpos.dao.factory.TicketPersist;
import mobi.nordpos.dao.model.Ticket;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public abstract class TicketBaseActionBean extends BaseActionBean {

    public final TicketPersist ticketPersist;
    private Ticket ticket;

    public TicketBaseActionBean() {
        ticketPersist = new TicketPersist();
    }

    public Ticket getTicket() {
        return ticket;
    }

    @ValidateNestedProperties({
        @Validate(field = "id",
                required = true,
                trim = true)
    })
    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

}
