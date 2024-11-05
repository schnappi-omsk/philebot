package com.gundomrays.philebot.telegram.bot.clown;

import com.gundomrays.philebot.telegram.bot.clown.domain.ClownRecord;
import com.gundomrays.philebot.telegram.bot.clown.domain.Wokeness;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Service
public class ClownService {

    private final static Logger log = LoggerFactory.getLogger(ClownService.class);

    @Value("${clown.csvUrl}")
    private String csvUrl;

    private final List<ClownRecord> records = new CopyOnWriteArrayList<>();

    @Scheduled(fixedDelay = 10L, timeUnit = TimeUnit.HOURS)
    public synchronized void refreshRecords() {
        InputStreamReader csvReader = null;
        log.info("Starting update from {}", csvUrl);
        try {
            final URI csvUri = new URI(csvUrl);
            final URL csvRemote = csvUri.toURL();
            csvReader = new InputStreamReader(csvRemote.openStream());
            final CSVFormat format = CSVFormat.Builder.create().setHeader("appid","name","banner", "woke", "description")
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .build();
            final CSVParser parser = new CSVParser(csvReader, format);
            final List<CSVRecord> csvRecords = parser.getRecords();

            if (csvRecords.size() > records.size()) {
                records.clear();
                for (CSVRecord record :csvRecords) {
                    final ClownRecord clownRecord = new ClownRecord();
                    clownRecord.setAppId(record.get("appid"));
                    clownRecord.setName(record.get("name"));
                    clownRecord.setBanner(record.get("banner"));
                    clownRecord.setWoke(Wokeness.isWoke(Integer.parseInt(record.get("woke"))));
                    clownRecord.setDescription(record.get("description"));
                    records.add(clownRecord);
                }
            } else {
                log.info("No need to update from {}", csvUrl);
            }

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException e) {
                    log.error("Cannot close stream reader.", e);
                }
            }
            log.info("Update from {} is finished, {} records loaded", csvUrl, records.size());
        }
    }

    public List<ClownRecord> searchGames(final String request) {
        String searchTerm = request.replaceAll(" ", ".*").toLowerCase();

        return records.stream()
                .filter(rec -> rec.getName().replaceAll("\\W", " ").toLowerCase().matches(".*" + searchTerm + ".*"))
                .toList();
    }

    public ClownRecord gameById(final String appId) {
        return records.stream()
                .filter(rec -> rec.getAppId().equals(appId))
                .findFirst()
                .orElse(null);
    }

}
