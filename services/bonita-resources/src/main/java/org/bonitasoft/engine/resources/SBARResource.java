/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.resources;

import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

/**
 * @author Baptiste Mesta
 */
@Data
@ToString(callSuper = true, exclude = { "content" })
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bar_resource")
public class SBARResource extends AbstractSBARResource {

    @Type(type = "materialized_blob")
    private byte[] content;

    public SBARResource(String name, BARResourceType type, long processDefinitionId, byte[] content) {
        this.name = name;
        this.type = type;
        this.processDefinitionId = processDefinitionId;
        this.content = content;
    }

}
