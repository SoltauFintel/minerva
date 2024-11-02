package minerva.model;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;

import github.soltaufintel.amalia.timer.BaseTimer;
import github.soltaufintel.amalia.web.config.AppConfig;
import gitper.base.FileService;
import minerva.MinervaWebapp;
import minerva.base.NlsString;

public class JournalSO {
    private static final String handle = "journal";
    public static final String FOLDER = "_journal";
    private final UserSO user;
    
    public JournalSO(UserSO user) {
        this.user = user;
    }

    public void save(String branch, String id, NlsString title, NlsString content) {
        synchronized (handle) {
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
            File file = new File(user.getUserFolder() + "/" + FOLDER + "/" + branch + "/" + now + "_" + id + ".json");
            Map<String, String> data = new HashMap<>();
            if (title != null) {
                title.putTo("title_", data);
            }
            content.putTo("content_", data);
            FileService.saveJsonFile(file, data);
            Logger.debug("Journal entry saved: " + file.getAbsolutePath());
            user.log("Journal entry saved: " + file.toString());
        }
    }

    public static String cleanupAllJournals() {
        int ret = 0;
        final LocalDate now = LocalDate.now();
        File[] files = new File(MinervaWebapp.factory().getConfig().getWorkspacesFolder()).listFiles();
        if (files != null) {
            for (File userFolder : files) {
                File journalDir = new File(userFolder, FOLDER);
                if (journalDir.isDirectory()) {
                    ret += processBranchDirs(journalDir, now); // under journal folder are branch dirs
                }
            }
        }
        Logger.info("Cleanup journals. deleted=" + ret);
        return "deleted=" + ret;
    }

    private static int processBranchDirs(File journalDir, LocalDate now) {
        int ret = 0;
        File[] branchDirs = journalDir.listFiles();
        if (branchDirs != null) {
            for (File branchDir : branchDirs) {
                if (branchDir.isDirectory()) {
                    ret += processJournalFiles(branchDir, now);
                }
            }
        }
        return ret;
    }

    private static int processJournalFiles(File branchDir, LocalDate now) {
        int ret = 0;
        File[] journalFiles = branchDir.listFiles();
        if (journalFiles != null) {
            for (File journalFile : journalFiles) {
                if (journalFile.isFile() && journalFile.getName().endsWith(".json")) {
                    if (processJournalFile(journalFile, now)) {
                        ret++;
                    }
                }
            }
        }
        return ret;
    }

    private static boolean processJournalFile(File journalFile, LocalDate now) {
        try {
            final String pattern = "yyyy-MM-dd";
            String dateStr = journalFile.getName().substring(0, pattern.length());
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            long days = ChronoUnit.DAYS.between(date, now);
            if (days > 30 && journalFile.delete()) {
                Logger.debug("old journal file deleted: " + journalFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            Logger.error("ignore journal file: " + journalFile.getAbsolutePath() + " => " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Cleanup journal once a month
     */
    public static class JournalTimer extends BaseTimer {
        private static String cron;
        
        public static void startTimer(AppConfig config) {
            cron = config.get("JournalTimer.cron", "0 0 6 1 * ?"); // first day of month 6:00
            new JournalTimer().start();
        }
        
        @Override
        protected void config() throws SchedulerException {
            start(cron);
        }

        @Override
        protected void timerEvent(JobExecutionContext context) throws Exception {
            cleanupAllJournals();
        }
    }
}
