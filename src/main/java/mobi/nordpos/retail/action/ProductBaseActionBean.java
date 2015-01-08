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
import mobi.nordpos.dao.ormlite.ProductPersist;
import mobi.nordpos.dao.ormlite.TaxPersist;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public abstract class ProductBaseActionBean extends BaseActionBean {

    private Product product;

    public final ProductPersist productPersist;
    public final TaxPersist taxPersist;

    public ProductBaseActionBean() {
        productPersist = new ProductPersist();
        taxPersist = new TaxPersist();
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
