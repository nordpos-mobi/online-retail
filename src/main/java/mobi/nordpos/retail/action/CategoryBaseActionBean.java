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

import mobi.nordpos.dao.model.ProductCategory;
import mobi.nordpos.dao.ormlite.ProductCategoryPersist;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
public abstract class CategoryBaseActionBean extends BaseActionBean {

    private ProductCategory category;

    public final ProductCategoryPersist pcPersist;

    public CategoryBaseActionBean() {
        pcPersist = new ProductCategoryPersist();
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

}
