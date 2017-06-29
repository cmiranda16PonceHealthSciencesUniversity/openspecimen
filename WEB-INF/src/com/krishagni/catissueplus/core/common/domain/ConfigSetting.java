package com.krishagni.catissueplus.core.common.domain;

import java.util.Date;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;
import com.krishagni.catissueplus.core.common.domain.ConfigProperty.AccessLevel;

public class ConfigSetting extends BaseEntity {
	private ConfigProperty property;
	
	private String value;
	
	private User activatedBy;
	
	private Date activationDate;
	
	private String activityStatus;
	
	private AccessLevel accessLevel;
	
	private Long objId;
	
	public ConfigProperty getProperty() {
		return property;
	}

	public void setProperty(ConfigProperty property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public User getActivatedBy() {
		return activatedBy;
	}

	public void setActivatedBy(User activatedBy) {
		this.activatedBy = activatedBy;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public AccessLevel getaccessLevel() {
		return accessLevel;
	}

	public void setaccessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
	}

	public Long getObjId() {
		return objId;
	}

	public void setObjId(Long objId) {
		this.objId = objId;
	}
}