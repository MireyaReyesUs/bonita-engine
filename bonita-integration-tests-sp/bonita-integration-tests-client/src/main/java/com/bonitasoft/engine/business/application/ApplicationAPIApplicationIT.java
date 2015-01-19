/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIApplicationIT extends TestWithApplication {

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "create" })
    @Test
    public void createApplication_returns_application_based_on_ApplicationCreator_information() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ProfileForApp", "Profile used by the application");
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");
        creator.setDescription("This is my application");
        creator.setIconPath("/icon.jpg");
        creator.setProfileId(profile.getId());

        //when
        final Application application = getApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo("My-Application");
        assertThat(application.getDisplayName()).isEqualTo("My application display name");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getId()).isGreaterThan(0);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getCreatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getUpdatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getHomePageId()).isNull();
        assertThat(application.getProfileId()).isEqualTo(profile.getId());

        getApplicationAPI().deleteApplication(application.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "create", "no profile" })
    @Test
    public void createApplication_without_profile_should_have_null_profileId() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");

        //when
        final Application application = getApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getProfileId()).isNull();
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "update" })
    @Test
    public void updateApplication_should_return_application_up_to_date() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ProfileForApp", "Profile used by the application");
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");
        final Application application = getApplicationAPI().createApplication(creator);

        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("My-updated-app");
        updater.setDisplayName("Updated display name");
        updater.setVersion("1.1");
        updater.setDescription("Up description");
        updater.setIconPath("/newIcon.jpg");
        updater.setProfileId(profile.getId());
        updater.setState(ApplicationState.ACTIVATED.name());

        //when
        final Application updatedApplication = getApplicationAPI().updateApplication(application.getId(), updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getToken()).isEqualTo("My-updated-app");
        assertThat(updatedApplication.getDisplayName()).isEqualTo("Updated display name");
        assertThat(updatedApplication.getVersion()).isEqualTo("1.1");
        assertThat(updatedApplication.getDescription()).isEqualTo("Up description");
        assertThat(updatedApplication.getIconPath()).isEqualTo("/newIcon.jpg");
        assertThat(updatedApplication.getProfileId()).isEqualTo(profile.getId());
        assertThat(updatedApplication.getState()).isEqualTo(ApplicationState.ACTIVATED.name());
        assertThat(updatedApplication).isEqualTo(getApplicationAPI().getApplication(application.getId()));

        getApplicationAPI().deleteApplication(application.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void getApplication_returns_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");
        final Application createdApp = getApplicationAPI().createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        final Application retrievedApp = getApplicationAPI().getApplication(createdApp.getId());

        //then
        assertThat(retrievedApp).isEqualTo(createdApp);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void deleteApplication_should_delete_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");
        final Application createdApp = getApplicationAPI().createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        getApplicationAPI().deleteApplication(createdApp.getId());

        //then
        try {
            getApplicationAPI().getApplication(createdApp.getId());
            fail("Not found exception");
        } catch (final NotFoundException e) {
            //ok
        }
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "no filter",
            "no search term" })
    @Test
    public void searchApplications_without_filter_return_all_elements_based_on_pagination() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0");

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        final Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchResult<Application> firstPage = getApplicationAPI().searchApplications(buildSearchOptions(0, 2));

        //then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getCount()).isEqualTo(3);
        assertThat(firstPage.getResult()).containsExactly(engineering, hr);

        //when
        final SearchResult<Application> secondPage = getApplicationAPI().searchApplications(buildSearchOptions(2, 2));

        //then
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getCount()).isEqualTo(3);
        assertThat(secondPage.getResult()).containsExactly(marketing);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on name",
            "no search term" })
    @Test
    public void searchApplications_can_filter_on_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0");

        getApplicationAPI().createApplication(hrCreator);
        final Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.TOKEN, "Engineering-dashboard");

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search",
            "filter on display name",
            "no search term" })
    @Test
    public void searchApplications_can_filter_on_display_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0");

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        getApplicationAPI().createApplication(engineeringCreator);
        getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.DISPLAY_NAME, "HR dashboard");

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(hr);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on version",
            "no search term" })
    @Test
    public void searchApplications_can_filter_on_version() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "2.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "2.0");

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.VERSION, "2.0");

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search",
            "filter on profileId", "no search term" })
    @Test
    public void searchApplications_can_filter_on_profileId() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("engineering", "Engineering");
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        engineeringCreator.setProfileId(profile.getId());
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0");

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        final Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.PROFILE_ID, profile.getId());

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);

        getApplicationAPI().deleteApplication(hr.getId());
        getApplicationAPI().deleteApplication(engineering.getId());
        getApplicationAPI().deleteApplication(marketing.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "no filter",
            "search term" })
    @Test
    public void searchApplications_can_use_search_term() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "My HR dashboard", "2.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0");

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.searchTerm("My");

        final SearchResult<Application> applications = getApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.TOKEN, Order.ASC);
        return builder;
    }

}