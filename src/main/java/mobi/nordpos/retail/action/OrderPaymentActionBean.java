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

import mobi.nordpos.dao.model.Payment;
import mobi.nordpos.dao.model.Payment.PaymentType;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public class OrderPaymentActionBean extends OrderBaseActionBean {

    private static final String PAYMENT_VIEW = "/WEB-INF/jsp/order_payment.jsp";

    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(PAYMENT_VIEW);
    }

    public Resolution post() {
        if (getPayment().getType().equals(PaymentType.valueOf("CASH").getKey())) {
            return new ForwardResolution(OrderPostActionBean.class, "accept").addParameter("payment.type", getPayment().getType()).addParameter("payment.amount", getPayment().getAmount());
        } else if (getPayment().getType().equals(PaymentType.valueOf("CARD").getKey())) {
            return new ForwardResolution(OrderPostActionBean.class, "card").addParameter("payment.type", getPayment().getType()).addParameter("payment.amount", getPayment().getAmount());
        } else {
            return getContext().getSourcePageResolution();
        }
    }

    @Override
    public void setPayment(Payment payment) {
        super.setPayment(payment);
    }
}
