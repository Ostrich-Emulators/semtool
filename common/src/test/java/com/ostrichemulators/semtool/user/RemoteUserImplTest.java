/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.semtool.user;

import com.ostrichemulators.semtool.user.RemoteUserImpl;
import com.ostrichemulators.semtool.user.User.UserProperty;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ryan
 */
public class RemoteUserImplTest {

	public RemoteUserImplTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testAuthorInfo1() {
		RemoteUserImpl rui = new RemoteUserImpl( "testuser" );
		rui.setProperty( UserProperty.USER_ORG, "org");
		rui.setProperty( UserProperty.USER_FULLNAME, "test name");
		assertEquals( "test name, org", rui.getAuthorInfo() );
	}

	@Test
	public void testAuthorInfo2() {
		RemoteUserImpl rui = new RemoteUserImpl( "testuser" );
		rui.setProperty( UserProperty.USER_ORG, "org");
		rui.setProperty( UserProperty.USER_EMAIL, "test@name.com");
		assertEquals( "<test@name.com> org", rui.getAuthorInfo() );
	}

	@Test
	public void testAuthorInfo3() {
		RemoteUserImpl rui = new RemoteUserImpl( "testuser" );
		rui.setProperty( UserProperty.USER_FULLNAME, "test name");
		rui.setProperty( UserProperty.USER_ORG, "org");
		rui.setProperty( UserProperty.USER_EMAIL, "test@name.com");
		assertEquals( "test name <test@name.com>, org", rui.getAuthorInfo() );
	}

	@Test
	public void testAuthorInfo4() {
		RemoteUserImpl rui = new RemoteUserImpl( "testuser" );
		assertEquals( "testuser", rui.getAuthorInfo() );
	}

	@Test
	public void testAuthorInfo5() {
		RemoteUserImpl rui = new RemoteUserImpl( "testuser" );
		rui.setProperty( UserProperty.USER_ORG, "org");
		assertEquals( "org", rui.getAuthorInfo() );
	}
}
