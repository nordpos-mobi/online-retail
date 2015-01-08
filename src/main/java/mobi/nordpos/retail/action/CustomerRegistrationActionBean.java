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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import mobi.nordpos.dao.model.Customer;
import mobi.nordpos.retail.ext.Public;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
@Public
public class CustomerRegistrationActionBean extends CustomerBaseActionBean {

    private static final String REG_FORM = "/WEB-INF/jsp/customer_reg.jsp";

    @Validate(on = {"accept"},
            required = true,
            minlength = 5,
            maxlength = 20)
    private String confirmPassword;

    @DefaultHandler
    public Resolution form() {
        return new ForwardResolution(REG_FORM);
    }

    public Resolution accept() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        try {
            customerPersist.init(getDataBaseConnection());
            Customer customer = getCustomer();
            customer.setSearchkey(customer.getName());
            getContext().getMessages().add(
                    new SimpleMessage(getLocalizationKey("message.Customer.registered"),
                            customerPersist.add(customer).getName())
            );
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
            return getContext().getSourcePageResolution();
        }
        return new ForwardResolution(CustomerAuthorizationActionBean.class);
    }

    @ValidateNestedProperties({
        @Validate(on = {"accept"},
                field = "name",
                required = true,
                minlength = 5,
                maxlength = 20),
        @Validate(on = {"accept"},
                field = "password",
                required = true,
                minlength = 5,
                maxlength = 20)})
    @Override
    public void setCustomer(Customer customer) {
        super.setCustomer(customer);
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @ValidationMethod(on = {"accept"})
    public void validateUserNameIsAvalaible(ValidationErrors errors) {
        try {
            customerPersist.init(getDataBaseConnection());
            Customer customer = customerPersist.find(Customer.NAME, getCustomer().getName());
            if (customer != null) {
                errors.add("customer.name", new SimpleError(
                        getLocalizationKey("error.Customer.AlreadyExists"), customer.getName()));
            }
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
        }
    }

    @ValidationMethod(on = {"accept"})
    public void validateConfirmPassword(ValidationErrors errors) throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        if (!getCustomer().isAuthentication(confirmPassword)) {
            errors.add("confirmPassword", new SimpleError(
                    getLocalizationKey("error.Customer.incorrectConfirmPassword")));
        }
    }

}
