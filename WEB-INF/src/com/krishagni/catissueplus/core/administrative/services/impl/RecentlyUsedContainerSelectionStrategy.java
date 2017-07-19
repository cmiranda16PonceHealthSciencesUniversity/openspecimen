package com.krishagni.catissueplus.core.administrative.services.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.krishagni.catissueplus.core.administrative.domain.StorageContainer;
import com.krishagni.catissueplus.core.administrative.events.ContainerCriteria;
import com.krishagni.catissueplus.core.administrative.services.ContainerSelectionStrategy;
import com.krishagni.catissueplus.core.biospecimen.domain.CollectionProtocol;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.common.Pair;

@Configurable
public class RecentlyUsedContainerSelectionStrategy implements ContainerSelectionStrategy {
	private static Log logger = LogFactory.getLog(RecentlyUsedContainerSelectionStrategy.class);

	@Autowired
	private DaoFactory daoFactory;

	@Autowired
	private SessionFactory sessionFactory;

	private CollectionProtocol cp;

	private Map<String, StorageContainer> recentlyUsed = new HashMap<>();

	@Override
	public StorageContainer getContainer(ContainerCriteria criteria, Boolean aliquotsInSameContainer) {
		StorageContainer container = recentlyUsed.get(criteria.key());
		if (container == null) {
			container = getRecentlySelectedContainer(criteria);
		}

		if (container == null) {
			return null;
		}

		int freePositions = criteria.getRequiredPositions(aliquotsInSameContainer);
		if (!canContainSpecimen(container, criteria, freePositions)) {
			container = nextContainer(container, criteria, freePositions);
		}

		if (container != null) {
			recentlyUsed.put(criteria.key(), container);
		}

		return container;
	}

	@SuppressWarnings("unchecked")
	private StorageContainer getRecentlySelectedContainer(ContainerCriteria crit) {
		//
		// first lookup containers used for (cp, class, type) combination
		//
		List<StorageContainer> containers = getRecentlySelectedContainerQuery(crit)
			.add(Restrictions.eq("spmn.specimenClass", crit.specimen().getSpecimenClass()))
			.add(Restrictions.eq("spmn.specimenType", crit.specimen().getType()))
			.list();

		if (CollectionUtils.isNotEmpty(containers)) {
			return containers.iterator().next();
		}

		//
		// when above fails, lookup containers used for cp alone
		//
		containers = getRecentlySelectedContainerQuery(crit).list();
		return CollectionUtils.isNotEmpty(containers) ? containers.iterator().next() : null;
	}

	private Criteria getRecentlySelectedContainerQuery(ContainerCriteria criteria) {
		Session session = sessionFactory.getCurrentSession();
		session.enableFilter("activeEntity");
		Criteria query = session.createCriteria(StorageContainer.class, "cont")
			.createAlias("cont.occupiedPositions", "pos")
			.createAlias("pos.occupyingSpecimen", "spmn")
			.createAlias("spmn.visit", "visit")
			.createAlias("visit.registration", "reg")
			.createAlias("reg.collectionProtocol", "cp")
			.createAlias("cont.site", "site")
			.createAlias("cont.compAllowedCps", "allowedCp", JoinType.LEFT_OUTER_JOIN)
			.add(Restrictions.eq("cp.id", criteria.specimen().getCpId()))
			.add(getSiteCpRestriction(criteria.siteCps()))
			.addOrder(Order.desc("pos.id"))
			.setMaxResults(1);

		if (criteria.rule() != null) {
			query.add(criteria.rule().getRestriction("cont", criteria.ruleParams()));
		}

		return query;
	}

	private Disjunction getSiteCpRestriction(Set<Pair<Long, Long>> siteCps) {
		Disjunction disjunction = Restrictions.disjunction();
		for (Pair<Long, Long> siteCp : siteCps) {
			disjunction.add(Restrictions.and(
					Restrictions.eq("site.id", siteCp.first()),
					Restrictions.or(Restrictions.isNull("allowedCp.id"), Restrictions.eq("allowedCp.id", siteCp.second()))
			));
		}

		return disjunction;
	}

	private StorageContainer nextContainer(StorageContainer last, ContainerCriteria crit, int freeLocs) {
		logger.info(String.format(
			"Finding next container satisfying criteria (cp = %d, class = %s, type = %s, free locs = %d) with base %s",
			crit.specimen().getCpId(), crit.specimen().getSpecimenClass(), crit.specimen().getType(), freeLocs, last.getName()
		));
		return nextContainer(last.getParentContainer(), last, crit, freeLocs, new HashSet<>());
	}

	private StorageContainer nextContainer(StorageContainer parent, StorageContainer last, ContainerCriteria criteria, int freeLocs, Set<StorageContainer> visited) {
		if (parent == null) {
			return null;
		}

		logger.info(String.format("Exploring children of %s, last = %s", parent.getName(), (last != null) ? last.getName() : "none"));

		int childIdx = -1;
		List<StorageContainer> children = parent.getChildContainersSortedByPosition();
		if (last != null) {
			for (StorageContainer container : children) {
				childIdx++;
				if (container.getPosition().getPosition().equals(last.getPosition().getPosition())) {
					logger.info(String.format("Found container %s at %d", container.getName(), childIdx));
					break;
				}
			}
		}

		for (int i = childIdx + 1; i < children.size(); ++i) {
			if (!visited.add(children.get(i))) {
				continue;
			}

			StorageContainer container = nextContainer(children.get(i), null, criteria, freeLocs, visited);
			if (container != null) {
				return container;
			}
		}

		logger.info("Probing whether container " + parent.getName() + " can satisfy request");
		if (canContainSpecimen(parent, criteria, freeLocs)) {
			logger.info("Selected container " + parent.getName());
			return parent;
		} else if (visited.add(parent)) {
			return nextContainer(parent.getParentContainer(), parent, criteria, freeLocs, visited);
		} else {
			logger.info("End of tree " + parent.getName());
			return null;
		}
	}

	private boolean canContainSpecimen(StorageContainer container, ContainerCriteria crit, int freeLocs) {
		if (cp == null) {
			cp = daoFactory.getCollectionProtocolDao().getById(crit.specimen().getCpId());
		}


		return container.canContainSpecimen(cp, crit.specimen().getSpecimenClass(), crit.specimen().getType()) &&
			container.hasFreePositionsForReservation(freeLocs) &&
			(crit.rule() == null || crit.rule().eval(container, crit.ruleParams()));
	}
}
