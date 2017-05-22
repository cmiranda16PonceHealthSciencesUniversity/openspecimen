package com.krishagni.catissueplus.core.biospecimen.services.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;

import com.krishagni.catissueplus.core.biospecimen.domain.StagedParticipant;
import com.krishagni.catissueplus.core.biospecimen.domain.StagedParticipantMedicalIdentifier;
import com.krishagni.catissueplus.core.biospecimen.events.PmiDetail;
import com.krishagni.catissueplus.core.biospecimen.events.StagedParticipantDetail;
import com.krishagni.catissueplus.core.biospecimen.repository.DaoFactory;
import com.krishagni.catissueplus.core.biospecimen.services.StagedParticipantService;
import com.krishagni.catissueplus.core.common.PlusTransactional;
import com.krishagni.catissueplus.core.common.errors.OpenSpecimenException;
import com.krishagni.catissueplus.core.common.events.RequestEvent;
import com.krishagni.catissueplus.core.common.events.ResponseEvent;

public class StagedParticipantServiceImpl implements StagedParticipantService {

	private DaoFactory daoFactory;

	public void setDaoFactory(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
	}

	@Override
	@PlusTransactional
	public ResponseEvent<StagedParticipantDetail> saveOrUpdateParticipant(RequestEvent<StagedParticipantDetail> req) {
		try {
			StagedParticipantDetail detail = req.getPayload();
			StagedParticipant savedParticipant = saveOrUpdateParticipant(getMatchingParticipant(detail), detail);
			return ResponseEvent.response(StagedParticipantDetail.from(savedParticipant));
		} catch (OpenSpecimenException ose) {
			return ResponseEvent.error(ose);
		} catch (Exception e) {
			return ResponseEvent.serverError(e);
		}
	}

	private StagedParticipant getMatchingParticipant(StagedParticipantDetail detail) {
		if (StringUtils.isBlank(detail.getEmpi())) {
			return null;
		}

		return daoFactory.getStagedParticipantDao().getByEmpi(detail.getEmpi());
	}

	private StagedParticipant saveOrUpdateParticipant(StagedParticipant existing, StagedParticipantDetail input) {
		StagedParticipant participant = createParticipant(existing, input);
		if (existing != null) {
			existing.update(participant);
			participant = existing;
		}

		daoFactory.getStagedParticipantDao().saveOrUpdate(participant);
		return participant;
	}

	private StagedParticipant createParticipant(StagedParticipant existing, StagedParticipantDetail detail) {
		StagedParticipant participant = new StagedParticipant();
		if (existing != null) {
			BeanUtils.copyProperties(existing, participant);
		}

		setParticipantAtrrs(detail, participant);
		return participant;
	}

	private void setParticipantAtrrs(StagedParticipantDetail detail, StagedParticipant participant) {
		participant.setFirstName(detail.getFirstName());
		participant.setLastName(detail.getLastName());
		participant.setBirthDate(detail.getBirthDate());
		participant.setGender(detail.getGender());
		participant.setVitalStatus(detail.getVitalStatus());
		participant.setUpdatedTime(Calendar.getInstance().getTime());

		if (StringUtils.isNotBlank(detail.getNewEmpi())) {
			participant.setEmpi(detail.getNewEmpi());
			participant.getPmiList().addAll(getPmis(detail.getPmis()));
		} else {
			participant.setEmpi(detail.getEmpi());
			participant.setPmiList(getPmis(detail.getPmis()));

		}

		Set<String> races = detail.getRaces();
		if (CollectionUtils.isNotEmpty(races)) {
			participant.setRaces(races);
		}

		Set<String> ethnicities = detail.getEthnicities();
		if (CollectionUtils.isNotEmpty(ethnicities)) {
			participant.setEthnicities(ethnicities);
		}
	}

	private Set<StagedParticipantMedicalIdentifier> getPmis(List<PmiDetail> pmis) {
		if (CollectionUtils.isEmpty(pmis)) {
			return Collections.emptySet();
		}

		return pmis.stream().map(
			id -> {
				StagedParticipantMedicalIdentifier pmi = new StagedParticipantMedicalIdentifier();
				pmi.setSite(id.getSiteName());
				pmi.setMedicalRecordNumber(id.getMrn());
				return pmi;
			}
		).collect(Collectors.toSet());
	}
}
