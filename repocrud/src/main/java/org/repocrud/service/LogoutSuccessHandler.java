package org.repocrud.service;

import org.repocrud.ui.LoginForm;
import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.repocrud.repository.CrudHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;

@Slf4j
@Component
public class LogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Autowired
    private CrudHistoryRepository historyRepository;

   @Override
   public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        super.onLogoutSuccess(request, response, authentication);

        if (authentication != null && authentication instanceof AbstractAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                LoginForm.UserCookieService.removeRememberedUser((User) principal);
                addAction((User) principal, CrudHistory.Operation.LOGOUT);
            }

        }
   }

    public void addAction(User user, CrudHistory.Operation login) {
        CrudHistory crudHistory = new CrudHistory(null, user, ZonedDateTime.now(), User.class.getSimpleName(), login,
               "");
        //getBody(args, User.class));
        historyRepository.saveAndFlush(crudHistory);
    }
}