package org.repocrud.domain;

import org.repocrud.components.Identifiable;
import org.repocrud.history.Auditable;
import org.repocrud.repository.converters.AuthorityListToStringConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;


@Data
@Entity(name = "crud_user")
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
public class User extends Auditable implements UserDetails{



    @Column(unique = true)
    private String username;

    @JsonIgnore
    private String password;

    private boolean enabled = true;

    @Convert(converter = AuthorityListToStringConverter.class)
    private List<GrantedAuthority> authorities;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;

    private boolean credentialsNonExpired = true;

    @Enumerated(EnumType.STRING)
    private Language locale = Language.RUSSIAN;

    @ManyToOne
    protected Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<CrudHistory> crudHistories;


    public User(String username, String encodedPassword, List<SimpleGrantedAuthority> authorities) {
        this.username = username;
        this.password = encodedPassword;
        this.locale = Language.RUSSIAN;
        this.authorities = authorities.stream()
                .map(a -> (GrantedAuthority) a)
                .collect(Collectors.toList());
//        this.authorities = authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.joining(";"));

    }


//    @Override
//    public List<GrantedAuthority> getAuthorities() {
//        if (authorities == null || authorities.length() == 0) {
//            return Collections.emptyList();
//        }
//        return Stream.of(authorities.split(";")).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
//    }


    @Override
    public String toString() {
        return  username ;
    }
}
