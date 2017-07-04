package com.krishagni.catissueplus.core.administrative.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.krishagni.catissueplus.core.biospecimen.domain.BaseEntity;
import com.krishagni.catissueplus.core.biospecimen.domain.Specimen;

public class ContainerStoreList extends BaseEntity {
	public enum Op {
		PUT,
		PICK
	}

	public enum STATUS {
		PENDING,
		FAILED,
		SUCCESS
	}

	private StorageContainer container;

	private User user;

	private Op op;

	private Date creationTime;

	private Date executionTime;

	private STATUS status = STATUS.PENDING;

	private Short state;

	private Set<ContainerStoreListItem> items = new HashSet<>();

	public StorageContainer getContainer() {
		return container;
	}

	public void setContainer(StorageContainer container) {
		this.container = container;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Op getOp() {
		return op;
	}

	public void setOp(Op op) {
		this.op = op;
	}

	public Date getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}

	public Date getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(Date executionTime) {
		this.executionTime = executionTime;
	}

	public STATUS getStatus() {
		return status;
	}

	public void setStatus(STATUS status) {
		this.status = status;
	}

	public Short getState() {
		return state;
	}

	public void setState(Short state) {
		this.state = state;
	}

	public Set<ContainerStoreListItem> getItems() {
		return items;
	}

	public void setItems(Set<ContainerStoreListItem> items) {
		this.items = items;
	}

	public void addItem(Specimen specimen) {
		ContainerStoreListItem item = new ContainerStoreListItem();
		item.setSpecimen(specimen);
		item.setStoreList(this);
		getItems().add(item);
	}

	public void process() {
		getContainer().processList(this);
	}
}
