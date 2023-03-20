package ru.tgbot.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tgbot.dao.AppUserDao;
import ru.tgbot.dao.RawDataDAO;
import ru.tgbot.entity.AppUser;
import ru.tgbot.entity.RawData;
import ru.tgbot.entity.enums.UserState;
import ru.tgbot.service.MainService;

import static ru.tgbot.entity.enums.UserState.BASIC_STATE;
import static ru.tgbot.entity.enums.UserState.WAIT_FOR_EMAIL_STATE;
import static ru.tgbot.service.enums.ServiceCommands.*;

@Service
@Slf4j
public class MainServiceImpl implements MainService {
    private final RawDataDAO rawDataDAO;
    private final ProduceServiceImpl produceService;
    private final AppUserDao appUserDao;

    public MainServiceImpl(RawDataDAO rawDataDAO, ProduceServiceImpl produceService, AppUserDao appUserDao) {
        this.rawDataDAO = rawDataDAO;
        this.produceService = produceService;
        this.appUserDao = appUserDao;
    }

    @Override
    public void processTextMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        String text = update.getMessage().getText();
        var output = "";

        if (CANCEL.equals(text)) {
            cancelProcess(appUser);
        } else if (BASIC_STATE.equals(text)) {
            processServiceCommand(appUser, text);
        } else if (WAIT_FOR_EMAIL_STATE.equals(appUser.getState())) {
            //TODO добавить обратку почты
        } else {
            log.error("Unknown user state: " + appUser.getState());
            output = "Неизвестная ошибка! введите /cancel и попробуйте снова";
        }

        Long chatId = update.getMessage().getChatId();
        sendAnswer(output, chatId);

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hello From Node");
        produceService.produceAnswer(sendMessage);
    }

    @Override
    public void processPhotoMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //TODO: добавить сохранение документа
        var answer = "Фото успешно загружено! ЗДЕСЬ БУДЕТ ССЫЛКА ДЛЯ СКАЧИВАНИЯ";
        sendAnswer(answer, chatId);
    }

    @Override
    public void processDocMessage(Update update) {
        saveRawData(update);
        AppUser appUser = findOrSaveAppUser(update);
        Long chatId = update.getMessage().getChatId();
        if (isNotAllowToSendContent(chatId, appUser)) {
            return;
        }
        //TODO: добавить сохранение документа
        var answer = "Документ успешно загружен! ЗДЕСЬ БУДЕТ ССЫЛКА ДЛЯ СКАЧИВАНИЯ";
        sendAnswer(answer, chatId);
    }

    private boolean isNotAllowToSendContent(Long chatId, AppUser appUser) {
        UserState userState = appUser.getState();
        if (!appUser.getIsActive()){
            String errorText = "Зарегистрируйтесь или активируйте свою учетну запись.";
            sendAnswer(errorText, chatId);
            return true;
        } else if (!BASIC_STATE.equals(userState)){
            String errorText = "отмените текущую команду с помощью /cancel для отправки файлов";
            sendAnswer(errorText, chatId);
            return true;
        }
        return false;
    }

    private void sendAnswer(String output, Long chatId) {

    }

    private String processServiceCommand(AppUser appUser, String text) {
        if (REGISTRATION.equals(text)) {
            //TODO add registration
            return "Temporarily unavailable";
        } else if (HELP.equals(text)) {
            return help();
        } else if (START.equals(text)) {
            return "Привет! 4то бы посмотреть список команд введите /help";
        } else {
            return "Unknown command! 4то бы посмотреть список команд введите /help";
        }
    }

    private String help() {
        return "Список доступных команды:\n" +
                "/cancel - отмена выполнения текущей команды\n" +
                "/registration - регистрация";
    }

    private String cancelProcess(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDao.save(appUser);
        return "Команда отменена";
    }

    private AppUser findOrSaveAppUser(Update update) {
        User user = update.getMessage().getFrom();
        AppUser persistentAppUser = appUserDao.findAppUserByTelegramUserId(user.getId());
        if (persistentAppUser == null) {
            AppUser transientUser = AppUser.builder().telegramUserId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .userName(user.getUserName())
                    .isActive(true)
                    .build();
            return appUserDao.save(transientUser);
        }
        return persistentAppUser;
    }

    private void saveRawData(Update update) {
        RawData rawData = RawData.builder().event(update).build();
        rawDataDAO.save(rawData);
    }
}
