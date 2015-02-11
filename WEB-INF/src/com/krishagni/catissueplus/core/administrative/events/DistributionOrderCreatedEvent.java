package com.krishagni.catissueplus.core.administrative.events;

import com.krishagni.catissueplus.core.common.errors.ObjectCreationException;
import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class DistributionOrderCreatedEvent extends ResponseEvent {
	private DistributionOrderDetail order;

	public DistributionOrderDetail getOrder() {
		return order;
	}

	public void setOrder(DistributionOrderDetail detail) {
		this.order = detail;
	}
	
	public static DistributionOrderCreatedEvent ok(DistributionOrderDetail distributionOrder) {
		DistributionOrderCreatedEvent resp = new DistributionOrderCreatedEvent();
		resp.setOrder(distributionOrder);
		resp.setStatus(EventStatus.OK);
		return resp;
	}

	public static DistributionOrderCreatedEvent badRequest(Exception e) {
		DistributionOrderCreatedEvent resp = new DistributionOrderCreatedEvent();
		resp.setStatus(EventStatus.BAD_REQUEST);
		resp.setException(e);
		resp.setMessage(e.getMessage());
		if (e instanceof ObjectCreationException) {
			resp.setErroneousFields(((ObjectCreationException)e).getErroneousFields());
		}
		
		return resp;
	}	
	
	public static DistributionOrderCreatedEvent serverError(Exception e) {
		DistributionOrderCreatedEvent resp = new DistributionOrderCreatedEvent();
		resp.setStatus(EventStatus.INTERNAL_SERVER_ERROR);
		resp.setException(e);
		resp.setMessage(e.getMessage());
		return resp;		
	}
}