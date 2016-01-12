/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import com.bonitasoft.engine.monitoring.MonitoringException;

/**
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public interface MonitoringAPI {

    /**
     * Get the number of all active transactions
     * If no active transactions there, return 0
     * 
     * @return the total number of active transaction
     * @throws MonitoringException
     *         occurs when an exception is thrown during monitoring
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfActiveTransactions() throws MonitoringException;

    /**
     * Get the number of users in the organization of the current tenant.
     * If there are no users return 0
     * 
     * @return The number of users
     * @throws MonitoringException
     *         occurs when an exception is thrown during monitoring
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    long getNumberOfUsers() throws MonitoringException;

}