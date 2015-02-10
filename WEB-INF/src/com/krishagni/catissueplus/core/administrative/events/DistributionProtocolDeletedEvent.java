
package com.krishagni.catissueplus.core.administrative.events;

import com.krishagni.catissueplus.core.common.errors.ObjectCreationException;
import com.krishagni.catissueplus.core.common.events.EventStatus;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class DistributionProtocolDeletedEvent extends ResponseEvent {
	private DistributionProtocolDetail protocol;
	
	private long id;

	private String title;
	
	public DistributionProtocolDetail getProtocol() {
		return protocol;
	}

	public void setProtocol(DistributionProtocolDetail details) {
		this.protocol = details;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public static DistributionProtocolDeletedEvent ok(DistributionProtocolDetail protocolDetails) {
		DistributionProtocolDeletedEvent createdEvent = new DistributionProtocolDeletedEvent();
		createdEvent.setProtocol(protocolDetails);
		createdEvent.setStatus(EventStatus.OK);

		return createdEvent;
	}
	
	public static DistributionProtocolDeletedEvent notFound(Long id) {
		DistributionProtocolDeletedEvent resp = new DistributionProtocolDeletedEvent();
		resp.setId(id);
		resp.setStatus(EventStatus.NOT_FOUND);
		return resp;
	}

	public static DistributionProtocolDeletedEvent notFound(String title) {
		DistributionProtocolDeletedEvent resp = new DistributionProtocolDeletedEvent();
		resp.setTitle(title);
		resp.setStatus(EventStatus.NOT_FOUND);
		return resp;
	}


	public static DistributionProtocolDeletedEvent badRequest(Exception e) {
		DistributionProtocolDeletedEvent resp = new DistributionProtocolDeletedEvent();
		resp.setStatus(EventStatus.BAD_REQUEST);
		resp.setException(e);
		resp.setMessage(e.getMessage());
		
		if (e instanceof ObjectCreationException) {
			resp.setErroneousFields(((ObjectCreationException)e).getErroneousFields());
		}
		return resp;
	}

	public static DistributionProtocolDeletedEvent serverError(Throwable... t) {
		Throwable t1 = t != null && t.length > 0 ? t[0] : null;
		DistributionProtocolDeletedEvent resp = new DistributionProtocolDeletedEvent();
		resp.setStatus(EventStatus.INTERNAL_SERVER_ERROR);
		resp.setException(t1);
		resp.setMessage(t1 != null ? t1.getMessage() : null);
		return resp;
	}
}
