/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.internal.client;

import static com.openshift.client.utils.Cartridges.MYSQL_51_NAME;
import static com.openshift.client.utils.Cartridges.FOREMAN_DOWNLOAD_URL;
import static com.openshift.client.utils.Samples.GET_DOMAINS;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_2EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_0ALIAS;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_1EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_2ALIAS;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_2EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_1EMBEDDED;
import static com.openshift.client.utils.Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_2EMBEDDED;
import static com.openshift.client.utils.Samples.POST_MYSQL_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES;
import static com.openshift.client.utils.Samples.POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT;
import static com.openshift.client.utils.Samples.POST_ADD_ENVIRONMENT_VARIABLE_FOO_TO_FOOBARZ_SPRINGEAP6;
import static com.openshift.client.utils.Samples.GET_0_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6;
import static com.openshift.client.utils.Samples.GET_1_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6;
import static com.openshift.client.utils.Samples.GET_2_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6;
import static com.openshift.client.utils.Samples.GET_4_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6;
import static com.openshift.client.utils.Samples.POST_ADD_2_ENVIRONMENT_VARIABLES_TO_FOOBARZ_SPRINGEAP6;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.openshift.client.IApplication;
import com.openshift.client.IApplicationPortForwarding;
import com.openshift.client.IDomain;
import com.openshift.client.IEnvironmentVariable;
import com.openshift.client.IField;
import com.openshift.client.OpenShiftEndpointException;
import com.openshift.client.OpenShiftException;
import com.openshift.client.OpenShiftSSHOperationException;
import com.openshift.client.OpenShiftTimeoutException;
import com.openshift.client.cartridge.EmbeddableCartridge;
import com.openshift.client.cartridge.IEmbeddableCartridge;
import com.openshift.client.cartridge.IEmbeddedCartridge;
import com.openshift.client.utils.CartridgeAssert;
import com.openshift.client.utils.Cartridges;
import com.openshift.client.utils.EmbeddedCartridgeAssert;
import com.openshift.client.utils.MessageAssert;
import com.openshift.client.utils.Samples;
import com.openshift.internal.client.httpclient.HttpClientException;
import com.openshift.internal.client.httpclient.InternalServerErrorException;

/**
 * @author Xavier Coulon
 * @author Andre Dietisheim
 * @author Nicolas Spano
 */
public class ApplicationResourceTest {

	private IDomain domain;
	private HttpClientMockDirector mockDirector;

	@Before
	public void setup() throws Throwable {
		this.mockDirector = new HttpClientMockDirector()
				.mockGetDomains(GET_DOMAINS)
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_1EMBEDDED)
				.mockGetApplication("foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_1EMBEDDED)
				.mockGetApplicationCartridges("foobarz", "springeap6",
						GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_1EMBEDDED);
		this.domain = mockDirector.getDomain("foobarz");
		assertThat(domain).isNotNull();
	}

	@Test
	public void shouldDestroyApplication() throws Throwable {
		// pre-conditions
		assertThat(domain).isNotNull();
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app).isNotNull();

		// operation
		app.destroy();

		// verifications
		assertThat(domain.getApplications()).hasSize(1).excludes(app);
	}

	@Test
	public void shouldStopApplication() throws Throwable {
		// pre-conditions
		mockDirector.mockPostApplicationEvent(
				"foobarz", "springeap6", POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT);
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		app.stop();

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldForceStopApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"honkabonka2", "springeap6", POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT);
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		app.stop(true);

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldStartApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"honkabonka2", "springeap6", POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT);
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		app.start();

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldRestartApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"honkabonka2", "springeap6", POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT);
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		app.restart();

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Ignore("Need higher quotas on stg")
	@Test
	public void shouldScaleDownApplication() throws Throwable {
	}

	@Test
	public void shouldNotScaleDownApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"foobarz",
						"springeap6",
						new InternalServerErrorException(
								"Failed to add event scale-down to application springeap6 due to: Cannot scale a non-scalable application"));
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		try {
			app.scaleDown();
			fail("Expected an exception here..");
		} catch (OpenShiftEndpointException e) {

			// verifications
			assertThat(e.getCause()).isInstanceOf(InternalServerErrorException.class);
		}

		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Ignore("Need higher quotas on stg")
	@Test
	public void shouldScaleUpApplication() throws Throwable {
	}

	@Test
	public void shouldNotScaleUpApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"foobarz", "springeap6", POST_STOP_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_EVENT)
				.mockPostApplicationEvent(
						"foobarz",
						"springeap6",
						new InternalServerErrorException(
								"Failed to add event scale-up to application springeap6 due to: Cannot scale a non-scalable application"));
		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		try {
			app.scaleUp();
			fail("Expected an exception here..");
		} catch (OpenShiftEndpointException e) {

			// verifications
			assertThat(e.getCause()).isInstanceOf(InternalServerErrorException.class);
		}

		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldAddAliasToApplication() throws Throwable {
		// pre-conditions
		mockDirector.mockPostApplicationEvent(
				"foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_2ALIAS);
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");

		// operation
		app.addAlias("redhat.com");

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
		assertThat(app.getAliases()).hasSize(2).contains("jbosstools.org", "redhat.com");
	}

	@Test
	public void shouldNotAddExistingAliasToApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"foobarz",
						"springeap6",
						new InternalServerErrorException(
								"Failed to add event add-alias to application springeap6 due to: Alias 'jbosstools.org' already exists for 'springeap6'"));
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");

		// operation
		try {
			app.addAlias("jbosstools.org");
			fail("Expected an exception..");
		} catch (OpenShiftEndpointException e) {

			// verifications
			assertThat(e.getCause()).isInstanceOf(InternalServerErrorException.class);
		}
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldRemoveAliasFromApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_0ALIAS);
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");

		// operation
		app.removeAlias("jbosstools.org");

		// verifications
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
		assertThat(app.getAliases()).hasSize(0);
	}

	@Test
	public void shouldNotRemoveAliasFromApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockPostApplicationEvent(
						"foobarz",
						"springeap6",
						new InternalServerErrorException(
								"Failed to add event remove-alias to application springeap6 due to: Alias 'openshift-origin.org' does not exist for 'springeap6'"));
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app).isNotNull();
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");
		// operation
		try {
			app.removeAlias("openshift-origin.org");
			fail("Expected an exception..");
		} catch (OpenShiftEndpointException e) {

			// verifications
			assertThat(e.getCause()).isInstanceOf(InternalServerErrorException.class);
		}
		assertThat(app.getAliases()).hasSize(1).contains("jbosstools.org");
		mockDirector.verifyPostApplicationEvent("foobarz", "springeap6");
	}

	@Test
	public void shouldListExistingCartridges() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz",
						GET_DOMAINS_FOOBARZ_APPLICATIONS_2EMBEDDED)
				.mockGetApplicationCartridges("foobarz", "springeap6",
						GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_2EMBEDDED);

		final IApplication app = domain.getApplicationByName("springeap6");

		// operation
		final List<IEmbeddedCartridge> embeddedCartridges = app.getEmbeddedCartridges();

		// verifications
		assertThat(embeddedCartridges).hasSize(2);
	}

	@Test
	public void shouldGetEmbeddableCartridgeByCartridge() throws Throwable {
		// pre-conditions
		// operation
		final IApplication app = domain.getApplicationByName("springeap6");
		IEmbeddedCartridge mongo = app.getEmbeddedCartridge(Cartridges.mongodb22());
		// verifications
		// embedded cartridge should get updated with name, description and
		// display name
		new EmbeddedCartridgeAssert(mongo);
	}

	@Test
	public void shouldGetDownloadableEmbeddableCartridgeByCartridge() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz",
						Samples.GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP_SCALABLE_DOWNLOADABLECART);

		IDomain domain = mockDirector.getDomain("foobarz");
		IApplication application = domain.getApplicationByName("downloadablecart");
		assertThat(application).isNotNull();

		IEmbeddableCartridge foreman = new EmbeddableCartridge(null, new URL(FOREMAN_DOWNLOAD_URL));
		new CartridgeAssert<IEmbeddableCartridge>(foreman)
				.hasUrl(Cartridges.FOREMAN_DOWNLOAD_URL)
				.hasName(null)
				.hasDescription(null)
				.hasDisplayName(null);

		// operation
		IEmbeddedCartridge embeddedForeman = application.getEmbeddedCartridge(foreman);
		// verifications
		// embedded cartridge should get updated with name, description and
		// display name
		new EmbeddedCartridgeAssert(embeddedForeman)
				.hasUrl(Cartridges.FOREMAN_DOWNLOAD_URL);
	}

	@Test
	public void shouldReloadExistingEmbeddedCartridges() throws Throwable {
		// pre-conditions
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getEmbeddedCartridges()).hasSize(1);
		// simulate new content on openshift, that should be grabbed while doing
		// a refresh()
		mockDirector.mockGetApplicationCartridges("foobarz", "springeap6",
				GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_2EMBEDDED);

		// operation
		app.refresh();

		// verify
		// get app resource wont load embedded cartridges, only refresh does
		// (thus should occur 1x)
		mockDirector.verifyListEmbeddableCartridges(1, "foobarz", "springeap6");
		assertThat(app.getEmbeddedCartridges()).hasSize(2);
	}

	@Test
	public void shouldAddCartridgeToApplication() throws Throwable {
		// pre-conditions
		mockDirector.mockAddEmbeddableCartridge("foobarz", "springeap6",
				POST_MYSQL_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES);
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getEmbeddedCartridges()).hasSize(1);

		// operation
		app.addEmbeddableCartridge(Cartridges.mysql51());

		// verifications
		mockDirector.verifyAddEmbeddableCartridge("foobarz", "springeap6");
		assertThat(app.getEmbeddedCartridges()).hasSize(2);
		IEmbeddedCartridge mySqlCartridge = app.getEmbeddedCartridge(MYSQL_51_NAME);
		new EmbeddedCartridgeAssert(mySqlCartridge)
				.hasMessages()
				.hasDescription()
				.hasName(MYSQL_51_NAME);

		new MessageAssert(mySqlCartridge.getMessages().getFirstBy(IField.DEFAULT))
				.hasExitCode(-1)
				.hasText("Added mysql-5.1 to application springeap6");
		new MessageAssert(mySqlCartridge.getMessages().getFirstBy(IField.RESULT))
				.hasExitCode(0)
				.hasText(
						"\nMySQL 5.1 database added.  Please make note of these credentials:\n\n"
								+ "       Root User: adminnFC22YQ\n   Root Password: U1IX8AIlrEcl\n   Database Name: springeap6\n\n"
								+ "Connection URL: mysql://$OPENSHIFT_MYSQL_DB_HOST:$OPENSHIFT_MYSQL_DB_PORT/\n\n"
								+ "You can manage your new MySQL database by also embedding phpmyadmin-3.4.\n"
								+ "The phpmyadmin username and password will be the same as the MySQL credentials above.\n");
		new MessageAssert(mySqlCartridge.getMessages().getFirstBy(IField.APPINFO))
				.hasExitCode(0)
				.hasText("Connection URL: mysql://127.13.125.1:3306/\n");

	}

	@Test
	public void shouldNotAddCartridgeToApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications("foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_2EMBEDDED)
				.mockAddEmbeddableCartridge("foobarz", "springeap6", new SocketTimeoutException("mock..."));
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app.getEmbeddedCartridges()).hasSize(2);

		// operation
		try {
			app.addEmbeddableCartridge(new EmbeddableCartridge(Cartridges.POSTGRESQL_84_NAME));
			fail("Expected an exception here...");
		} catch (OpenShiftTimeoutException e) {
			// ok
		}

		// verifications
		mockDirector.verifyAddEmbeddableCartridge("foobarz", "springeap6");
		assertThat(app.getEmbeddedCartridge(Cartridges.POSTGRESQL_84_NAME)).isNull();
		assertThat(app.getEmbeddedCartridges()).hasSize(2);
	}

	@Test
	public void shouldRemoveCartridgeFromApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications(
						"foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_2EMBEDDED)
				.mockGetApplication(
						"foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_2EMBEDDED)
				.mockGetApplicationCartridges(
						"foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_2EMBEDDED);
		final IApplication application = domain.getApplicationByName("springeap6");
		assertThat(application.getEmbeddedCartridges()).hasSize(2);

		// operation
		application.getEmbeddedCartridge("mysql-5.1").destroy();

		// verifications
		mockDirector.verifyDeleteEmbeddableCartridge("foobarz", "springeap6", "mysql-5.1");
		assertThat(application.getEmbeddedCartridge("mysql-5.1")).isNull();
		assertThat(application.getEmbeddedCartridges()).hasSize(1);
	}

	@Test
	public void shouldNotRemoveCartridgeFromApplication() throws Throwable {
		// pre-conditions
		mockDirector
				.mockGetApplications(
						"foobarz", GET_DOMAINS_FOOBARZ_APPLICATIONS_2EMBEDDED)
				.mockGetApplicationCartridges(
						"foobarz", "springeap6", GET_DOMAINS_FOOBARZ_APPLICATIONS_SPRINGEAP6_CARTRIDGES_2EMBEDDED)
				.mockRemoveEmbeddableCartridge("foobarz", "springeap6", "mysql-5.1",
						new SocketTimeoutException("mock..."));
		final IApplication application = domain.getApplicationByName("springeap6");
		assertThat(application.getEmbeddedCartridges()).hasSize(2);

		// operation
		final IEmbeddedCartridge mysql = application.getEmbeddedCartridge("mysql-5.1");
		try {
			mysql.destroy();
			fail("Expected an exception here..");
		} catch (OpenShiftTimeoutException e) {
			// ok
		}

		// verifications
		mockDirector.verifyDeleteEmbeddableCartridge("foobarz", "springeap6", "mysql-5.1");
		assertThat(application.getEmbeddedCartridges()).hasSize(2).contains(mysql);
	}

	@Test
	public void shouldWaitUntilTimeout() throws HttpClientException, Throwable {
		// pre-conditions
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app).isNotNull().isInstanceOf(ApplicationResource.class);
		ApplicationResource spy = Mockito.spy(((ApplicationResource) app));
		Mockito.doReturn(false).when(spy).canResolv(Mockito.anyString());
		long timeout = 2 * 1000;
		long startTime = System.currentTimeMillis();

		// operation
		boolean successfull = spy.waitForAccessible(timeout);

		// verification
		assertFalse(successfull);
		assertTrue(System.currentTimeMillis() >= (startTime + timeout));
	}

	@Test
	public void shouldEndBeforeTimeout() throws HttpClientException, Throwable {
		// pre-conditions
		long startTime = System.currentTimeMillis();
		long timeout = 10 * 1000;
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app).isNotNull().isInstanceOf(ApplicationResource.class);
		ApplicationResource spy = Mockito.spy(((ApplicationResource) app));
		Mockito.doReturn(true).when(spy).canResolv(Mockito.anyString());

		// operation
		boolean successfull = spy.waitForAccessible(timeout);

		// verification
		assertTrue(successfull);
		assertTrue(System.currentTimeMillis() < (startTime + timeout));
	}

	@Test
	public void shouldGetForwardablePorts() throws Throwable {
		// pre-conditions
		final IApplication app = domain.getApplicationByName("springeap6");
		assertThat(app).isNotNull().isInstanceOf(ApplicationResource.class);
		String[] rhcListPortsOutput = new String[] {
				"haproxy -> 127.7.233.2:8080",
				" haproxy -> 127.7.233.3:8080",
				" java -> 127.7.233.1:3528",
				" java -> 127.7.233.1:4447",
				" java -> 127.7.233.1:5445",
				" java -> 127.7.233.1:5455",
				" java -> 127.7.233.1:8080",
				" java -> 127.7.233.1:9990",
				" java -> 127.7.233.1:9999",
				" mysql -> 5190d701500446506a0000e4-foobarz.rhcloud.com:56756" };
		ApplicationResource spy = Mockito.spy(((ApplicationResource) app));
		Mockito.doReturn(Arrays.asList(rhcListPortsOutput)).when(spy)
				.sshExecCmd(Mockito.anyString(), (ApplicationResource.SshStreams) Mockito.any());

		// operation
		List<IApplicationPortForwarding> forwardablePorts = spy.getForwardablePorts();

		// verification
		assertThat(forwardablePorts).isNotEmpty().hasSize(10);
		assertThat(forwardablePorts)
				.onProperty("name").containsExactly("haproxy", "haproxy", "java", "java", "java", "java", "java",
						"java", "java", "mysql");
		assertThat(forwardablePorts)
				.onProperty("remoteAddress").containsExactly("127.7.233.2", "127.7.233.3", "127.7.233.1",
						"127.7.233.1", "127.7.233.1", "127.7.233.1", "127.7.233.1", "127.7.233.1", "127.7.233.1",
						"5190d701500446506a0000e4-foobarz.rhcloud.com");
		assertThat(forwardablePorts)
				.onProperty("remotePort").containsExactly(8080, 8080, 3528, 4447, 5445, 5455, 8080, 9990, 9999, 56756);
	}
    @Test
	public void shouldAddOneEnvironmentVariableToApplication() throws Throwable{
    	//pre-conditions
    	mockDirector.mockAddEnvironmentVariable("foobarz","springeap6",POST_ADD_ENVIRONMENT_VARIABLE_FOO_TO_FOOBARZ_SPRINGEAP6)
    	            .mockGetEnvironmentVariables("foobarz", "springeap6",GET_0_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6, GET_1_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
    	
    	//operation
    	final IApplication app = domain.getApplicationByName("springeap6");
    	IEnvironmentVariable environmentVariable = app.addEnvironmentVariable("FOO","123");
    	//vaerification
    	assertThat(environmentVariable).isNotNull();
    	assertThat(environmentVariable.getName()).isEqualTo("FOO");
		assertThat(environmentVariable.getValue()).isEqualTo("123");
    	
    	
    }
    
    @Test
	public void shouldAddEnvironmentVariablesToApplication() throws Throwable{
    	//pre-conditions
    	mockDirector.mockAddEnvironmentVariable("foobarz","springeap6",POST_ADD_2_ENVIRONMENT_VARIABLES_TO_FOOBARZ_SPRINGEAP6)
                    .mockGetEnvironmentVariables("foobarz", "springeap6", GET_2_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
    	           
    	//operation
    	final IApplication app = domain.getApplicationByName("springeap6");
    	Map<String,String> environmentVariables = new HashMap<String,String>();
    	environmentVariables.put("X_NAME","X_VALUE");
    	environmentVariables.put("Y_NAME","Y_VALUE");
    	List<IEnvironmentVariable> environmentVariablesList = app.addEnvironmentVariables(environmentVariables);
    	//verification
    	assertThat(environmentVariablesList).hasSize(2);
    }
    
    @Test
 	public void shouldGetEnvironmentVariableByNameFromApplication() throws Throwable{
    	//precondition
		mockDirector.mockGetEnvironmentVariables("foobarz", "springeap6",GET_1_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
		//operation
		final IApplication app = domain.getApplicationByName("springeap6");
		IEnvironmentVariable environmentVariable = app.getEnvironmentVariableByName("FOO");
		//verification
		assertThat(environmentVariable).isNotNull();
		assertThat(environmentVariable.getName()).isEqualTo("FOO");
		assertThat(environmentVariable.getValue()).isEqualTo("123");
		
	}
    @Test
	public void shouldNotAddExistingEnvironmentVariableToApplication() throws Throwable{
		//precondition
		mockDirector.mockGetEnvironmentVariables("foobarz", "springeap6",GET_1_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
		//operation
		final IApplication app = domain.getApplicationByName("springeap6");
		IEnvironmentVariable existingEnvironmentVariable = app.getEnvironmentVariables().get(0);
		assertThat(app.getEnvironmentVariables()).hasSize(1);
		assertThat(existingEnvironmentVariable.getName()).isEqualTo("FOO");
		try{
		app.addEnvironmentVariable("FOO","123");
		fail("Expected an exception here...");
		}catch(OpenShiftException e){
			//expected
		}
		assertThat(app.getEnvironmentVariables()).hasSize(1);
	}

	@Test
	public void shouldListAllEnvironmentVariablesFromApplication() throws Throwable{
	//preconditions
	mockDirector.mockGetEnvironmentVariables("foobarz", "springeap6",GET_4_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
	//operation	
	final IApplication app = domain.getApplicationByName("springeap6");
	List<IEnvironmentVariable> environmentVariables = app.getEnvironmentVariables();
	//verifications
	assertThat(environmentVariables).isNotEmpty();
	assertThat(environmentVariables).hasSize(4);
	
		
	}
	@Test
	public void shouldLoadEmptyListOfEnvironmentVariables() throws Throwable{
		//precondition
		mockDirector.mockGetEnvironmentVariables("foobarz", "springeap6",GET_0_ENVIRONMENT_VARIABLES_FOOBARZ_SPRINGEAP6);
		//operation
		final IApplication application = domain.getApplicationByName("springeap6");
		List<IEnvironmentVariable> environmentVariables = application.getEnvironmentVariables();
		//verifications
		assertThat(environmentVariables).isEmpty();
	}

}
