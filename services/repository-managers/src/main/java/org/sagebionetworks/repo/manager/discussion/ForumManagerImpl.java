package org.sagebionetworks.repo.manager.discussion;

import org.sagebionetworks.repo.manager.AuthorizationManager;
import org.sagebionetworks.repo.manager.AuthorizationManagerUtil;
import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.NodeDAO;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.dao.discussion.ForumDAO;
import org.sagebionetworks.repo.model.discussion.Forum;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.transactions.WriteTransactionReadCommitted;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.util.ValidateArgument;
import org.springframework.beans.factory.annotation.Autowired;

public class ForumManagerImpl implements ForumManager {
	@Autowired
	private ForumDAO forumDao;
	@Autowired
	private NodeDAO nodeDao;
	@Autowired
	private AuthorizationManager authorizationManager;

	@WriteTransactionReadCommitted
	@Override
	public Forum createForum(UserInfo user, String projectId) {
		validateProjectIdAndThrowException(projectId);
		UserInfo.validateUserInfo(user);
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(user, projectId, ObjectType.ENTITY, ACCESS_TYPE.READ));
		return forumDao.createForum(projectId);
	}

	private void validateProjectIdAndThrowException(String projectId) {
		ValidateArgument.required(projectId, "projectId");
		if (!nodeDao.doesNodeExist(KeyFactory.stringToKey(projectId))) {
			throw new NotFoundException("Project does not exist.");
		}
	}

	@WriteTransactionReadCommitted
	@Override
	public Forum getForumByProjectId(UserInfo user, String projectId) {
		validateProjectIdAndThrowException(projectId);
		UserInfo.validateUserInfo(user);
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(user, projectId, ObjectType.ENTITY, ACCESS_TYPE.READ));
		try {
				return forumDao.getForumByProjectId(projectId);
		} catch (NotFoundException e) {
			return createForum(user, projectId);
		}
	}

	@Override
	public Forum getForum(UserInfo user, String forumId) {
		ValidateArgument.required(forumId, "forumId");
		UserInfo.validateUserInfo(user);
		Forum forum = forumDao.getForum(Long.parseLong(forumId));
		AuthorizationManagerUtil.checkAuthorizationAndThrowException(
				authorizationManager.canAccess(user, forum.getProjectId(), ObjectType.ENTITY, ACCESS_TYPE.READ));
		return forum;
	}
}
