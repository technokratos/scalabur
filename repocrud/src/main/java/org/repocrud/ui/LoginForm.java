package org.repocrud.ui;


import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import lombok.extern.slf4j.Slf4j;
import org.repocrud.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.repocrud.text.LocalText.text;
import static org.repocrud.ui.components.Wrap.icon;


@Slf4j
@Route("login")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
public class LoginForm extends VerticalLayout {

    public static final String REMEMBER_ME = "remember-me";
    public static final String MAIN = "main";

    public LoginForm() {
        log.info("Init login form");
    }

    @Autowired
    private DaoAuthenticationProvider authenticationProvider;

    @Autowired
    private RememberMeAuthenticationProvider rememberMeAuthenticationProvider;

    @PostConstruct
    public void init() {


        FormLayout nameLayout = new FormLayout();

        VerticalLayout verticalLayout = new VerticalLayout();
        nameLayout.add(verticalLayout);

        verticalLayout.setHeight("600px");

        verticalLayout.getElement().getStyle().set("padding-top", "200px");

        verticalLayout.setMargin(true);
        verticalLayout.setPadding(true);

        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        TextField username = new TextField();

        username.setPlaceholder("логин");
        username.setAutocomplete(Autocomplete.ON);

        PasswordField passwordField = new PasswordField();
        passwordField.setPlaceholder("*****");
        passwordField.setAutocomplete(Autocomplete.ON);

        UI.getCurrent().addAfterNavigationListener( e -> redirect());

        Button loginButton = new Button("Логин");

        loginButton.addClickListener(event -> {
            String usernameValue = username.getValue();
            String passwordFieldValue = passwordField.getValue();
            login(usernameValue, passwordFieldValue);
        });

        verticalLayout.add(icon(VaadinIcon.USER, username), icon(VaadinIcon.PASSWORD, passwordField));
        verticalLayout.add(loginButton);
        HorizontalLayout horizontalLayout = new HorizontalLayout(verticalLayout);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        add(verticalLayout);
    }

    private void redirect() {
        User rememberedUser = getCookieUser();
        if (rememberedUser != null) {
//            username.setValue(rememberedUser.getUsername());
//            passwordField.setValue(rememberedUser.getUsername());
            try {
                Authentication auth = new RememberMeAuthenticationToken(LoginForm.REMEMBER_ME,
                        rememberedUser, rememberedUser.getAuthorities());
                Authentication authenticated = rememberMeAuthenticationProvider.authenticate(auth);
                SecurityContextHolder.getContext().setAuthentication(authenticated);
                User user = (User) authenticated.getPrincipal();
                rememberUser(user);
                getUI().ifPresent(ui -> ui.navigate(MAIN));
                Notification.show(text("remember") + " " + user.getUsername());
            } catch (Exception e) {
                log.error("Error in notification", e);
                Notification.show("Не правильный логин пароль");
            }
        }
    }

    private void login(String usernameValue, String passwordFieldValue) {
        try {
            Authentication auth = new UsernamePasswordAuthenticationToken(usernameValue, passwordFieldValue);
            Authentication authenticated = authenticationProvider.authenticate(auth);
            SecurityContextHolder.getContext().setAuthentication(authenticated);
            User user = (User) authenticated.getPrincipal();
            rememberUser(user);
            getUI().ifPresent(ui -> ui.navigate(MAIN));
        } catch (Exception e) {
            log.error("Error in notification", e);
            Notification.show("Не правильный логин пароль");
        }
    }

    private static void rememberUser(User user) {
        String id = UserCookieService.rememberUser(user);

        Cookie cookie = new Cookie(REMEMBER_ME, id);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30);
        VaadinService.getCurrentResponse().addCookie(cookie);

    }

    private static User getCookieUser() {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies != null) {
            Optional<String> first = Stream.of(cookies)
                    .filter(cookie -> REMEMBER_ME.equals(cookie.getName()))
                    .map(Cookie::getValue).findFirst();
            if (first.isPresent()) {
                return UserCookieService.getRememberedUser(first.get());
            }
        }
        return null;
    }


    public static class UserCookieService {

        private static SecureRandom random = new SecureRandom();

        private static Map<String, User> rememberedUsers = new ConcurrentHashMap<>();


        public static String rememberUser(User username) {
            String randomId = new BigInteger(130, random).toString(32);
            rememberedUsers.put(randomId, username);
            return randomId;
        }

        public static User getRememberedUser(String id) {
            return rememberedUsers.get(id);
        }

        public static void removeRememberedUser(User userName) {
                rememberedUsers.entrySet().stream().filter(e-> e.getValue().equals(userName))
                    .findFirst().ifPresent(e -> rememberedUsers.remove(e.getKey()));

        }

    }
}