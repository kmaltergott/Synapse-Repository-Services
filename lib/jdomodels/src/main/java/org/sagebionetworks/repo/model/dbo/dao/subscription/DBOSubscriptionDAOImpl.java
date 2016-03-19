package org.sagebionetworks.repo.model.dbo.dao.subscription;

import static org.sagebionetworks.repo.model.query.jdo.SqlConstants.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sagebionetworks.ids.IdGenerator;
import org.sagebionetworks.ids.IdGenerator.TYPE;
import org.sagebionetworks.repo.model.dao.subscription.SubscriptionDAO;
import org.sagebionetworks.repo.model.dbo.DBOBasicDao;
import org.sagebionetworks.repo.model.subscription.Subscription;
import org.sagebionetworks.repo.model.subscription.SubscriptionObjectType;
import org.sagebionetworks.repo.model.subscription.SubscriptionPagedResults;
import org.sagebionetworks.repo.transactions.WriteTransactionReadCommitted;
import org.sagebionetworks.repo.web.NotFoundException;
import org.sagebionetworks.util.ValidateArgument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class DBOSubscriptionDAOImpl implements SubscriptionDAO{

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private DBOBasicDao basicDao;
	@Autowired
	private IdGenerator idGenerator;

	private static final String SQL_INSERT_IGNORE = "INSERT IGNORE INTO "
			+ TABLE_SUBSCRIPTION + " ( "
			+ COL_SUBSCRIPTION_ID + ", "
			+ COL_SUBSCRIPTION_SUBSCRIBER_ID + ", "
			+ COL_SUBSCRIPTION_OBJECT_ID + ", "
			+ COL_SUBSCRIPTION_OBJECT_TYPE + ", "
			+ COL_SUBSCRIPTION_CREATED_ON + ") "
			+ " VALUES (?, ?, ?, ?, ?)";

	private static final String SQL_GET = "SELECT * "
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_ID+" = ?";

	private static final String SQL_GET_BY_PRIMARY_KEY = "SELECT * "
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" = ? "
			+ "AND "+COL_SUBSCRIPTION_OBJECT_ID+" = ? "
			+ "AND "+COL_SUBSCRIPTION_OBJECT_TYPE+" = ?";

	private static final String SQL_GET_ALL = "SELECT * "
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" = ? ";

	private static final String OBJECT_TYPE_CONDITION = "AND "+COL_SUBSCRIPTION_OBJECT_TYPE+" = ? ";

	private static final String LIMIT_OFFSET = "LIMIT ? OFFSET ?";

	private static final String SQL_COUNT = "SELECT COUNT(*)"
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" = ? ";

	private static final String SQL_GET_LIST = "SELECT * "
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" = :subscriberId "
			+ "AND "+COL_SUBSCRIPTION_OBJECT_TYPE+" = :objectType "
			+ "AND "+COL_SUBSCRIPTION_OBJECT_ID+" IN ( :ids )";

	private static final String SQL_DELETE = "DELETE FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_ID+" = ?";

	private static final String SQL_DELETE_ALL = "DELETE FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" = ?";

	private static final String SQL_GET_SUBSCRIBERS = "SELECT "+COL_SUBSCRIPTION_SUBSCRIBER_ID+" "
			+ "FROM "+TABLE_SUBSCRIPTION+" "
			+ "WHERE "+COL_SUBSCRIPTION_OBJECT_ID+" = ? "
			+ "AND "+COL_SUBSCRIPTION_OBJECT_TYPE+" = ?";

	private static final RowMapper<Subscription> ROW_MAPPER = new RowMapper<Subscription>(){

		@Override
		public Subscription mapRow(ResultSet rs, int rowNum) throws SQLException {
			Subscription subscription = new Subscription();
			subscription.setSubscriptionId(""+rs.getLong(COL_SUBSCRIPTION_ID));
			subscription.setSubscriberId(""+rs.getLong(COL_SUBSCRIPTION_SUBSCRIBER_ID));
			subscription.setObjectId(""+rs.getLong(COL_SUBSCRIPTION_OBJECT_ID));
			subscription.setObjectType(SubscriptionObjectType.valueOf(rs.getString(COL_SUBSCRIPTION_OBJECT_TYPE)));
			subscription.setCreatedOn(new Date(rs.getLong(COL_SUBSCRIPTION_CREATED_ON)));
			return subscription;
		}
	};

	@WriteTransactionReadCommitted
	@Override
	public Subscription create(String subscriberId, String objectId,
			SubscriptionObjectType objectType) {
		ValidateArgument.required(subscriberId, "subscriberId");
		ValidateArgument.required(objectId, "objectId");
		ValidateArgument.required(objectType, "objectType");
		long subscriptionId = idGenerator.generateNewId(TYPE.SUBSCRIPTION_ID);
		jdbcTemplate.update(SQL_INSERT_IGNORE, subscriptionId, subscriberId, objectId, objectType.name(), new Date().getTime());
		return get(subscriberId, objectId, objectType);
	}

	private Subscription get(String subscriberId, String objectId,
			SubscriptionObjectType objectType) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET_BY_PRIMARY_KEY, new Object[]{subscriberId, objectId, objectType.name()}, ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException(e.getMessage());
		}
	}

	@Override
	public Subscription get(long subscriptionId) {
		try {
			return jdbcTemplate.queryForObject(SQL_GET, new Object[]{subscriptionId}, ROW_MAPPER);
		} catch (EmptyResultDataAccessException e) {
			throw new NotFoundException(e.getMessage());
		}
	}

	@Override
	public SubscriptionPagedResults getAll(String subscriberId, Long limit,
			Long offset, SubscriptionObjectType objectType) {
		ValidateArgument.required(subscriberId, "subscriberId");
		ValidateArgument.required(limit, "limit");
		ValidateArgument.required(offset, "offset");
		SubscriptionPagedResults results = new SubscriptionPagedResults();
		if (objectType == null) {
			results.setResults(jdbcTemplate.query(SQL_GET_ALL+LIMIT_OFFSET, ROW_MAPPER, subscriberId, limit, offset));
			results.setTotalNumberOfResults(jdbcTemplate.queryForLong(SQL_COUNT, subscriberId));
		} else {
			results.setResults(jdbcTemplate.query(SQL_GET_ALL+OBJECT_TYPE_CONDITION+LIMIT_OFFSET,
					ROW_MAPPER, subscriberId, objectType.name(), limit, offset));
			results.setTotalNumberOfResults(jdbcTemplate.queryForLong(SQL_COUNT+OBJECT_TYPE_CONDITION, subscriberId, objectType.name()));
		}
		return results;
	}

	@Override
	public SubscriptionPagedResults getSubscriptionList(String subscriberId,
			SubscriptionObjectType objectType, List<String> ids) {
		ValidateArgument.required(subscriberId, "subscriberId");
		ValidateArgument.required(objectType, "objectType");
		ValidateArgument.required(ids, "ids");
		SubscriptionPagedResults results = new SubscriptionPagedResults();
		if (ids.isEmpty()) {
			results.setResults(new ArrayList<Subscription>(0));
			results.setTotalNumberOfResults(0L);
		} else {
			MapSqlParameterSource parameters = new MapSqlParameterSource("ids", ids);
			parameters.addValue("subscriberId", subscriberId);
			parameters.addValue("objectType", objectType.name());
			NamedParameterJdbcTemplate namedTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			List<Subscription> subscriptions = namedTemplate.query(SQL_GET_LIST, parameters, ROW_MAPPER);
			results.setResults(subscriptions);
			results.setTotalNumberOfResults((long) subscriptions.size());
		}
		return results;
	}

	@WriteTransactionReadCommitted
	@Override
	public void delete(long subscriptionId) {
		jdbcTemplate.update(SQL_DELETE, subscriptionId);
	}

	@WriteTransactionReadCommitted
	@Override
	public void deleteAll(Long userId) {
		jdbcTemplate.update(SQL_DELETE_ALL, userId);
	}

	@WriteTransactionReadCommitted
	@Override
	public void subscribeForumSubscriberToThread(String forumId, final String threadId) {
		ValidateArgument.required(forumId, "forumId");
		ValidateArgument.required(threadId, "threadId");
		List<String> forumSubscribers = getAllSubscribers(forumId, SubscriptionObjectType.FORUM);
		jdbcTemplate.batchUpdate(SQL_INSERT_IGNORE, forumSubscribers, forumSubscribers.size(), new ParameterizedPreparedStatementSetter<String>(){

			@Override
			public void setValues(PreparedStatement ps, String subscriberId)
					throws SQLException {
				ps.setLong(1, Long.parseLong(idGenerator.generateNewId(TYPE.SUBSCRIPTION_ID).toString()));
				ps.setLong(2, Long.parseLong(subscriberId));
				ps.setLong(3, Long.parseLong(threadId));
				ps.setString(4, SubscriptionObjectType.DISCUSSION_THREAD.name());
				ps.setLong(5, new Date().getTime());
			}
		});
	}

	@Override
	public List<String> getAllSubscribers(String objectId, SubscriptionObjectType objectType) {
		ValidateArgument.required(objectId, "objectId");
		ValidateArgument.required(objectType, "objectType");
		return jdbcTemplate.queryForList(SQL_GET_SUBSCRIBERS, new Object[]{objectId, objectType.name()}, String.class);
	}

	@Override
	public void subscribeAll(final String userId, List<String> idList, final SubscriptionObjectType objectType) {
		ValidateArgument.required(userId, "userId");
		ValidateArgument.required(idList, "idList");
		ValidateArgument.required(objectType, "objectType");
		jdbcTemplate.batchUpdate(SQL_INSERT_IGNORE, idList, idList.size(), new ParameterizedPreparedStatementSetter<String>(){

			@Override
			public void setValues(PreparedStatement ps, String objectId)
					throws SQLException {
				ps.setLong(1, Long.parseLong(idGenerator.generateNewId(TYPE.SUBSCRIPTION_ID).toString()));
				ps.setLong(2, Long.parseLong(userId));
				ps.setLong(3, Long.parseLong(objectId));
				ps.setString(4, objectType.name());
				ps.setLong(5, new Date().getTime());
			}
		});
	}
}
