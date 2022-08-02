package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class);
    private Connection cnn;

    public PsqlStore(Properties cfg) {
        String driver = cfg.getProperty("db.driver");
        String url = cfg.getProperty("db.url");
        String user = cfg.getProperty("db.username");
        String pwd = cfg.getProperty("db.password");
        try {
            Class.forName(driver);
            cnn = DriverManager.getConnection(url, user, pwd);
        } catch (SQLException | ClassNotFoundException e) {
            throw new IllegalStateException(String.format(
                    "Не удалось подключиться к БД. Driver: %s, url: %s, user: %s, password: %s",
                    driver, url, user, pwd), e);
        }
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement ps = cnn.prepareStatement("""
                insert into post(name, text, link, created)
                values(?,?,?,?)
                on conflict (link) do nothing;
                """)) {
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getDescription());
            ps.setString(3, post.getLink());
            ps.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            ps.execute();
        } catch (SQLException e) {
            LOG.error("Не удалось сохранить запись в БД: {}", post, e);
        }
    }

    @Override
    public List<Post> getAll() {
        final List<Post> posts = new ArrayList<>();
        try (PreparedStatement ps = cnn.prepareStatement("select * from post;")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(createPost(rs));
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось получить записи из БД", e);
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post = null;
        try (PreparedStatement ps = cnn.prepareStatement("select * from post where id=?;")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    post = createPost(rs);
                }
            }
        } catch (SQLException e) {
            LOG.error("Не удалось получить запись из БД по id = {}", id, e);
        }
        return post;
    }

    @Override
    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post createPost(ResultSet rs) throws SQLException {
        return new Post(rs.getInt("id"),
                rs.getString("name"),
                rs.getString("link"),
                rs.getString("text"),
                rs.getTimestamp("created").toLocalDateTime());
    }
}
