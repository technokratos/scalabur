package org.repocrud.crud;

import org.repocrud.config.SecurityUtils;
import org.repocrud.components.dialogs.ConfirmDialog;
import org.repocrud.domain.CrudHistory;
import org.repocrud.domain.User;
import org.repocrud.repository.CrudHistoryRepository;
import org.repocrud.repository.UserRepository;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.crudui.crud.CrudOperation;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Set;

import static org.repocrud.text.LocalText.text;

//import com.vaadin.flow.component.confirmdialog.ConfirmDialog;

@Slf4j
@UIScope
@SpringComponent
public class UserRepositoryCrud extends AbstractCrudContainer<User, Long> {

    public static final int DURATION = 1000;
    public static final String[] HIDE_FIELDS = {"id", "password", "authorities", "locale", "accountNonExpired", "accountNonLocked", "credentialsNonExpired"};
    private static final String[] VISIBLE_FIELDS_WITH_COMPANY = {"username", "enabled", "company"};
    private static final String[] VISIBLE_FIELDS = {"username", "enabled", "company"};
    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private CrudHistoryRepository historyRepository;

    @PostConstruct
    private void init() {
        RepositoryCrudFormFactory<User> formFactory = new RepositoryCrudFormFactory<>(User.class);


        final String[] visibleFields;
        if (((User) SecurityUtils.getUserDetails()).getCompany() == null) {
            visibleFields = VISIBLE_FIELDS_WITH_COMPANY;
        } else {
            visibleFields = VISIBLE_FIELDS;
        }

        formFactory.setVisibleProperties(CrudOperation.READ, visibleFields);
        formFactory.setVisibleProperties(CrudOperation.UPDATE, visibleFields);
        formFactory.setVisibleProperties(CrudOperation.ADD, visibleFields);
        formFactory.setVisibleProperties(CrudOperation.DELETE, "username");

        crud = new RepositoryCrud<>(repository, formFactory);
        getContent().add(crud);
        Button resetPassword = new Button(text(User.class, "resetPassword"));
        crud.getCrudLayout().addToolbarComponent(resetPassword);


        crud.setAddOperation(user -> {
            new ResetPasswordDialogAdapter(user).open();
            repository.saveAndFlush(user);
            return user;
        });

        resetPassword.setEnabled(false);
        crud.getGrid().addSelectionListener(selectionEvent -> {
            if (selectionEvent.getAllSelectedItems().size() == 1) {
                resetPassword.setEnabled(true);
            } else {
                resetPassword.setEnabled(false);
            }
        });

        resetPassword.addClickListener(buttonClickEvent -> {
            changePassword();
        });
        crud.addContextMenuItem(text(User.class, "resetPassword"), VaadinIcon.PASSWORD.create(), user -> changePassword(), "resetPassword");
        initHistory(crud);

    }

    private void changePassword() {
        Set<User> selectedItems = crud.getGrid().getSelectedItems();
        if (selectedItems.size() == 1) {
            User user = selectedItems.iterator().next();
            User currentUser = repository.findByUsernameIgnoreCase(SecurityUtils.getUsername());
            if (currentUser.getUsername().equals(user.getUsername()) ||
                    (currentUser.getAuthorities() != null &&
                    currentUser.getAuthorities().stream().anyMatch(grantedAuthority -> "admin".equals( grantedAuthority.getAuthority())))) {
                new ResetPasswordDialogAdapter(user).open();
            } else {
                Notification.show(text(UserRepositoryCrud.class, "noRightsToChange"), DURATION, Notification.Position.MIDDLE);
            }
        }
    }

    private void initHistory(RepositoryCrud<User, Long> crud) {
        RepositoryCrudFormFactory<CrudHistory> historyFormFactory = new RepositoryCrudFormFactory<>(CrudHistory.class);




        historyFormFactory.hideVisibleProperties("id", "body");
        RepositoryCrud<CrudHistory, Long> historyCrud = new RepositoryCrud<>(historyRepository, historyFormFactory);


        historyCrud.setPageCountSupplier(() -> 0L);
//        historyCrud.getGrid().setItemDetailsRenderer(new TextRenderer<>((ItemLabelGenerator<CrudHistory>) crudHistory -> crudHistory.getBody()!= null ? crudHistory.getBody(): ""));

        ComboBox<String> domainTypes = new ComboBox<>(text(User.class, "domainTypes"), historyRepository.allDomains());
        domainTypes.addValueChangeListener(event -> historyCrud.refreshGrid());

        historyCrud.getCrudLayout().addToolbarComponent(domainTypes);

        historyCrud.getGrid().setItemDetailsRenderer(new ComponentRenderer<Component, CrudHistory>() {
            @Override
            public Component createComponent(CrudHistory item) {
                return new Label(item.getBody() != null ? item.getBody() : "");
            }
        });

        historyCrud.setAddOperationVisible(false);
        historyCrud.setUpdateOperationVisible(false);
        historyCrud.setDeleteOperationVisible(false);
        historyCrud.setFindAllOperationVisible(false);
        historyCrud.setDeleteOperation(crudHistory -> {});
        historyCrud.setUpdateOperation(crudHistory -> crudHistory);
        historyCrud.setAddOperation(crudHistory -> crudHistory);



        historyCrud.setFindAllOperation(() ->  {
            Set<User> selectedItems = crud.getGrid().getSelectedItems();
            if (selectedItems.size() == 1) {
                User selected = selectedItems.iterator().next();
                selected.setCrudHistories(Collections.emptyList());
                PageRequest pageRequest = PageRequest.of(historyCrud.getPage(), historyCrud.getPageSize());

                String domain = domainTypes.getValue();

                final Page<CrudHistory> collections;
                if (domain == null) {
                    historyCrud.setPageCountSupplier(() -> historyRepository.countByUser(selected));
                    collections = historyRepository.findByUserOrderByTimeDesc(selected, pageRequest);
                } else {

                    historyCrud.setPageCountSupplier(() -> historyRepository.countByUserAndDomain(selected, domain));
                    collections = historyRepository.findByUserAndDomainOrderByTimeDesc(selected, domain, pageRequest);
                }


                return collections.getContent();
            } else {
                return Collections.emptyList();
            }
        });
        crud.addDependentView(historyCrud, user -> historyCrud.refreshGrid(), text(User.class, "subHistory"));
    }

    private class ResetPasswordDialogAdapter {

        private final ConfirmDialog confirmDialog;
        private final PasswordField passwordField;
        private final PasswordField secondPassword;

        public ResetPasswordDialogAdapter(User user) {
            passwordField = new PasswordField(text(User.class, "password"));
            secondPassword = new PasswordField(text(User.class, "repeatPassword"));
            EqualsPassword equalsPassword = new EqualsPassword(passwordField, secondPassword);
            passwordField.addValueChangeListener(new NotEmptyPassword(passwordField));
            passwordField.addValueChangeListener(equalsPassword);
            secondPassword.addValueChangeListener(equalsPassword);

            ConfirmDialog.ConfirmAction confirmAction = () -> {
                if (tooShort(passwordField.getValue())) {
                    Notification.show(text(User.class, "tooShortPassword"), DURATION, Notification.Position.MIDDLE);

                    return false;
                }
                if (secondPassword.getErrorMessage() != null && secondPassword.getErrorMessage().length() > 0) {
                    Notification.show(text(User.class, "passWordShouldBeEquals"), DURATION, Notification.Position.MIDDLE);

                    return false;
                }
                log.info("Updated password {}", user);
                user.setPassword(encoder.encode(passwordField.getValue()));
                repository.saveAndFlush(user);
                Notification.show(text(User.class, "passwordIsUpdated"));
                return true;
            };
            confirmDialog = new ConfirmDialog(
                    //text(User.class, "resetPasswordHeader"),
                    text(User.class, "resetPasswordText", user.getUsername()),
                    text(User.class, "resetPassword"),
                    text(User.class, "resetPasswordCancel"),
                    DURATION,
                    confirmAction,
                    new HorizontalLayout(passwordField, secondPassword)
            );
            log.info("Open reset password {}", user);
        }

        public void open() {
            confirmDialog.open();
        }
    }

    @AllArgsConstructor
    private class NotEmptyPassword implements HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<PasswordField, String>> {
        private final PasswordField passwordField;


        @Override
        public void valueChanged(AbstractField.ComponentValueChangeEvent<PasswordField, String> e) {
            String value = e.getValue();
            if (tooShort(value)) {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage(text(User.class, "tooShortPassword"));
            } else {
                passwordField.setInvalid(false);
                passwordField.setErrorMessage("");
            }
        }
    }

    public boolean tooShort(String value) {
        return value == null || value.length() < 4;
    }

    @AllArgsConstructor
    private class EqualsPassword implements HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<PasswordField, String>> {
        private final PasswordField passwordField;
        private final PasswordField second;


        @Override
        public void valueChanged(AbstractField.ComponentValueChangeEvent<PasswordField, String> e) {

            String value = second.getValue();
            if (value == null || !value.equals(passwordField.getValue())) {
                second.setInvalid(true);
                second.setErrorMessage(text(User.class, "passWordShouldBeEquals"));
            } else {
                second.setInvalid(false);
                second.setErrorMessage("");
            }
        }
    }

}


