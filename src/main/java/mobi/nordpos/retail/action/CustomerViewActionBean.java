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
import mobi.nordpos.dao.model.Customer;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.validation.SimpleError;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class CustomerViewActionBean extends CustomerBaseActionBean {

    private static final String CUSTOMER_VIEW = "/WEB-INF/jsp/customer_view.jsp";

    @DefaultHandler
    public Resolution form() {
        return new ForwardResolution(CUSTOMER_VIEW);
    }

    public Resolution update() {
        Customer customer = getContext().getCustomer();
        try {
            customerPersist.init(getDataBaseConnection());
            if (customerPersist.change(customer)) {
                getContext().getMessages().add(
                        new SimpleMessage(getLocalizationKey("message.Customer.updated"),
                                customer.getName()));
            }
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
            return getContext().getSourcePageResolution();
        }
        return new ForwardResolution(WelcomeActionBean.class);
    }

}
