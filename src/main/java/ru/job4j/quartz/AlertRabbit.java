package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {
    private static final Logger LOG = LoggerFactory.getLogger(AlertRabbit.class);

    private static Properties loadProperties() {
        var cfg = new Properties();
        String fileName = "rabbit.properties";
        try (InputStream is = AlertRabbit.class.getClassLoader()
                .getResourceAsStream(fileName)) {
            cfg.load(is);
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Не удалось загрузить настройки из файла '%s'", fileName));
        }
        return cfg;
    }

    private static Scheduler createScheduler(Properties cfg, Connection cn)
            throws SchedulerException {
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        JobDataMap data = new JobDataMap();
        data.put("connection", cn);
        JobDetail job = newJob(Rabbit.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(
                        Integer.parseInt(cfg.getProperty("rabbit.interval"))
                )
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
        return scheduler;
    }

    public static class Rabbit implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            LOG.info("Rabbit runs here ...");
            Connection cn = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (var ps = cn.prepareStatement(
                    "insert into rabbit(created_date) values(?);")) {
                ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                ps.execute();
            } catch (SQLException e) {
                LOG.error("Ошибка при записи в таблицу 'rabbit'.", e);
            }
        }
    }
}
