package net.anfoya.movie.browser.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.anfoya.movie.browser.model.Movie;
import net.anfoya.movie.browser.model.Section;
import net.anfoya.movie.browser.model.Tag;

public class MovieTagDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(MovieTagDao.class);

	private final DataSource dataSource;

	public MovieTagDao(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void init() throws SQLException {

		final String sql = "CREATE TABLE IF NOT EXISTS movie_tag"
				+ "(	movie_id	INT		NOT NULL"
				+ ", 	tag_id		INT		NOT NULL"
				+ ",	PRIMARY KEY(movie_id, tag_id)"
				+ ",	CONSTRAINT fk_movie_id FOREIGN KEY (movie_id) REFERENCES movie (id) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ",	CONSTRAINT fk_tag_id FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE"
				+ ")";

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		statement.execute();
		statement.close();
		connection.close();
	}

	public void addTag(final Set<Movie> movies, final Tag tag) throws SQLException {
		if (movies.isEmpty()) {
			return;
		}

		String sql = "INSERT"
				+ " INTO movie_tag(movie_id,tag_id)"
				+ " VALUES ";
		for(int i=0, n=movies.size(); i<n; i++) {
			if (i > 0) {
				sql += ",";
			}
			sql += "(?,?)";
		}

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		int i = 0;
		for(final Movie movie: movies) {
			statement.setInt(++i, movie.getId());
			statement.setInt(++i, tag.getIntId());
		}
		statement.execute();
		statement.close();
		connection.close();
	}

	public void delTag(final Set<Movie> movies, final Tag tag) throws SQLException {
		if (movies.size() == 0) {
			return;
		}

		String sql = "DELETE"
				+ " FROM movie_tag"
				+ " WHERE ";
		for(int i=0, n=movies.size(); i<n; i++) {
			if (i > 0) {
				sql += " OR ";
			}
			sql += "(movie_id=? AND tag_id=?)";
		}

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		int i = 0;
		for(final Movie movie: movies) {
			statement.setInt(++i, movie.getId());
			statement.setInt(++i, tag.getIntId());
		}
		statement.execute();
		statement.close();
		connection.close();
	}

	public void del(final Set<Movie> movies) throws SQLException {
		if (movies.size() == 0) {
			return;
		}

		String sql = "DELETE"
				+ " FROM movie_tag"
				+ " WHERE movie_id IN (";
		for(int i=0, len=movies.size(); i<len; i++) {
			if (i != 0) {
				sql += ",";
			}
			sql += "?";
		}
		sql += ")";

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		int i=0;
		for(final Movie movie: movies) {
			statement.setInt(++i, movie.getId());
		}
		statement.execute();
		statement.close();
		connection.close();
	}

	public int countSectionMovies(final Section section, final Set<Tag> includes, final Set<Tag> excludes, final String namePattern) throws SQLException {
		final boolean incClause = !includes.isEmpty(), excClause = !excludes.isEmpty(), nameClause = !namePattern.isEmpty();
		String sql = "SELECT COUNT(DISTINCT(mt.movie_id)) AS movie_count"
				+ " FROM movie_tag mt"
				+ " JOIN movie m ON m.id = mt.movie_id"
				+ " JOIN tag t ON t.id = mt.tag_id"
				+ " WHERE t.section = ?"
				+ " AND t.name LIKE CONCAT('%', ?, '%')";

		if (incClause) {
			sql += " AND ? = (SELECT COUNT(*)"
					+ " FROM movie_tag"
					+ " WHERE movie_id = mt.movie_id"
					+ " AND tag_id IN (";
			for(int i=0, n=includes.size(); i<n; i++) {
				if (i != 0) {
					sql += ",";
				}
				sql += "?";
			}
			sql += "))";
		}

		if (excClause) {
			sql += " AND 0 = (SELECT COUNT(*)"
					+ " FROM movie_tag"
					+ " WHERE movie_id = mt.movie_id"
					+ " AND tag_id IN (";
			for(int i=0, n=excludes.size(); i<n; i++) {
				if (i != 0) {
					sql += ",";
				}
				sql += "?";
			}
			sql += "))";
		}

		if (nameClause) {
			sql += " AND m.path LIKE CONCAT('%', ?, '%')";
		}

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		int i=0;
		statement.setString(++i, section.getName());
		statement.setString(++i, namePattern);
		if (incClause) {
			statement.setInt(++i, includes.size());
			for(final Tag tag: includes) {
				statement.setInt(++i, tag.getIntId());
			}
		}
		for(final Tag tag: excludes) {
			statement.setInt(++i, tag.getIntId());
		}
		if (nameClause) {
			statement.setString(++i, namePattern);
		}

		int count = 0;
		final ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			count = rs.getInt("movie_count");
		}

		rs.close();
		statement.close();
		connection.close();
		return count;
	}

	public int countMovies(final Set<Tag> tags, final Set<Tag> excludes, final String nameSearch) throws SQLException {
		final boolean tagClause = !tags.isEmpty(), nameClause = !nameSearch.isEmpty(), excClause = !excludes.isEmpty();
		String sql = "SELECT COUNT(DISTINCT(mt.movie_id)) AS movie_count"
							+ " FROM movie_tag mt"
							+ " JOIN movie m ON m.id = mt.movie_id";
		if (tagClause || excClause || nameClause) {
			sql += " WHERE ";
		}
		if (tagClause) {
			sql += " ? = (SELECT COUNT(*)"
					+ " FROM movie_tag"
					+ " WHERE movie_id = mt.movie_id"
					+ " AND tag_id IN (";
			for(int i=0, n=tags.size(); i<n; i++) {
				if (i != 0) {
					sql += ",";
				}
				sql += "?";
			}
			sql += "))";
		}
		if (excClause) {
			if (tagClause) {
				sql += " AND ";
			}
			sql += " 0 = (SELECT COUNT(*)"
					+ " FROM movie_tag"
					+ " WHERE movie_id = mt.movie_id"
					+ " AND tag_id IN (";
			for(int i=0, n=excludes.size(); i<n; i++) {
				if (i != 0) {
					sql += ",";
				}
				sql += "?";
			}
			sql += "))";
		}
		if (nameClause) {
			if (tagClause || excClause) {
				sql += " AND ";
			}
			sql += " m.path LIKE CONCAT('%', ?, '%')";
		}

		LOGGER.debug(sql);
		final Connection connection = dataSource.getConnection();
		final PreparedStatement statement = connection.prepareStatement(sql);
		int i=0;
		if (tagClause) {
			statement.setInt(++i, tags.size());
			for(final Tag tag: tags) {
				statement.setInt(++i, tag.getIntId());
			}
		}
		for(final Tag tag: excludes) {
			statement.setInt(++i, tag.getIntId());
		}
		if (nameClause) {
			statement.setString(++i, nameSearch);
		}

		int count = 0;
		final ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			count = rs.getInt("movie_count");
		}

		rs.close();
		statement.close();
		connection.close();
		return count;
	}
}
