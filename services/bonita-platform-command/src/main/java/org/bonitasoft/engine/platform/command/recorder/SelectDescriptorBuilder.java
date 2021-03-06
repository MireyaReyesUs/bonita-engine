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
package org.bonitasoft.engine.platform.command.recorder;

import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.platform.command.model.SPlatformCommand;

/**
 * @author Bole Zhang
 */
public class SelectDescriptorBuilder {

    public static SelectOneDescriptor<SPlatformCommand> getPlatformCommandByName(final String platformCommandName) {
        final Map<String, Object> parameters = Collections.singletonMap("name", (Object) platformCommandName);
        return new SelectOneDescriptor<SPlatformCommand>("getPlatformCommandByName", parameters,
                SPlatformCommand.class);
    }

}
