package org.sagebionetworks.repo.web.service;

import java.util.List;
import java.util.Set;

import org.sagebionetworks.competition.model.Competition;
import org.sagebionetworks.competition.model.Participant;
import org.sagebionetworks.competition.model.Submission;
import org.sagebionetworks.competition.model.SubmissionStatus;
import org.sagebionetworks.competition.model.SubmissionStatusEnum;
import org.sagebionetworks.repo.model.ConflictingUpdateException;
import org.sagebionetworks.repo.model.DatastoreException;
import org.sagebionetworks.repo.model.InvalidModelException;
import org.sagebionetworks.repo.model.QueryResults;
import org.sagebionetworks.repo.model.UnauthorizedException;
import org.sagebionetworks.repo.web.NotFoundException;

public interface CompetitionService {

	/**
	 * Create a new Synapse Competition
	 * 
	 * @throws DatastoreException
	 * @throws InvalidModelException
	 * @throws NotFoundException
	 */
	public Competition createCompetition(String userId, Competition comp)
			throws DatastoreException, InvalidModelException, NotFoundException;

	/**
	 * Get a collection of Competitions, within a given range
	 *
	 * @param limit
	 * @param offset
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public QueryResults<Competition> getCompetitionsInRange(long limit,
			long offset) throws DatastoreException, NotFoundException;

	/**
	 * Get the total number of Competitions in the system
	 *
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public long getCompetitionCount() throws DatastoreException,
			NotFoundException;

	/**
	 * Find a Competition, by name
	 * 
	 * @param name
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 * @throws UnauthorizedException
	 */
	public Competition findCompetition(String name) throws DatastoreException,
			NotFoundException, UnauthorizedException;

	/**
	 * Update a Synapse Competition.
	 * 
	 * @param userId
	 * @param comp
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 * @throws UnauthorizedException
	 * @throws InvalidModelException
	 * @throws ConflictingUpdateException
	 */
	public Competition updateCompetition(String userId, Competition comp)
			throws DatastoreException, NotFoundException,
			UnauthorizedException, InvalidModelException,
			ConflictingUpdateException;

	/**
	 * Delete a Synapse Competition.
	 * 
	 * @param userId
	 * @param id
	 * @throws DatastoreException
	 * @throws NotFoundException
	 * @throws UnauthorizedException
	 */
	public void deleteCompetition(String userId, String id)
			throws DatastoreException, NotFoundException, UnauthorizedException;

	/**
	 * Add a Participant to an existing Competition. 'userId' is of the
	 * requesting user, 'compId' is of the target competition, and
	 * 'idToAdd' is of the user to be added as a Participant.
	 * 
	 * Note that Competition admins can any user at any time to a Competition,
	 * while non-admins can only manage their own Participation.
	 * 
	 * @param userId
	 * @param compId
	 * @param idToAdd
	 * @return
	 * @throws NotFoundException
	 */
	public Participant addParticipant(String userId, String compId,
			String idToAdd) throws NotFoundException;

	/**
	 * Get a Participant
	 * 
	 * @param userId
	 * @param compId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Participant getParticipant(String userId, String compId)
			throws DatastoreException, NotFoundException;

	/**
	 * Remove a Participant from a Competition.
	 * 
	 * @param userId
	 * @param compId
	 * @param idToRemove
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public void removeParticipant(String userId, String compId,
			String idToRemove) throws DatastoreException, NotFoundException;

	/**
	 * Get all Participants for a given Competition.
	 * 
	 * @param compId
	 * @return
	 * @throws NumberFormatException
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Set<Participant> getAllParticipants(String compId)
			throws NumberFormatException, DatastoreException, NotFoundException;

	/**
	 * Get the number of Participants in a given Competition.
	 * 
	 * @param compId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public long getParticipantCount(String compId) throws DatastoreException,
			NotFoundException;

	/**
	 * Create a Submission.
	 * 
	 * @param userId
	 * @param submission
	 * @return
	 * @throws NotFoundException
	 */
	public Submission createSubmission(String userId, Submission submission)
			throws NotFoundException;

	/**
	 * Get a Submission.
	 * 
	 * @param submissionId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public Submission getSubmission(String submissionId)
			throws DatastoreException, NotFoundException;

	/**
	 * Get the SubmissionStatus object for a Submission.
	 * 
	 * @param submissionId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public SubmissionStatus getSubmissionStatus(String submissionId)
			throws DatastoreException, NotFoundException;

	/**
	 * Update the SubmissionStatus object for a Submission. Note that the
	 * requesting user must be an admin of the Competition for which this
	 * Submission was created.
	 * 
	 * @param userId
	 * @param submissionStatus
	 * @return
	 * @throws NotFoundException
	 */
	public SubmissionStatus updateSubmissionStatus(String userId,
			SubmissionStatus submissionStatus) throws NotFoundException;

	/**
	 * Delete a Submission. Note that the requesting user must be an admin
	 * of the Competition for which this Submission was created.
	 * 
	 * Use of this method is discouraged, since Submissions should be immutable.
	 * 
	 * @param userId
	 * @param submissionId
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	@Deprecated
	public void deleteSubmission(String userId, String submissionId)
			throws DatastoreException, NotFoundException;

	/**
	 * Get all Submissions for a given Competition. This method requires admin
	 * rights.
	 * 
	 * If a SubmissionStatusEnum is provided, results will be filtered
	 * accordingly.
	 * 
	 * @param userId
	 * @param compId
	 * @param status
	 * @return
	 * @throws DatastoreException
	 * @throws UnauthorizedException
	 * @throws NotFoundException
	 */
	public List<Submission> getAllSubmissions(String userId, String compId,
			SubmissionStatusEnum status) throws DatastoreException,
			UnauthorizedException, NotFoundException;

	/**
	 * Get all Submissions by a given Synapse user.
	 * 
	 * @param userId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public List<Submission> getAllSubmissionsByUser(String userId)
			throws DatastoreException, NotFoundException;

	/**
	 * Get the number of Submissions to a given Competition.
	 * 
	 * @param compId
	 * @return
	 * @throws DatastoreException
	 * @throws NotFoundException
	 */
	public long getSubmissionCount(String compId) throws DatastoreException,
			NotFoundException;

}