package com.krishagni.catissueplus.core.administrative.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.krishagni.catissueplus.core.administrative.domain.DistributionOrder;
import com.krishagni.catissueplus.core.biospecimen.events.SpecimenListSummary;
import com.krishagni.catissueplus.core.common.events.UserSummary;

public class DistributionOrderDetail extends DistributionOrderSummary implements Mergeable<String, DistributionOrderDetail>, Serializable {
	private UserSummary distributor;
	
	private String trackingUrl;
	
	private String comments;

	private SpecimenRequestSummary request;

	private SpecimenListSummary specimenList;
	
	private List<DistributionOrderItemDetail> orderItems = new ArrayList<>();
	
	private String activityStatus;

	private Map<String, Object> extraAttrs;

	//
	// For BO template
	//
	private DistributionOrderItemDetail orderItem;

	//
	// for async execution
	//
	private boolean async;

	private boolean completed = true;

	public UserSummary getDistributor() {
		return distributor;
	}

	public void setDistributor(UserSummary distributor) {
		this.distributor = distributor;
	}

	public String getTrackingUrl() {
		return trackingUrl;
	}

	public void setTrackingUrl(String trackingUrl) {
		this.trackingUrl = trackingUrl;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public SpecimenRequestSummary getRequest() {
		return request;
	}

	public void setRequest(SpecimenRequestSummary request) {
		this.request = request;
	}

	public SpecimenListSummary getSpecimenList() {
		return specimenList;
	}

	public void setSpecimenList(SpecimenListSummary specimenList) {
		this.specimenList = specimenList;
	}

	public List<DistributionOrderItemDetail> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<DistributionOrderItemDetail> orderItems) {
		this.orderItems = orderItems;
	}

	public String getActivityStatus() {
		return activityStatus;
	}

	public void setActivityStatus(String activityStatus) {
		this.activityStatus = activityStatus;
	}

	public Map<String, Object> getExtraAttrs() {
		return extraAttrs;
	}

	public void setExtraAttrs(Map<String, Object> extraAttrs) {
		this.extraAttrs = extraAttrs;
	}

	public DistributionOrderItemDetail getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(DistributionOrderItemDetail orderItem) {
		this.orderItem = orderItem;
	}

	public boolean isAsync() {
		return async;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public static DistributionOrderDetail from(DistributionOrder order) {
		return from(order, false);
	}

	public static DistributionOrderDetail from(DistributionOrder order, boolean includeOrderItems) {
		DistributionOrderDetail detail = new DistributionOrderDetail();
		fromTo(order, detail);

		if (order.getDistributor() != null ) {
			detail.setDistributor(UserSummary.from(order.getDistributor()));
		}

		if (order.getRequest() != null) {
			detail.setRequest(SpecimenRequestSummary.from(order.getRequest()));
		}

		if (order.getSpecimenList() != null) {
			detail.setSpecimenList(SpecimenListSummary.fromSpecimenList(order.getSpecimenList()));
		}
		
		detail.setTrackingUrl(order.getTrackingUrl());
		detail.setComments(order.getComments());
		if (includeOrderItems) {
			detail.setOrderItems(DistributionOrderItemDetail.from(order.getOrderItems()));
		}

		detail.setActivityStatus(order.getActivityStatus());
		return detail;
	}
	
	public static List<DistributionOrderDetail> from(List<DistributionOrder> orders) {
		return orders.stream().map(DistributionOrderDetail::from).collect(Collectors.toList());
	}

	@Override
	@JsonIgnore
	public String getMergeKey() {
		return getName();
	}

	@Override
	public void merge(DistributionOrderDetail other) {
		getOrderItems().add(other.getOrderItem());
	}
}
