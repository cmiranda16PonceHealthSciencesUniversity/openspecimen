package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.List;
import java.util.Set;

import com.krishagni.catissueplus.core.biospecimen.domain.SpecimenList;
import com.krishagni.catissueplus.core.common.ListenAttributeChanges;
import com.krishagni.catissueplus.core.common.events.UserSummary;

@ListenAttributeChanges
public class SpecimenListDetail extends SpecimenListSummary {
	private List<UserSummary> sharedWith;

	private List<Long> specimenIds;

	public List<UserSummary> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(List<UserSummary> sharedWith) {
		this.sharedWith = sharedWith;
	}

	public List<Long> getSpecimenIds() {
		return specimenIds;
	}

	public void setSpecimenIds(List<Long> specimenIds) {
		this.specimenIds = specimenIds;
	}

	public static SpecimenListDetail from(SpecimenList list) {
		SpecimenListDetail details = new SpecimenListDetail();
		details.setId(list.getId());
		details.setName(list.getName());
		details.setDescription(list.getDescription());
		details.setCreatedOn(list.getCreatedOn());
		details.setOwner(UserSummary.from(list.getOwner()));
		details.setSharedWith(UserSummary.from(list.getSharedWith()));
		details.setDefaultList(list.isDefaultList());
		return details;
	}
}