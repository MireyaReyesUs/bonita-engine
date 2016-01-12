/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.service.platform;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.bonitasoft.manager.Manager;
import org.bonitasoft.engine.platform.PlatformRetriever;
import org.bonitasoft.engine.platform.model.SPlatform;
import org.bonitasoft.engine.platform.model.impl.SPlatformImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformInformationManagerImplTest {

    @Mock
    private PlatformInformationService platformInformationService;

    @Mock
    private PlatformRetriever platformRetriever;

    @Mock
    private PlatformInformationProvider provider;

    @InjectMocks
    private PlatformInformationManagerImpl infoManager;

    private PlatformInformationManagerImpl spyInfoManager;

    @Mock
    Manager manager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {

        spyInfoManager = spy(infoManager);
        given(spyInfoManager.getManager()).willReturn(manager);

    }

    @Test
    public void update_should_read_and_update_information() throws Throwable {
        //given
        String initialInfo = "initialInfo";
        String newInfo = "newInfo";

        SPlatformImpl platform = buildPlatform(initialInfo);

        given(platformRetriever.getPlatform()).willReturn(platform);
        given(manager.calculateNewPlatformInfo(initialInfo,1)).willReturn(newInfo);
        given(provider.getAndReset()).willReturn(1);

        // when
        spyInfoManager.update();

        //then
        verify(platformInformationService).updatePlatformInfo(platform, newInfo);
    }

    private SPlatformImpl buildPlatform(final String initialInfo) {
        SPlatformImpl platform = new SPlatformImpl("7.0.0", "6.5.3", "6.4.0", "admin", System.currentTimeMillis());
        platform.setInformation(initialInfo);
        return platform;
    }

    @Test
    public void update_should_do_nothing_if_info_does_not_change() throws Throwable {
        //given
        given(provider.getAndReset()).willReturn(0);

        // when
        spyInfoManager.update();

        //then
        verify(platformInformationService, never()).updatePlatformInfo(any(SPlatform.class), anyString());
        verify(manager, never()).calculateNewPlatformInfo(anyString(),anyInt());
    }

    @Test
    public void update_should_process_several_calls() throws Throwable {
        //given
        final int numberOfStartedCases = 3;
        String initialInfo = "initialInfo";
        String [] newInfo = {"newInfo1", "newInfo2", "newInfo3"};
        given(provider.getAndReset()).willReturn(numberOfStartedCases);
        SPlatformImpl platform = buildPlatform(initialInfo);

        given(platformRetriever.getPlatform()).willReturn(platform);
        given(manager.calculateNewPlatformInfo(initialInfo, numberOfStartedCases)).willReturn(newInfo[2]);

        // when
        spyInfoManager.update();

        //then
        verify(platformInformationService).updatePlatformInfo(platform, newInfo[2]);
    }

    @Test
    public void update_throws_SPlatformUpdateException_when_calculate_throws_Exception() throws Throwable {
        //given
        String initialInfo = "initialInfo";
        SPlatformImpl platform = buildPlatform(initialInfo);

        given(platformRetriever.getPlatform()).willReturn(platform);
        IllegalStateException illegalStateException = new IllegalStateException("error");
        given(manager.calculateNewPlatformInfo(initialInfo,1)).willThrow(illegalStateException);
        given(provider.getAndReset()).willReturn(1);

        //then
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("error");

        // when
        spyInfoManager.update();

    }

}