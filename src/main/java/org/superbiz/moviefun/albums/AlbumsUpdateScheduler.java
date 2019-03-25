package org.superbiz.moviefun.albums;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DataSource dataSource;
     //   dataSource.setUrl(System.getenv("SPRING_DATASOURCE_URL"));


    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater,  DataSource dataSource) {

        this.albumsUpdater = albumsUpdater;
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            if (startAlbumSchedulerTask()) {
                logger.debug("Starting albums update");
                albumsUpdater.update();
                logger.debug("Finished albums update");

            } else {
                logger.debug("Nothing to start");
            }

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }



    private boolean startAlbumSchedulerTask(){


        String sql = "UPDATE album_scheduler_task SET started_at = NOW() WHERE started_at IS NULL OR TIMESTAMPDIFF(MINUTE,started_at,NOW()) > 2" ;

        if (jdbcTemplate.update(sql)  == 0)
        {
           return false;
        }
        return true;
    }
}
