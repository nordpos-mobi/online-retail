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

import java.sql.SQLException;
import javax.servlet.http.HttpServletResponse;
import mobi.nordpos.retail.ext.Public;
import mobi.nordpos.dao.model.ProductCategory;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

/**
 * @author Andrey Svininykh <svininykh@gmail.com>
 */
@Public
public class CategoryImageActionBean extends CategoryBaseActionBean {

    private int thumbnailSize = 256;

    public StreamingResolution preview() {
        return new StreamingResolution("image/jpeg") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                if (getCategory().getImage() != null) {
                    response.getOutputStream().write(getCategory().getImageThumbnail(thumbnailSize));
                    response.flushBuffer();
                }
            }
        }.setFilename("category-".concat(getCategory().getCode() != null ? getCategory().getCode() : "0000").concat(".jpeg"));
    }

    @ValidationMethod(on = "preview")
    public void validateCategoryIdIsAvalaible(ValidationErrors errors) {
        try {
            pcPersist.init(getDataBaseConnection());
            ProductCategory category = pcPersist.read(getCategory().getId());
            if (category != null) {
                setCategory(category);
            }
        } catch (SQLException ex) {
            getContext().getValidationErrors().addGlobalError(
                    new SimpleError(ex.getMessage()));
        }
    }

    public int getThumbnailSize() {
        return thumbnailSize;
    }

    public void setThumbnailSize(int thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

}
