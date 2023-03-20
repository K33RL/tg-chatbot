package ru.tgbot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tgbot.entity.AppUser;

public interface AppUserDao extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
