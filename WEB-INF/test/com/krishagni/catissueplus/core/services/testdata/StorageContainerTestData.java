
package com.krishagni.catissueplus.core.services.testdata;

import static com.krishagni.catissueplus.core.common.errors.CatissueException.reportError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;

import com.krishagni.catissueplus.core.administrative.domain.Address;
import com.krishagni.catissueplus.core.administrative.domain.Department;
import com.krishagni.catissueplus.core.administrative.domain.Site;
import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.administrative.domain.factory.UserErrorCode;
import com.krishagni.catissueplus.core.administrative.events.DisableStorageContainerEvent;
import com.krishagni.catissueplus.core.administrative.events.CreateStorageContainerEvent;
import com.krishagni.catissueplus.core.administrative.events.PatchStorageContainerEvent;
import com.krishagni.catissueplus.core.administrative.events.StorageContainerDetails;
import com.krishagni.catissueplus.core.administrative.events.UpdateStorageContainerEvent;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.common.util.Status;
import com.krishagni.catissueplus.core.privileges.domain.UserCPRole;

import edu.wustl.common.beans.SessionDataBean;

public class StorageContainerTestData {

	public static final String ACTIVITY_STATUS_CLOSED = "Disabled";

	public static final String CONTAINER_NAME = "container name";

	public static final String SITE = "site";

	public static final String COLLECTION_PROTOCOL = "collection protocol";

	public static final Object USER = "user";

	public static final Object BARCODE = "barcode";

	public static final String STORAGE_CONTAINER = "storage container";

	public static final String PATCH_CONTAINER = "patch container";
	
	public static final String ONE_DIMENSION_CAPACITY = "one dimension capacity";

	public static final String TWO_DIMENSION_CAPACITY = "two dimension capacity";

	public static List<User> getUserList() {
		List<User> users = new ArrayList<User>();
		users.add(new User());
		users.add(new User());
		return users;
	}

	public static SessionDataBean getSessionDataBean() {
		SessionDataBean sessionDataBean = new SessionDataBean();
		sessionDataBean.setAdmin(true);
		sessionDataBean.setCsmUserId("1");
		sessionDataBean.setFirstName("admin");
		sessionDataBean.setIpAddress("127.0.0.1");
		sessionDataBean.setLastName("admin");
		sessionDataBean.setUserId(1L);
		sessionDataBean.setUserName("admin@admin.com");
		return sessionDataBean;
	}

	private static Collection<String> getCpNames() {
		Collection<String> cpNames = new HashSet<String>();
		cpNames.add("My CP");
		cpNames.add("Cp1");
		return cpNames;
	}

	public static User getUser(Long id) {
		User user = new User();
		user.setId(id);
		user.setFirstName("firstName1");
		user.setLastName("lastName1");
		user.setLoginName("admin@admin.com");
		user.setEmailAddress("sci@sci.com");
		user.setPasswordToken("e5412f93-a1c5-4ede-b66d-b32302cd4018");
		user.setDepartment(new Department());
		user.setAddress(new Address());
		user.setUserSites(new HashSet<Site>());
		user.setUserCPRoles(new HashSet<UserCPRole>());
		return user;
	}

	public static Site getSite() {
		Site site = new Site();
		site.setName("My Site");
		site.setId(1l);
		return site;
	}

	public static CollectionProtocol getCp() {
		CollectionProtocol collectionProtocol = new CollectionProtocol();
		collectionProtocol.setTitle("Query CP");
		collectionProtocol.setId(1l);
		collectionProtocol.setShortTitle("qcp");
		return collectionProtocol;
	}

	public static CreateStorageContainerEvent getCreateStorageContainerEvent() {
		CreateStorageContainerEvent event = new CreateStorageContainerEvent(getStorageContainerDetails());
		return event;
	}
	
	public static CreateStorageContainerEvent getCreateStorageContainerEventWithoutCpRestrict() {
		CreateStorageContainerEvent event = new CreateStorageContainerEvent(getStorageContainerDetails());
		event.getDetails().setCpTitleCollection(new HashSet<String>());
		return event;
	}

	public static CreateStorageContainerEvent getCreateStorageContainerEventWithEmptyName() {
		CreateStorageContainerEvent event = getCreateStorageContainerEvent();
		event.getDetails().setName("");
		return event;
	}

	public static StorageContainerDetails getStorageContainerDetails() {
		StorageContainerDetails details = new StorageContainerDetails();
		details.setActivityStatus("Active");
		details.setCpTitleCollection(getCpNames());
		details.setBarcode("2-edpwesdadas-343");
		details.setOneDimensionCapacity(10);
		details.setTwoDimensionCapacity(10);
		details.setName("Container1");
		details.setComments("Blah blah blah");
		details.setParentContainerName("Freezer");
		details.setSiteName("My Site");
		details.setTempratureInCentigrade(22.22);
		details.setCreatedBy(1l);
		return details;
	}

	public static StorageContainer getStorageContainer(Long l) {
		StorageContainer container = new StorageContainer();
		container.setHoldsCPs(getCps());
		container.setBarcode("2-edpwesdadas-343");
		container.setOneDimensionCapacity(10);
		container.setTwoDimensionCapacity(10);
		container.setName("Container1");
		container.setComments("Blah blah blah");
		container.setSite(getSite());
		container.setTempratureInCentigrade(22.22);
		container.setCreatedBy(getUser(1l));
		return container;
	}

	private static Set<CollectionProtocol> getCps() {
		Set<CollectionProtocol> cps = new HashSet<CollectionProtocol>();
		cps.add(getCp());
		return cps;
	}

	public static UpdateStorageContainerEvent getUpdateStorageContainerEvent() {
		UpdateStorageContainerEvent event = new UpdateStorageContainerEvent(getStorageContainerDetails(), 1l);
		return event;
	}

	public static PatchStorageContainerEvent getPatchData() {
		PatchStorageContainerEvent event = new PatchStorageContainerEvent();
		event.setStorageContainerId(1l);
		StorageContainerDetails details = new StorageContainerDetails();
		try {
			BeanUtils.populate(details, getStorageContainerPatchAttributes());
		}
		catch (Exception e) {
			reportError(UserErrorCode.BAD_REQUEST, PATCH_CONTAINER);
		}
		details.setModifiedAttributes(new ArrayList<String>(getStorageContainerPatchAttributes().keySet()));
		event.setStorageContainerDetails(details);
		return event;
	}

	private static Map<String, Object> getStorageContainerPatchAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("name", "Container");
		attributes.put("barcode", "a-essdsds-222");
		attributes.put("siteName", "mySite");
		attributes.put("cpTitleCollection", getCpNames());
		attributes.put("activityStatus", Status.ACTIVITY_STATUS_DISABLED.getStatus());
		attributes.put("comments", "blah blah");
		attributes.put("tempratureInCentigrade", 22.22);
		attributes.put("parentContainerName", "Freezer");
		attributes.put("holdsSpecimenTypes", getSpecimenTypes());	
		attributes.put("createdBy", 1l);
		attributes.put("oneDimensionCapacity", 20);
		attributes.put("oneDimensionCapacity", 20);
		return attributes;
	}

	private static Set<String> getSpecimenTypes() {
		Set<String> spectTypes = new HashSet<String>();
		spectTypes.add("Tissue");
		spectTypes.add("Blood");
		return spectTypes;
	}

	public static DisableStorageContainerEvent getDisableStorageContainerEvent() {
		DisableStorageContainerEvent event = new DisableStorageContainerEvent();
		event.setId(1l);
		event.setSessionDataBean(getSessionDataBean());
		return event;
	}

	public static CreateStorageContainerEvent getCreateStorageContainerEventForOneDimentionCapacity() {
		CreateStorageContainerEvent event = getCreateStorageContainerEvent();
		event.getDetails().setOneDimensionCapacity(null);
		return event;
	}

	public static CreateStorageContainerEvent getCreateStorageContainerEventForTwoDimentionCapacity() {
		CreateStorageContainerEvent event = getCreateStorageContainerEvent();
		event.getDetails().setTwoDimensionCapacity(-1);
		return event;
	}

	public static UpdateStorageContainerEvent getUpdateStorageContainerEventWithChangeInName() {
		UpdateStorageContainerEvent event = getUpdateStorageContainerEvent();
		event.getDetails().setName("Dsda");
		event.getDetails().setBarcode("sda434-434");		
		return event;
	}

	public static UpdateStorageContainerEvent getUpdateStorageContainerEventForTwoDimentionCapacity() {
		UpdateStorageContainerEvent event = getUpdateStorageContainerEvent();
		event.getDetails().setTwoDimensionCapacity(-1);
		return event;
	}
}
