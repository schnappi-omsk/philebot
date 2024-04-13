package com.gundomrays.philebot.telegram.bot.awards;

import com.gundomrays.philebot.telegram.data.AwardMsgRepository;
import com.gundomrays.philebot.telegram.data.AwardNomineeRepository;
import com.gundomrays.philebot.telegram.data.ChatAwardsRepository;
import com.gundomrays.philebot.telegram.domain.AwardMsg;
import com.gundomrays.philebot.telegram.domain.AwardNominee;
import com.gundomrays.philebot.telegram.domain.ChatAwards;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;

@Service
public class ChatAwardsService {

    private final static Logger log = LoggerFactory.getLogger(ChatAwardsService.class);

    private final AwardNomineeRepository awardNomineeRepository;
    private final ChatAwardsRepository chatAwardsRepository;

    private final AwardMsgRepository awardMsgRepository;

    public ChatAwardsService(AwardNomineeRepository awardNomineeRepository,
                             ChatAwardsRepository chatAwardsRepository,
                             AwardMsgRepository awardMsgRepository) {
        this.awardNomineeRepository = awardNomineeRepository;
        this.chatAwardsRepository = chatAwardsRepository;
        this.awardMsgRepository = awardMsgRepository;
    }

    public boolean hasWinner() {
        return chatAwardsRepository.findByAwardDate(LocalDate.now()).isPresent();
    }

    public AwardNominee todayWinner() {
        final ChatAwards awards = chatAwardsRepository.findByAwardDate(LocalDate.now()).orElse(null);
        if (awards == null) {
            return null;
        } else {
            return awardNomineeRepository
                    .findById(awards.getTgId())
                    .orElseThrow(() -> new RuntimeException("Winner is weirdly null!"));
        }
    }

    public AwardNominee awardGoesTo() {
        final Iterable<AwardNominee> nominees = awardNomineeRepository.findAll();

        final List<AwardNominee> nomineesList = StreamSupport.stream(nominees.spliterator(), false).toList();

        final Random random = new Random();
        final AwardNominee winner = nomineesList.get(random.nextInt(nomineesList.size()));
        final ChatAwards awards = grantAward(winner);
        log.info("Award for {} is granted to {}", awards.getAwardDate(), winner.getTgName());

        return winner;
    }

    public String congratulations() {
        final Iterable<AwardMsg> allMessages = awardMsgRepository.findAll();
        final List<AwardMsg> messages = StreamSupport.stream(allMessages.spliterator(), false).toList();
        final Random random = new Random();
        final AwardMsg message = messages.get(random.nextInt(messages.size()));
        return message.getMessage();
    }

    public boolean registerNominee(final String tgId, final String tgName) {
        AwardNominee nominee = awardNomineeRepository.findById(tgId).orElse(null);
        if (nominee == null) {
            saveNominee(tgId, tgName);
            log.info("Nominee {} was registered", tgName);
            return true;
        } else {
            log.info("Nominee {} is already registered", tgName);
            return false;
        }
    }

    private ChatAwards grantAward(final AwardNominee nominee) {
        return chatAwardsRepository.findByAwardDate(LocalDate.now()).orElse(saveAward(nominee));
    }

    private void saveNominee(final String tgId, final String tgName) {
        final AwardNominee nominee = new AwardNominee();
        nominee.setTgId(tgId);
        nominee.setTgName(tgName);
        awardNomineeRepository.save(nominee);
    }

    private ChatAwards saveAward(final AwardNominee nominee) {
        final ChatAwards chatAwards = new ChatAwards();
        chatAwards.setTgId(nominee.getTgId());
        chatAwards.setAwardDate(LocalDate.now());
        return chatAwardsRepository.save(chatAwards);
    }

}
