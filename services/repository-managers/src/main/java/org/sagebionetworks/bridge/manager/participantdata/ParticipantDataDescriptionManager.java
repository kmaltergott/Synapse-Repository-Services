package org.sagebionetworks.bridge.manager.participantdata;

import java.util.List;

import org.sagebionetworks.bridge.model.data.ParticipantDataColumnDescriptor;
import org.sagebionetworks.bridge.model.data.ParticipantDataCurrentRow;
import org.sagebionetworks.bridge.model.data.ParticipantDataDescriptor;
import org.sagebionetworks.bridge.model.data.ParticipantDataStatus;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.PaginatedResults;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.web.NotFoundException;

public interface ParticipantDataDescriptionManager {
	ParticipantDataDescriptor createParticipantDataDescriptor(UserInfo userInfo, ParticipantDataDescriptor participantDataDescriptor);

	ParticipantDataDescriptor getParticipantDataDescriptor(UserInfo userInfo, String participantDataId) throws DatastoreException, NotFoundException;

	PaginatedResults<ParticipantDataDescriptor> getAllParticipantDataDescriptors(UserInfo userInfo, Integer limit, Integer offset);

	PaginatedResults<ParticipantDataDescriptor> getUserParticipantDataDescriptors(UserInfo userInfo, Integer limit, Integer offset);

	ParticipantDataColumnDescriptor createParticipantDataColumnDescriptor(UserInfo userInfo, ParticipantDataColumnDescriptor participantDataColumnDescriptor);

	PaginatedResults<ParticipantDataColumnDescriptor> getParticipantDataColumnDescriptor(UserInfo userInfo, String participantDataId, Integer limit, Integer offset);

	void updateStatuses(UserInfo userInfo, List<ParticipantDataStatus> statuses) throws DatastoreException;

	List<ParticipantDataColumnDescriptor> getColumns(String participantDataId) throws DatastoreException, NotFoundException;
}
