package com.krishagni.catissueplus.core.biospecimen.domain;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.krishagni.catissueplus.core.common.util.AuthUtil;

public class ReceivedEvent extends SpecimenEvent {
	private String quality;

	public ReceivedEvent(Specimen specimen) {
		super(specimen);
	}
	
	public String getQuality() {
		loadRecordIfNotLoaded();
		return quality;
	}

	public void setQuality(String quality) {
		loadRecordIfNotLoaded();
		this.quality = quality;
	}

	@Override
	public String getFormName() {
		return "SpecimenReceivedEvent";
	}
	
	@Override
	public Map<String, Object> getEventAttrs() {		
		return Collections.<String, Object>singletonMap("quality", quality);
	}

	@Override
	public void setEventAttrs(Map<String, Object> attrValues) {
		this.quality = (String)attrValues.get("quality");
	}
	
	public void update(ReceivedEvent other) {
		super.update(other);
		setQuality(other.getQuality());
	}
	
	public static ReceivedEvent getFor(Specimen specimen) {
		if (specimen.getId() == null) {
			return createFromSr(specimen);
		}

		List<Long> recIds = new ReceivedEvent(specimen).getRecordIds();		
		if (CollectionUtils.isEmpty(recIds)) {
			return createFromSr(specimen);
		}
		
		ReceivedEvent event = new ReceivedEvent(specimen);
		event.setId(recIds.iterator().next());
		return event;		
	}
	
	public static ReceivedEvent createFromSr(Specimen specimen) {
		ReceivedEvent event = new ReceivedEvent(specimen);
		event.setQuality(Specimen.ACCEPTABLE);
		
		SpecimenRequirement sr = specimen.getSpecimenRequirement();
		if (sr != null) {
			event.setUser(sr.getReceiver());
		}
		
		event.setTime(Calendar.getInstance().getTime());		
		if (event.getUser() == null) {
			event.setUser(AuthUtil.getCurrentUser());
		}		
		
		return event;
	}	
}
