package com.mchan.authorization.service.spring.security;

import com.mchan.authorization.service.entities.dao.AccountDao;
import com.mchan.authorization.service.entities.dao.SessionsDao;
import com.mchan.authorization.service.entities.dao.entities.ClassicAccountEntity;
import com.mchan.authorization.service.entities.dao.entities.SessionEntity;
import com.mchan.authorization.service.entities.utils.DateProvider;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * Records an actual interactive login once, without writes for every Basic-authenticated read.
 */
@Component
public class LoginAuditListener {
    private final AccountDao accounts;
    private final SessionsDao sessions;
    private final DateProvider dates;

    /**
     * Creates bounded-purpose browser login auditing using the existing session table.
     */
    public LoginAuditListener(AccountDao accounts, SessionsDao sessions, DateProvider dates) {
        this.accounts = accounts;
        this.sessions = sessions;
        this.dates = dates;
    }

    /**
     * Persists one interactive authentication event without credentials or tokens.
     */
    @EventListener
    public void loggedIn(InteractiveAuthenticationSuccessEvent event) {
        ClassicAccountEntity account = accounts.getAccountByEmail(event.getAuthentication().getName());
        if (account != null) {
            sessions.createSession(SessionEntity.builder().accountId(account.getAccountId())
                .sessionType(account.getAccountType()).sessionTime(dates.now()).build());
        }
    }
}
