package org.repocrud.crud;

import org.repocrud.domain.SmtpSettings;
import org.repocrud.repository.SmtpSettingsRepository;
import org.repocrud.service.SmtpFactoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

import static org.repocrud.text.LocalText.text;

/**
 * @author Denis B. Kulikov<br/>
 * date: 07.11.2018:18:08<br/>
 */
@Slf4j
@UIScope
@SpringComponent
public class SettingsCrudContainer extends AbstractCrudContainer<SmtpSettings, Long> {

    @Autowired
    private SmtpSettingsRepository repository;

    @Autowired
    private SmtpFactoryService smtpFactoryService;

    @PostConstruct
    private void init() {
        RepositoryCrudFormFactory<SmtpSettings> formFactory = new RepositoryCrudFormFactory<>(SmtpSettings.class);

        formFactory.hideVisibleProperties("smptPassword");

        formFactory.addCustomComponent(smtpSettings -> {
            PasswordField passwordField = new PasswordField(text(SmtpSettings.class, "smptPassword"));
            if (smtpSettings.getSmptPassword() != null) {
                passwordField.setValue(smtpSettings.getSmptPassword());
            }
            formFactory.getBinder().bind(passwordField, "smptPassword");
            return passwordField;
        });
        crud = new RepositoryCrud<>(repository, formFactory);
        getContent().add(crud);

        crud.setDeleteOperationVisible(false);

        crud.setFindAllOperation(() -> repository.findByCompany(null));
        crud.setPageCountSupplier(() -> {
            Long count = repository.countByCompany(null);
            crud.getAddButton().setEnabled(count < 1);
            return count;
        });
        Grid.Column<SmtpSettings> settingsColumn = crud.getGrid().addComponentColumn(this::getTestButton);
        settingsColumn.setHeader(text(SettingsCrudContainer.class, "check"));

    }

    private Button getTestButton(SmtpSettings smtpSettings) {
        Button button = new Button(text("check"));
        button.addClickListener(event -> {

            boolean test = smtpFactoryService.sendWarning("test", smtpSettings);
            if (test) {
                Notification.show(text(SettingsCrudContainer.class, "sentTestMessage"));
            } else {
                Notification.show(text(SettingsCrudContainer.class, "failedConnection"));
            }
        });
        return button;
    }

}
