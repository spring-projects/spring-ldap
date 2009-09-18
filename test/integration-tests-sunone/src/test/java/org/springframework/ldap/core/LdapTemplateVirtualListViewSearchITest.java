/*
 * Copyright 2005-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ldap.core;

import static junit.framework.Assert.assertEquals;

import java.util.List;

import javax.naming.directory.SearchControls;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.itest.Person;
import org.springframework.ldap.itest.PersonContextMapper;
import org.springframework.ldap.control.VirtualListViewControlDirContextProcessor;
import org.springframework.ldap.control.VirtualListViewResultsCookie;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Integration tests for the virtual list view search result capability of
 * LdapTemplate. The test should reflect the example in Chapter 7 of the <a
 * href=
 * "http://www3.tools.ietf.org/html/draft-ietf-ldapext-ldapv3-vlv-09">Virtual
 * List View RFC draft</a>.
 *
 * <blockquote> Here we walk through the client-server interaction for a
 * specific virtual list view example: The task is to display a list of all
 * 78564 persons in the US company "Ace Industry". This will be done by creating
 * a graphical user interface object to display the list contents, and by
 * repeatedly sending different versions of the same virtual list view search
 * request to the server. The list view displays 20 entries on the screen at a
 * time.
 * <p>
 * We form a search with baseObject of "o=Ace Industry,c=us"; scope of
 * wholeSubtree; and filter of "(objectClass=person)". We attach a server-side
 * sort control [SSS] to the search request, specifying ascending sort on
 * attribute "cn". To this search request, we attach a virtual list view request
 * control with contents determined by the user activity and send the search
 * request to the server. We display the results from each search result entry
 * in the list window and update the slider position.
 * <p>
 * When the list view is first displayed, we want to initialize the contents
 * showing the beginning of the list. Therefore, we set beforeCount to 0,
 * afterCount to 19, contentCount to 0, offset to 1 and send the request to the
 * server. The server duly returns the first 20 entries in the list, plus a
 * content count of 78564 and targetPosition of 1. We therefore leave the scroll
 * bar slider at its current location (the top of its range).
 * <p>
 * Say that next the user drags the scroll bar slider down to the bottom of its
 * range. We now wish to display the last 20 entries in the list, so we set
 * beforeCount to 19, afterCount to 0, contentCount to 78564, offset to 78564
 * and send the request to the server. The server returns the last 20 entries in
 * the list, plus a content count of 78564 and a targetPosition of 78564.
 * <p>
 * Next the user presses a page up key. Our page size is 20, so we set
 * beforeCount to 0, afterCount to 19, contentCount to 78564, offset to
 * 78564-19-20 and send the request to the server. The server returns the
 * preceding 20 entries in the list, plus a content count of 78564 and a
 * targetPosition of 78525.
 * <p>
 * Now the user grabs the scroll bar slider and drags it to 68% of the way down
 * its travel. 68% of 78564 is 53424 so we set beforeCount to 9, afterCount to
 * 10, contentCount to 78564, offset to 53424 and send the request to the
 * server. The server returns the preceding 20 entries in the list, plus a
 * content count of 78564 and a targetPosition of 53424.
 * <p>
 * Lastly, the user types the letter "B". We set beforeCount to 9, afterCount to
 * 10 and greaterThanOrEqual to "B". The server finds the first entry in the
 * list not less than "B", let's say "Babs Jensen", and returns the nine
 * preceding entries, the target entry, and the proceeding 10 entries. The
 * server returns a content count of 78564 and a targetPosition of 5234 and so
 * the client updates its scroll bar slider to 6.7% of full scale. </blockquote>
 *
 * @author Ulrik Sandberg
 */
@ContextConfiguration(locations = { "/conf/ldapTemplateTestContext.xml" })
public class LdapTemplateVirtualListViewSearchITest extends
		AbstractJUnit4SpringContextTests {

	@Autowired
	private LdapTemplate tested;

	private static final String BASE_STRING = "";

	private static final String FILTER_STRING = "(objectClass=person)";

	private SearchControls searchControls;

	private CollectingNameClassPairCallbackHandler callbackHandler;

	@Before
	public void prepareTestedInstance() throws Exception {
		searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	}

	@After
	public void cleanup() throws Exception {
		searchControls = null;
	}

	@Test
	public void testSearchUsingVirtualListView() {
		List list;
		Person person;
		VirtualListViewResultsCookie cookie;
		VirtualListViewControlDirContextProcessor requestControl;
		PersonContextMapper contextMapper = new PersonContextMapper();
		int listSize;
		int targetOffset;

		//
		// Step 1: Prepare for getting the first 20
		//

		callbackHandler = new ContextMapperCallbackHandler(contextMapper);
		requestControl = new VirtualListViewControlDirContextProcessor(20);

		tested.search(BASE_STRING, FILTER_STRING, searchControls,
				callbackHandler, requestControl);
		cookie = requestControl.getCookie();

		// assert that total count is still 78564
		listSize = cookie.getContentCount();
		assertEquals(78564, listSize);

		// assert that we are now at 1
		targetOffset = cookie.getTargetPosition();
		assertEquals(1, targetOffset);

		// assert that we got the right 20
		list = callbackHandler.getList();
		assertEquals(20, list.size());
		person = (Person) list.get(0);
		assertEquals("Adam Ace", person.getFullname());

		//
		// Step 2: Prepare for getting the last 20
		//

		callbackHandler = new ContextMapperCallbackHandler(contextMapper);

		// we need a constructor that takes a beforeCount and an afterCount
		requestControl = new VirtualListViewControlDirContextProcessor(20,
				78564, listSize, cookie);

		tested.search(BASE_STRING, FILTER_STRING, searchControls,
				callbackHandler, requestControl);
		cookie = requestControl.getCookie();

		// assert that total count is still 78564
		listSize = requestControl.getListSize();
		assertEquals(78564, listSize);

		// assert that we are now at 78564
		targetOffset = requestControl.getTargetOffset();
		assertEquals(78564, targetOffset);

		// assert that we got the right 20
		list = callbackHandler.getList();
		assertEquals(20, list.size());
		person = (Person) list.get(19);
		assertEquals("Xavier Zyxel", person.getFullname());

		//
		// Step 3: Prepare for getting the next to last 20
		//

		callbackHandler = new ContextMapperCallbackHandler(contextMapper);

		// we need a constructor that takes a beforeCount and an afterCount
		requestControl = new VirtualListViewControlDirContextProcessor(20,
				78564 - 19 - 20, listSize, cookie);

		tested.search(BASE_STRING, FILTER_STRING, searchControls,
				callbackHandler, requestControl);
		cookie = requestControl.getCookie();

		// assert that total count is still 78564
		listSize = requestControl.getListSize();
		assertEquals(78564, listSize);

		// assert that we are now at 78525
		targetOffset = requestControl.getTargetOffset();
		assertEquals(78525, targetOffset);

		// assert that we got the right 20
		list = callbackHandler.getList();
		assertEquals(20, list.size());
		person = (Person) list.get(0);
		assertEquals("William Schnyder", person.getFullname());

		//
		// Step 4: Prepare for getting the 20 entries around 68%, ie 53424
		//

		callbackHandler = new ContextMapperCallbackHandler(contextMapper);

		// we need a constructor that takes a beforeCount and an afterCount
		requestControl = new VirtualListViewControlDirContextProcessor(20,
				68, listSize, cookie);

		requestControl.setOffsetPercentage(true);
		tested.search(BASE_STRING, FILTER_STRING, searchControls,
				callbackHandler, requestControl);
		cookie = requestControl.getCookie();

		// assert that total count is still 78564
		listSize = requestControl.getListSize();
		assertEquals(78564, listSize);

		// assert that we are now at 53424
		targetOffset = requestControl.getTargetOffset();
		assertEquals(53424, targetOffset);

		// assert that we got the right 20
		list = callbackHandler.getList();
		assertEquals(20, list.size());
		person = (Person) list.get(9);
		assertEquals("Peter Sellers", person.getFullname());

		//
		// Step 5: Prepare for getting the 20 entries around the letter 'B', ie 5234
		//

		callbackHandler = new ContextMapperCallbackHandler(contextMapper);

		// we need a constructor that takes a String for 'greaterThanOrEqual'
		// also beforeCount and afterCount
		requestControl = new VirtualListViewControlDirContextProcessor(20,
				5234, listSize, cookie);

		requestControl.setOffsetPercentage(true);
		tested.search(BASE_STRING, FILTER_STRING, searchControls,
				callbackHandler, requestControl);
		cookie = requestControl.getCookie();

		// assert that total count is still 78564
		listSize = requestControl.getListSize();
		assertEquals(78564, listSize);

		// assert that we are now at 5234
		targetOffset = requestControl.getTargetOffset();
		assertEquals(5234, targetOffset);

		// assert that we got the right 20
		list = callbackHandler.getList();
		assertEquals(20, list.size());
		person = (Person) list.get(9);
		assertEquals("Babs Jensen", person.getFullname());
	}
}
