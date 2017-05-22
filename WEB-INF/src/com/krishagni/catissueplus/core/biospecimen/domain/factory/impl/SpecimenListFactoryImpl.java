package com.krishagni.catissueplus.core.biospecimen.domain.factory.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.krishagni.catissueplus.core.administrative.domain.User;
import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenList;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenListErrorCode;
import com.krishagni.catissueplus.core.biospecimen.domain.factory.SpecimenListFactory;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenListDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.errors.ErrorType;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.UserSummary;
import com.krishagni.catissueplus.core.common.util.MessageUtil;

public class SpecimenListFactoryImpl implements SpecimenListFactory {
	private DaoFactory daoFactory;
	
	public DaoFactory getDaoFactory() {
		return daoFactory;
	}

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	public SpecimenList createSpecimenList(SpecimenListDetail details) {
		SpecimenList specimenList = new SpecimenList();
		
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		setSpecimenListAttributes(details, specimenList, false, ose);

		ose.checkAndThrow();
		return specimenList;
	}
	
	@Override
	public SpecimenList createSpecimenList(SpecimenList existing, SpecimenListDetail details) {
		SpecimenList specimenList = new SpecimenList();
		BeanUtils.copyProperties(existing, specimenList);
		
		OpenSpecimenException ose = new OpenSpecimenException(ErrorType.USER_ERROR);
		setSpecimenListAttributes(details, specimenList, true, ose);
		
		ose.checkAndThrow();
		return specimenList;
	}

	public SpecimenList createDefaultSpecimenList(User user) {
		SpecimenList specimenList = new SpecimenList();
		specimenList.setOwner(user);
		specimenList.setName(SpecimenList.getDefaultListName(user));
		specimenList.setDescription(MessageUtil.getInstance().getMessage("specimen_list_default_user_list"));
		specimenList.setCreatedOn(Calendar.getInstance().getTime());
		specimenList.setLastUpdatedOn(specimenList.getCreatedOn());
		return specimenList;
	}

	private void setSpecimenListAttributes(SpecimenListDetail details, SpecimenList specimenList, boolean partial, OpenSpecimenException ose) {
		if (specimenList.getId() == null && details.getId() != null) {
			specimenList.setId(details.getId()); 
		}
		
		setOwner(details, specimenList, partial , ose);
		setName(details, specimenList, partial , ose);
		setDescription(details, specimenList, partial, ose);
		setSharedUsers(details, specimenList, partial, ose);

		specimenList.setCreatedOn(Calendar.getInstance().getTime());
		specimenList.setLastUpdatedOn(specimenList.getCreatedOn());
	}
    
	private void setOwner(SpecimenListDetail details, SpecimenList specimenList, boolean partial, OpenSpecimenException ose) {
		if (partial && !details.isAttrModified("owner")) {
			return;
		}
		
		Long userId = details.getOwner() != null ? details.getOwner().getId() : null;
		
		if (userId == null) {
			ose.addError(SpecimenListErrorCode.OWNER_REQUIRED);			
		} else {
			User user = daoFactory.getUserDao().getById(userId);
			if (user == null) {
				ose.addError(SpecimenListErrorCode.OWNER_NOT_FOUND);
			} else {
				specimenList.setOwner(user);
			}
		}
	}

	private void setName(SpecimenListDetail details, SpecimenList specimenList, boolean partial, OpenSpecimenException ose) {
		if (partial && !details.isAttrModified("name")) {
			return;
		}
		
		String name = details.getName();
		if (StringUtils.isBlank(name)) {
			ose.addError(SpecimenListErrorCode.NAME_REQUIRED);
		} else {
			specimenList.setName(name);
		}		
	}

	private void setDescription(SpecimenListDetail details, SpecimenList specimenList, boolean partial, OpenSpecimenException ose) {
		if (partial && !details.isAttrModified("description")) {
			return;
		}

		specimenList.setDescription(details.getDescription());
	}

	private void setSharedUsers(SpecimenListDetail details, SpecimenList specimenList, boolean partial, OpenSpecimenException ose) {
		if (partial && !details.isAttrModified("sharedWith")) {
			return;
		}
		
		Long userId = details.getOwner() != null ? details.getOwner().getId() : null;
		
		List<Long> userIds = new ArrayList<Long>();
		if (!CollectionUtils.isEmpty(details.getSharedWith())) {
			for (UserSummary user : details.getSharedWith()) {
				if (user.getId().equals(userId)) {
					continue;
				}
				userIds.add(user.getId());
			}
		}
		
		if (userIds != null && !userIds.isEmpty()) {
			List<User> sharedUsers = daoFactory.getUserDao().getUsersByIds(userIds);
			if (sharedUsers.size() != userIds.size()) {
				ose.addError(SpecimenListErrorCode.INVALID_USERS_LIST);
			} else {
				specimenList.setSharedWith(new HashSet<User>(sharedUsers));
			}
		} else {
			specimenList.getSharedWith().clear();
		}
	}
}
