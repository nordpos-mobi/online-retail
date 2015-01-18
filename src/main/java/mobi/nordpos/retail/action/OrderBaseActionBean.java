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

import mobi.nordpos.dao.model.Product;
import mobi.nordpos.dao.factory.ProductPersist;
import mobi.nordpos.dao.factory.SharedTicketPersist;
import mobi.nordpos.dao.model.Payment;
import net.sourceforge.stripes.validation.Validate;
import net.sourceforge.stripes.validation.ValidateNestedProperties;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public abstract class OrderBaseActionBean extends BaseActionBean {

    public final ProductPersist productPersist;
    public final SharedTicketPersist sharedTicketPersist;
    private Product product;
    private Payment payment;

    public OrderBaseActionBean() {
        productPersist = new ProductPersist();
        sharedTicketPersist = new SharedTicketPersist();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Payment getPayment() {
        return payment;
    }

    @ValidateNestedProperties({
        @Validate(on = {"accept", "post"},
                field = "type",
                required = true,
                trim = true,
                expression = "${type == 'cash' || type == 'magcard'}"),
        @Validate(on = {"accept", "post"},
                field = "amount",
                required = true,
                trim = true,
                expression = "${(type == 'cash' && amount >= context.order.totalValue) || (type == 'magcard' && amount == context.order.totalValue)}")})
    public void setPayment(Payment payment) {
        this.payment = payment;
    }

}
